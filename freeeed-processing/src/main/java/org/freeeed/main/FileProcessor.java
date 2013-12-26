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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
import org.freeeed.mail.EmailProperties;
import org.freeeed.ocr.OCRProcessor;
import org.freeeed.print.OfficePrint;
import org.freeeed.services.Settings;
import org.freeeed.services.Util;
import org.freeeed.services.Project;
import org.freeeed.services.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.lucene.index.IndexReader;


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
    abstract public void process() throws IOException, InterruptedException;

    /**
     * Cull, then emit responsive files
     *
     * @param tempFile Temporary uncompressed file on disk
     * @param originalFileName Original file name
     * @throws IOException
     * @throws InterruptedException
     */
    protected void processFileEntry(String tempFile, String originalFileName)
            throws IOException, InterruptedException {
        Project project = Project.getProject();
        project.incrementCurrentMapCount();
        if (!project.isMapCountWithinRange()) {
            return;
        }
        // update application log
        logger.trace("Processing file: {}", originalFileName);
        // set to true if file matches any query params
        boolean isResponsive = false;
        // exception message to place in output if error occurs
        String exceptionMessage = null;
        // Document metadata, derived from Tika metadata class
        DocumentMetadata metadata = new DocumentMetadata();
        try {
            metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH,
                    getOriginalDocumentPath(tempFile, originalFileName));
            // extract file contents with Tika
            // Tika metadata class contains references to metadata and file text
            extractMetadata(tempFile, metadata, originalFileName);
            if (project.isRemoveSystemFiles() && Util.isSystemFile(metadata)) {
                // TODO should we log denisting?
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
            createImage(tempFile, metadata, originalFileName);
            emitAsMap(tempFile, metadata, originalFileName);
        }
        logger.trace("Is the file responsive: {}", isResponsive);
    }

    private boolean isPdf() {
        return Project.getProject().isCreatePDF();
    }

    private void createImage(String fileName, Metadata metadata, String originalFileName) {
        if (isPdf()) {
            OfficePrint.getInstance().createPdf(fileName, fileName + ".pdf", originalFileName);
        }
    }

    /**
     * Add the search result (Tika metadata) to Hadoop context as a map Key is
     * the MD5 of the file used to create map
     *
     * @param fileName Filename of file search performed on
     * @param metadata Metadata extracted from search
     * @throws IOException
     * @throws InterruptedException
     */
    @SuppressWarnings("unchecked")
    private void emitAsMap(String fileName, Metadata metadata, String originalFileName) throws IOException, InterruptedException {
        MapWritable mapWritable = createMapWritable(metadata, fileName);
        //create the hash for this type of file
        MD5Hash key = createKeyHash(fileName, metadata, originalFileName);
        // emit map
        if (PlatformUtil.isNix()) {
            context.write(key, mapWritable);
            context.progress();
        } else {
            ArrayList<MapWritable> values = new ArrayList<>();
            values.add(mapWritable);
            WindowsReduce.getInstance().reduce(key, values, null);
        }
        // update stats
        Stats.getInstance().increaseItemCount();
    }
    
    public static MD5Hash createKeyHash(String fileName, Metadata metadata, String originalFileName) throws IOException {
        String extension = Util.getExtension(originalFileName);
        
        if ("eml".equalsIgnoreCase(extension)) {
            String hashNames = EmailProperties.getInstance().getProperty(EmailProperties.EMAIL_HASH_NAMES);
            String[] hashNamesArr = hashNames.split(",");
            
            StringBuilder data = new StringBuilder();
            
            for (String hashName : hashNamesArr) {
                String value = metadata.get(hashName);
                if (value != null) {
                    data.append(value);
                    data.append(" ");
                }
            }
            
            return MD5Hash.digest(data.toString());
        } else {
            MD5Hash key;
            try ( //use MD5 of the input file as Hadoop key
            FileInputStream fileInputStream = new FileInputStream(fileName)) {
                key = MD5Hash.digest(fileInputStream);
            }
            
            return key;
        }
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
        return mapWritable;
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
     * Extracts document metadata. Text is part of it. Forensics information is
     * part of it.
     *
     * @param tempFile
     * @return DocumentMetadata
     */
    private void extractMetadata(String tempFile, DocumentMetadata metadata, String originalFileName) {
        DocumentParser.getInstance().parse(tempFile, metadata, originalFileName);
        //System.out.println(Util.toString(metadata));
        
        //OCR processing
        if (Project.getProject().isOcrEnabled()) {
            OCRProcessor ocrProcessor = OCRProcessor.createProcessor(Settings.getSettings().getOCRDir(), context);
            List<String> images = ocrProcessor.getImageText(tempFile);

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

    abstract String getOriginalDocumentPath(String tempFile, String originalFileName);
}
