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

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.tika.metadata.Metadata;
import org.freeeed.data.index.ESIndex;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.html.DocumentToHtml;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.print.OfficePrint;
import org.freeeed.services.*;
import org.freeeed.util.Util;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Opens the file, creates Lucene index and searches, then updates Hadoop map
 */
public class FileProcessor implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileProcessor.class);
    protected String zipFileName;
    protected String singleFileName;
    protected MetadataWriter metadataWriter;
    protected LuceneIndex luceneIndex;
    DiscoveryFile discoveryFile;


    public FileProcessor() {
    }


    public String getZipFileName() {
        return zipFileName;
    }

    public String getSingleFileName() {
        return singleFileName;
    }

    public FileProcessor(MetadataWriter metadataWriter, LuceneIndex luceneIndex, DiscoveryFile discoveryFile) {
        this.metadataWriter = metadataWriter;
        this.luceneIndex = luceneIndex;
        this.discoveryFile = discoveryFile;
    }

    protected boolean isPreview() {
        return Project.getCurrentProject().isPreview();
    }

    private boolean isPdf() {
        return Project.getCurrentProject().isCreatePDF();
    }

    protected void createImage(DiscoveryFile discoveryFile) {
        if (isPdf()) {
            OfficePrint.getInstance().createPdf(discoveryFile.getPath(), discoveryFile.getRealFileName());
        }
    }

    protected void createHtmlForDocument(DiscoveryFile discoveryFile) throws Exception {
        //first make sure the output directory is empty
        File outputDir = new File(getHtmlOutputDir());
        if (outputDir.exists()) {
            Util.deleteDirectory(outputDir);
        }

        outputDir.mkdirs();

        //convert using open office (special processing for eml files)
        String outputHtmlFileName = outputDir.getPath() + File.separator + discoveryFile.getPath().getName() + ".html";
        DocumentToHtml.getInstance().createHtml(discoveryFile, new File(outputHtmlFileName), discoveryFile.getRealFileName());
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
        return Settings.getSettings().getHTMLDir();
    }

    /**
     * Add the search result (Tika metadata) to Hadoop context as a map Key is
     * the MD5 of the file used to create map.
     *
     * @param metadata Metadata extracted from search.
     * @throws IOException          thrown on any IO problem.
     * @throws InterruptedException thrown by Hadoop processing.
     */
    @SuppressWarnings("all")
    protected void writeMetadata(DiscoveryFile discoveryFile, DocumentMetadata metadata)
            throws IOException, InterruptedException {
        MapWritable mapWritable = createMapWritable(metadata, discoveryFile);
        metadataWriter.processMap(mapWritable,discoveryFile);
    }

    /**
     * Create a map
     *
     * @param metadata Hadoop metadata to insert into map
     * @return Created map
     * @throws IOException
     */
    private MapWritable createMapWritable(Metadata metadata, DiscoveryFile discoveryFile) throws IOException {
        String fileName = discoveryFile.getPath().getPath();
        MapWritable mapWritable = new MapWritable();
        String[] names = metadata.names();
        for (String name : names) {
            mapWritable.put(new Text(name), new Text(metadata.get(name)));
        }
        byte[] bytes = Util.getFileContent(fileName);
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

    /**
     * Collect the html code
     *
     * @param mapWritable
     * @throws IOException
     */
    // TODO lots of room to improve html generation
    private void createMapWritableForHtml(MapWritable mapWritable) throws IOException {
        File htmlOutputDir = new File(getHtmlOutputDir());
        List<String> htmlFiles = new ArrayList<>();
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
    protected boolean isResponsive(Metadata metadata) throws IOException, ParseException {
        // set true if search finds a match
        /*boolean isResponsive;
        // get culling parameters
        String queryString = Project.getCurrentProject().getCullingAsTextBlock();
        // TODO parse important parameters to mappers and reducers individually, not globally
        IndexWriter writer = null;
        RAMDirectory directory = null;
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        directory = new RAMDirectory();
        // make a writer to create the index
        writer = new IndexWriter(directory, config);
        writer.addDocument(createDocument(metadata));
        // close the writer to finish building the index
        writer.close();

        //adding the build index to FS
        if (Project.getCurrentProject().isLuceneIndexEnabled() && luceneIndex != null) {
            luceneIndex.addToIndex(directory);
        }

        // TODO terrible!!! Side effect is putting file into ES
        // ESIndex.getInstance().addBatchData(metadata);

        if (queryString == null || queryString.trim().isEmpty()) {
            return true;
        }
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);
        QueryParser parser = new QueryParser("content", analyzer);
        String parsedQuery = parseQueryString(queryString);
        Query query = parser.parse(parsedQuery);
        TopDocs hits = isearcher.search(query, 1);
        isResponsive = hits.scoreDocs.length > 0;
        ireader.close();
        directory.close();
        return isResponsive;*/
        return true;
    }

    protected void addToES(Metadata metadata) {
        ESIndex.getInstance().addBatchData(metadata);
    }

    /**
     * Extracts document metadata. Text is part of it. Forensics information is
     * part of it.
     *
     * @return DocumentMetadata container receiving metadata.
     */
    protected void extractMetadata(DiscoveryFile discoveryFile, DocumentMetadata metadata) {
        DocumentParser.getInstance().parse(discoveryFile, metadata);
    }

    public String getOriginalDocumentPath(DiscoveryFile discoveryFile) {
        return discoveryFile.getRealFileName();
    }

    protected void extractJlFields(DiscoveryFile discoveryFile) {
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(discoveryFile.getPath(), "UTF-8");
            while (it.hasNext()) {
                DocumentMetadata metadata = new DocumentMetadata();
                String jsonAsString = it.nextLine();
                String htmlText = JsonParser.getJsonField(jsonAsString, "extracted_text");
                String text = Jsoup.parse(htmlText).text();
                // text metadata fields
                metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);
                metadata.setContentType("application/jl");
                // other necessary metadata fields
                metadata.setOriginalPath(getOriginalDocumentPath(discoveryFile));
                metadata.setHasAttachments(discoveryFile.isHasAttachments());
                metadata.setHasParent(discoveryFile.isHasParent());
                metadata.setCustodian(Project.getCurrentProject().getCurrentCustodian());
                writeMetadata(discoveryFile, metadata);
                ESIndex.getInstance().addBatchData(metadata);
            }
        } catch (Exception e) {
            LOGGER.error("Problem with JSON line", e);
        } finally {
            if (it != null) {
                it.close();
            }
        }
    }

    @Override
    public void run() {
        // update application log
        //LOGGER.trace("Processing file: {}", discoveryFile.getRealFileName());
        // set to true if file matches any query params
        boolean isResponsive = false;
        // exception message to place in output if error occurs
        String exceptionMessage = null;
        // ImageTextParser metadata, derived from Tika metadata class
        DocumentMetadata metadata = new DocumentMetadata();
        discoveryFile.setMetadata(metadata);
        String extension = Util.getExtension(discoveryFile.getRealFileName());

        if ("jl".equalsIgnoreCase(extension)) {
            extractJlFields(discoveryFile);
        }
        try {
            metadata.setOriginalPath(getOriginalDocumentPath(discoveryFile));
            metadata.setHasAttachments(discoveryFile.isHasAttachments());
            metadata.setHasParent(discoveryFile.isHasParent());
            // extract file contents with Tika
            // Tika metadata class contains references to metadata and file text
            // TODO discoveryFile has the pointer to the same metadata - simplify this
            extractMetadata(discoveryFile, metadata);

            /*
            if (project.isRemoveSystemFiles() && Util.isSystemFile(metadata)) {
                LOGGER.info("File {} is recognized as system file and is not processed further",
                        discoveryFile.getPath().getPath());
                return;
            }
            metadata.setCustodian(project.getCurrentCustodian());
            */
            // add Hash to metadata

            MD5Hash hash = Util.createKeyHash(discoveryFile.getPath(), metadata);
            metadata.setHash(hash.toString());
            metadata.acquireUniqueId();
            // search through Tika results using Lucene
            isResponsive = isResponsive(metadata);
            if (isResponsive) {
                addToES(metadata);
            }

        } catch (IOException | ParseException e) {
            // LOGGER.warn("Exception processing file ", e);
            exceptionMessage = e.getMessage();
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, exceptionMessage);
        }

        if (isResponsive || exceptionMessage != null) {
            createImage(discoveryFile);
            if (isPreview()) {
                try {
                    createHtmlForDocument(discoveryFile);
                } catch (Exception e) {
                    metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
                }
            }
            try {
                writeMetadata(discoveryFile, metadata);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Stats.getInstance().increaseItemCount(discoveryFile.getPath().length());


        //LOGGER.trace("Is the file responsive: {}", isResponsive);
    }
}
