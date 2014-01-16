/*
 *
 * Copyright SHMsoft, Inc. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freeeed.main;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.data.index.SolrIndex;
import org.freeeed.html.DocumentToHtml;
import org.freeeed.ocr.OCRProcessor;
import org.freeeed.print.OfficePrint;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.services.Stats;
import org.freeeed.services.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

/**
 * Opens the file, creates Lucene index and searches, then updates Hadoop map
 */
public abstract class FileProcessor {

    private static Logger logger = LoggerFactory.getLogger(FileProcessor.class);
    private String zipFileName;
    private String singleFileName;
    private Context context;            // Hadoop processing result context
    protected int docCount;
    private LuceneIndex luceneIndex;

    public String getZipFileName() {
        return zipFileName;
    }

    public String getSingleFileName() {
        return singleFileName;
    }

    public Context getContext() {
        return context;
    }

    public LuceneIndex getLuceneIndex() {
        return luceneIndex;
    }

    /**
     * Zip files are the initial file format passed to Hadoop map step
     *
     * @param zipFileName
     */
    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }

    // TODO method not used in project
    public void setSingleFileName(String singleFileName) {
        this.singleFileName = singleFileName;
    }

    /**
     * Constructor
     *
     * @param context Set Hadoop processing context
     */
    public FileProcessor(Context context, LuceneIndex luceneIndex) {
        this.context = context;
        this.luceneIndex = luceneIndex;
    }

    /**
     * @throws IOException
     * @throws InterruptedException
     */
    abstract public void process(boolean hasAttachments, MD5Hash hash) throws IOException, InterruptedException;

    /**
     * Cull, then emit responsive files.
     *
     * @param discoveryFile object with info for processing discovery.
     * @throws IOException on any IO problem.
     * @throws InterruptedException throws by Hadoop.
     */
    protected void processFileEntry(DiscoveryFile discoveryFile)
            throws IOException, InterruptedException {
        Project project = Project.getProject();
        project.incrementCurrentMapCount();
        if (!project.isMapCountWithinRange()) {
            return;
        }
        // update application log
        logger.trace("Processing file: {}", discoveryFile.getRealFileName());
        // set to true if file matches any query params
        boolean isResponsive = false;
        // exception message to place in output if error occurs
        String exceptionMessage = null;
        // Document metadata, derived from Tika metadata class
        DocumentMetadata metadata = new DocumentMetadata();
        try {
            metadata.setOriginalPath(getOriginalDocumentPath(discoveryFile));
            metadata.setHasAttachments(discoveryFile.isHasAttachments());
            // extract file contents with Tika
            // Tika metadata class contains references to metadata and file text
            extractMetadata(discoveryFile, metadata);
            if (project.isRemoveSystemFiles() && Util.isSystemFile(metadata)) {
                logger.info("File {} is recognized as system file and is not processed further",
                        discoveryFile.getPath().getPath());
                return;
            }
            metadata.set(DocumentMetadataKeys.CUSTODIAN, project.getCurrentCustodian());
            // search through Tika results using Lucene
            isResponsive = isResponsive(metadata);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            logger.warn("Exception processing file ", e);
            exceptionMessage = e.getMessage();
        }
        // update exception message if error
        if (exceptionMessage != null) {
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, exceptionMessage);
        }
        if (isResponsive || exceptionMessage != null) {
            createImage(discoveryFile);
            createHtmlForDocument(discoveryFile);
            emitAsMap(discoveryFile, metadata);
        }
        logger.trace("Is the file responsive: {}", isResponsive);
    }

    private boolean isPdf() {
        return Project.getProject().isCreatePDF();
    }

    private void createImage(DiscoveryFile discoveryFile) {
        if (isPdf()) {
            OfficePrint.getInstance().createPdf(discoveryFile.getPath().getPath(),
                    discoveryFile.getPath().getPath() + ".pdf", discoveryFile.getRealFileName());
        }
    }

    private void createHtmlForDocument(DiscoveryFile discoveryFile) throws IOException {
        //first make sure the output directory is empty
        File outputDir = new File(getHtmlOutputDir());
        if (outputDir.exists()) {
            Util.deleteDirectory(outputDir);
        }
        
        outputDir.mkdirs();
        
        //convert using open office (special processing for eml files)
        String outputHtmlFileName = outputDir.getPath() + File.separator + discoveryFile.getPath().getName() + ".html";
        DocumentToHtml.getInstance().createHtml(discoveryFile.getPath().getPath(), 
                outputHtmlFileName, discoveryFile.getRealFileName());
        //link the image files to be downloaded by the UI file download controller
        prepareImageSrcForUI(outputHtmlFileName, discoveryFile.getPath().getName());
    }
    
    private void prepareImageSrcForUI(String htmlFileName, String docName) throws IOException {
        File htmlFile = new File(htmlFileName);
        String htmlContent = Files.toString(htmlFile, Charset.defaultCharset());
        String replaceWhat = "SRC=\"" + Pattern.quote(docName) + "_html_";
        String replacement = "SRC=\"filedownload.html?action=exportHtmlImage&docPath=" + docName + "_html_";
        htmlContent = htmlContent.replaceAll(replaceWhat, replacement);
        
        Files.write(htmlContent, htmlFile, Charset.defaultCharset());
    }
    
    private String getHtmlOutputDir() {
        String outputDir = Settings.getSettings().getHTMLDir();
        if (PlatformUtil.isNix()) {
            return outputDir + File.separator + context.getTaskAttemptID();
        }
        
        return outputDir;
    }
    
    /**
     * Add the search result (Tika metadata) to Hadoop context as a map Key is the MD5 of the file used to create map.
     *
     * @param fileName Filename of file search performed on.
     * @param metadata Metadata extracted from search.
     * @throws IOException thrown on any IO problem.
     * @throws InterruptedException thrown by Hadoop processing.
     */
    @SuppressWarnings("unchecked")
    private void emitAsMap(DiscoveryFile discoveryFile, Metadata metadata)
            throws IOException, InterruptedException {
        MapWritable mapWritable = createMapWritable(metadata, discoveryFile.getPath().getPath());
        MD5Hash hash = Util.createKeyHash(discoveryFile.getPath(), metadata);
        // if this is a standalone file, not an attachment, create its key as a hash, otherwise
        // use pre-computed hash (which is that of its parent) together with this file's hash as a compound key         
        String mrkey = discoveryFile.getHash() == null ? hash.toString() + "\t#"
                : discoveryFile.getHash().toString() + "\t" + hash.toString();
        if (PlatformUtil.isNix()) {
            context.write(new Text(mrkey), mapWritable);
            context.progress();
        } else {
            ArrayList<MapWritable> values = new ArrayList<>();
            values.add(mapWritable);
            WindowsReduce.getInstance().reduce(new Text(hash.toString()), values, null);
        }
        // update stats
        // TODO use counters
        Stats.getInstance().increaseItemCount();
    }

    /**
     * Create a map
     *
     * @param metadata Hadoop metadata to insert into map
     * @param fileName File currently in process
     * @return Created map
     * @throws IOException
     */
    private MapWritable createMapWritable(Metadata metadata, String fileName) throws IOException {
        MapWritable mapWritable = new MapWritable();
        String[] names = metadata.names();
        for (String name : names) {
            mapWritable.put(new Text(name), new Text(metadata.get(name)));
        }
        byte[] bytes = new File(fileName).length() < ParameterProcessing.ONE_GIG
                ? Util.getFileContent(fileName)
                : "File too large".getBytes();
        mapWritable.put(new Text(ParameterProcessing.NATIVE), new BytesWritable(bytes));

        if (isPdf()) {
            String pdfFileName = fileName + ".pdf";
            if (new File(pdfFileName).exists()) {
                byte[] pdfBytes = Util.getFileContent(pdfFileName);
                mapWritable.put(new Text(ParameterProcessing.NATIVE_AS_PDF), new BytesWritable(pdfBytes));
            }
        }
        
        createMapWritableForHtml(mapWritable);
        
        return mapWritable;
    }
    
    private void createMapWritableForHtml(MapWritable mapWritable) throws IOException {
        //html processing
        
        //keep the track of all generated files - html + images
        List<String> htmlFiles = new ArrayList<String>();
        
        File htmlOutputDir = new File(getHtmlOutputDir());
        //get all generated files
        String[] files = htmlOutputDir.list();
        if (files != null) {
            for (String file : files) {
                String htmlFileName = htmlOutputDir.getPath() + File.separator + file;
                File htmlFile = new File(htmlFileName);
                if (htmlFile.exists()) {
                    if ("html".equalsIgnoreCase(Util.getExtension(htmlFile.getName()))) {
                        byte[] htmlBytes = Util.getFileContent(htmlFileName);
                        mapWritable.put(new Text(ParameterProcessing.NATIVE_AS_HTML_NAME), new BytesWritable(htmlBytes));
                    } else {
                        byte[] htmlBytes = Util.getFileContent(htmlFileName);
                        String key = ParameterProcessing.NATIVE_AS_HTML + "_" + file;
                        mapWritable.put(new Text(key), new BytesWritable(htmlBytes));
                        
                        htmlFiles.add(file);
                    }
                }
            }
        }
        
        if (htmlFiles.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String file : htmlFiles) {
                sb.append(file).append(",");
            }
            
            mapWritable.put(new Text(ParameterProcessing.NATIVE_AS_HTML), new Text(sb.toString()));
        }
    }

    /**
     * Search metadata and file contents
     *
     * @param metadata
     * @return true if match is found else false
     */
    private boolean isResponsive(Metadata metadata) {
        // set true if search finds a match
        boolean isResponsive = false;

        // get culling parameters
        String queryString = Project.getProject().getCullingAsTextBlock();

        // TODO parse important parameters to mappers and reducers individually, not globally
        IndexWriter writer = null;
        RAMDirectory idx = null;
        try {
            // construct a RAMDirectory to hold the in-memory representation of the index.
            idx = new RAMDirectory();

            // make a writer to create the index
            writer = new IndexWriter(idx, new StandardAnalyzer(Version.LUCENE_30),
                    true, IndexWriter.MaxFieldLength.UNLIMITED);

            writer.addDocument(createDocument(metadata));

            // close the writer to finish building the index
            writer.close();

            //adding the build index to FS
            if (Project.getProject().isLuceneIndexEnabled() && luceneIndex != null) {
                luceneIndex.addToIndex(idx);
            }

            SolrIndex.getInstance().addBatchData(metadata);

            if (queryString == null || queryString.trim().isEmpty()) {
                return true;
            }
            IndexReader indexReader = IndexReader.open(idx);
            try (IndexSearcher searcher = new IndexSearcher(indexReader)) {
                isResponsive = search(searcher, queryString);
            }
        } catch (IOException | ParseException e) {
            // TODO handle this better
            // if anything happens - don't stop processing
            e.printStackTrace(System.out);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (idx != null) {
                    idx.close();
                }
            } catch (Exception e) {
                // swallow exception, what else can you do now?
            }
        }
        return isResponsive;
    }

    /**
     * Create Apache Lucene document
     *
     * @param title Title of document
     * @param content Document contents
     * @return Lucene document
     */
    private static Document createDocument(Metadata metadata) {
        // add some Document objects containing quotes
        String title = metadata.get(ParameterProcessing.TITLE);
        // TODO - where is my title?
        if (title == null) {
            title = "";
        }

        String content = metadata.get(DocumentMetadataKeys.DOCUMENT_TEXT);

        Document doc = new Document();
        doc.add(new Field(ParameterProcessing.TITLE, title.toLowerCase(), Field.Store.YES, Field.Index.ANALYZED));
        if (content != null) {
            doc.add(new Field(ParameterProcessing.CONTENT, content.toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
        }

        //add all metadata fields
        String[] metadataNames = metadata.names();
        for (String name : metadataNames) {
            String data = metadata.get(name);
            doc.add(new Field(name, data.toLowerCase(), Field.Store.YES, Field.Index.ANALYZED));
        }

        return doc;
    }

    /**
     * Search for query
     *
     * @param searcher Lucene index
     * @param queryString What to search for
     * @return True if matches found, else False
     * @throws ParseException
     * @throws IOException
     */
    private static boolean search(IndexSearcher searcher, String queryString)
            throws ParseException, IOException {
        // explode search string input string into OR search
        String parsedQuery = parseQueryString(queryString);
        // Lucene query parser
        QueryParser queryParser = new QueryParser(Version.LUCENE_30,
                "content",
                new StandardAnalyzer(Version.LUCENE_30));
        if (parsedQuery.length() == 0) {
            return true;
        } else {
            // Build a Query object
            Query query = queryParser.parse(parsedQuery);
            // Search for the query
            TopDocs topDocs = searcher.search(query, 1);
            return topDocs.totalHits > 0;
        }
    }

    /**
     * Add OR statements to search input
     *
     * @param queryString
     * @return
     */
    private static String parseQueryString(String queryString) {
        StringBuilder query = new StringBuilder();
        String[] strings = queryString.split("\n");
        for (int i = 0; i < strings.length; ++i) {
            String string = strings[i];
            query.append(string);
            if (i < strings.length - 1) {
                query.append(" OR ");
            }
        }
        return query.toString();
    }

    /**
     * Extracts document metadata. Text is part of it. Forensics information is part of it.
     *
     * @param tempFile (temporary) file from which we extract metadata.
     * @return DocumentMetadata container receiving metadata.
     */
    private void extractMetadata(DiscoveryFile discoveryFile, DocumentMetadata metadata) {
        DocumentParser.getInstance().parse(discoveryFile, metadata);
        //System.out.println(Util.toString(metadata));

        //OCR processing
        if (Project.getProject().isOcrEnabled()) {
            OCRProcessor ocrProcessor = OCRProcessor.createProcessor(Settings.getSettings().getOCRDir(), context);
            List<String> images = ocrProcessor.getImageText(discoveryFile.getPath().getPath());

            if (images != null && images.size() > 0) {
                StringBuilder allContent = new StringBuilder();

                String documentContent = metadata.get(DocumentMetadataKeys.DOCUMENT_TEXT);
                allContent.append(documentContent);

                for (String image : images) {
                    allContent.append(System.getProperty("line.separator")).append(image);
                }

                metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, allContent.toString());
            }
        }
    }

    abstract String getOriginalDocumentPath(DiscoveryFile discoveryFile);
}
