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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.tika.metadata.Metadata;
import org.freeeed.ai.ExtractPiiAws;
import org.freeeed.ai.ExtractPiiInabia;
import org.freeeed.ai.SummarizeText;
import org.freeeed.data.index.LuceneIndex;
import org.freeeed.data.index.SolrIndex;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import org.freeeed.html.DocumentToHtml;
import org.freeeed.mr.MetadataWriter;
import org.freeeed.ocr.OCRProcessor;
import org.freeeed.print.OfficePrint;
import org.freeeed.services.*;
import org.freeeed.util.LogFactory;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Opens the file, creates Lucene index and searches, then updates map
 */
public abstract class FileProcessor {

    private final static Logger LOGGER = LogFactory.getLogger(FileProcessor.class.getName());
    private final LuceneIndex luceneIndex;
    protected String zipFileName;
    protected String singleFileName;
    protected MetadataWriter metadataWriter;
    protected int docCount;
    private String hash;

    /**
     * Constructor
     *
     * @param metadataWriter
     * @param luceneIndex
     */
    public FileProcessor(MetadataWriter metadataWriter, LuceneIndex luceneIndex) {
        this.metadataWriter = metadataWriter;
        this.luceneIndex = luceneIndex;
    }

    /**
     * Create Apache Lucene document
     *
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
        doc.add(new TextField(ParameterProcessing.TITLE, title.toLowerCase(), Field.Store.YES));
        if (content != null) {
            doc.add(new TextField(ParameterProcessing.CONTENT, content.toLowerCase(), Field.Store.NO));
        }

        //add all metadata fields
        String[] metadataNames = metadata.names();
        for (String name : metadataNames) {
            String data = metadata.get(name);
            doc.add(new TextField(name, data.toLowerCase(), Field.Store.YES));
        }

        return doc;
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

    public String getZipFileName() {
        return zipFileName;
    }

    /**
     * Zip files are the initial file format passed to Hadoop map step
     *
     * @param zipFileName
     */
    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }

    public String getSingleFileName() {
        return singleFileName;
    }

    // TODO method not used in project
    public void setSingleFileName(String singleFileName) {
        this.singleFileName = singleFileName;
    }

    public LuceneIndex getLuceneIndex() {
        return luceneIndex;
    }

    /**
     * @param hasAttachments
     * @param hash
     * @throws IOException
     * @throws InterruptedException
     */
    abstract public void process(boolean hasAttachments, String hash) throws IOException, InterruptedException;

    /**
     * Add file to output
     *
     * @param discoveryFile object with info for processing discovery.
     * @throws IOException          on any IO problem.
     * @throws InterruptedException throws by Hadoop.
     */
    protected void processFileEntry(DiscoveryFile discoveryFile)
            throws IOException, InterruptedException {
        Project project = Project.getCurrentProject();

        if (project.isStopThePresses()) {
            return;
        }
        // update application log
        LOGGER.finer("Processing file: " + discoveryFile.getRealFileName());
        String exceptionMessage = null;
        // Document metadata, derived from Tika metadata class
        DocumentMetadata metadata = new DocumentMetadata();
        String extension = Util.getExtension(discoveryFile.getRealFileName());
        try {
            metadata.setOriginalPath(getOriginalDocumentPath(discoveryFile));
            metadata.setHasAttachments(discoveryFile.isHasAttachments());
            metadata.setHasParent(discoveryFile.isHasParent());
            // extract file contents with Tika
            // Tika metadata class contains references to metadata and file text
            extractMetadata(discoveryFile, metadata);
            if (project.isRemoveSystemFiles() && Util.isSystemFile(metadata)) {
                LOGGER.finer("File is recognized as system file and is not processed further: " +
                        discoveryFile.getPath().getPath());
                return;
            }
            metadata.setCustodian(project.getCurrentCustodian());
            // add Hash to metadata
            hash = Util.createKeyHash(discoveryFile.getPath(), metadata);
            metadata.setHash(hash.toString());
            metadata.acquireUniqueId();
            enrichMetadata(metadata);
            addToSolr(metadata);

        } catch (IOException e) {
            LOGGER.warning("Exception processing file");
            exceptionMessage = e.getMessage();
        }
        // update exception message if error
        if (exceptionMessage != null) {
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, exceptionMessage);
        }
        if (exceptionMessage != null) {
            createImage(discoveryFile);
            if (isPreview()) {
                try {
                    createHtmlForDocument(discoveryFile);
                } catch (Exception e) {
                    metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
                }
            }
            writeMetadata(discoveryFile, metadata);
        }
    }

    private boolean isPreview() {
        return Project.getCurrentProject().isPreview();
    }

    private boolean isPdf() {
        return Project.getCurrentProject().isCreatePDF();
    }

    private void createImage(DiscoveryFile discoveryFile) {
        if (isPdf()) {
            OfficePrint.getInstance().createPdf(discoveryFile.getPath(), discoveryFile.getRealFileName());
        }
    }

    private void createHtmlForDocument(DiscoveryFile discoveryFile) throws Exception {
        //first make sure the output directory is empty
        File outputDir = new File(getHtmlOutputDir());
        if (outputDir.exists()) {
            Util.deleteDirectory(outputDir);
        }

        outputDir.mkdirs();

        //convert using open office (special processing for eml files)
        String outputHtmlFileName = outputDir.getPath() + File.separator + discoveryFile.getPath().getName() + ".html";
        DocumentToHtml.getInstance().createHtml(discoveryFile.getPath(), new File(outputHtmlFileName), discoveryFile.getRealFileName());
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
    private void writeMetadata(DiscoveryFile discoveryFile, DocumentMetadata metadata)
            throws IOException, InterruptedException {
        Map<String, String> mapWritable = createMapWritable(metadata, discoveryFile);
        metadataWriter.processMap(mapWritable);
        Stats.getInstance().increaseItemCount();
    }

    /**
     * Create a map
     *
     * @param metadata Hadoop metadata to insert into map
     * @param discoveryFile File currently in process
     * @return Created map
     * @throws IOException
     */
    private Map<String, String> createMapWritable(Metadata metadata, DiscoveryFile discoveryFile) throws IOException {
        String fileName = discoveryFile.getPath().getPath();
        Map<String, String> mapWritable = new HashMap<>();
        String[] names = metadata.names();
        for (String name : names) {
            mapWritable.put(name, metadata.get(name));
        }
        byte[] bytes = Util.getFileContent(fileName);
        mapWritable.put(ParameterProcessing.NATIVE, Base64.getUrlEncoder().encodeToString(bytes));

        if (isPdf()) {
            String pdfFileName = fileName + ".pdf";
            if (new File(pdfFileName).exists()) {
                byte[] pdfBytes = Util.getFileContent(pdfFileName);
                mapWritable.put(ParameterProcessing.NATIVE_AS_PDF, Base64.getUrlEncoder().encodeToString(pdfBytes));
            }
        }

        createMapWritableForHtml(mapWritable, discoveryFile);

        return mapWritable;
    }

    /**
     * Collect the html code
     *
     * @param mapWritable
     * @throws IOException
     */
    // TODO lots of room to improve html generation
    private void createMapWritableForHtml(Map<String, String> mapWritable, DiscoveryFile discoveryFile) throws IOException {
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
                        mapWritable.put(ParameterProcessing.NATIVE_AS_HTML_NAME, Base64.getUrlEncoder().encodeToString(htmlBytes));
                    } else {
                        byte[] htmlBytes = Util.getFileContent(htmlFileName);
                        String key = ParameterProcessing.NATIVE_AS_HTML + "_" + file;
                        mapWritable.put(key, Base64.getUrlEncoder().encodeToString(htmlBytes));

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

            mapWritable.put(ParameterProcessing.NATIVE_AS_HTML, sb.toString());
        }
    }

    /**
     * Search metadata and file contents
     *
     * @param metadata
     * @return true if match is found else false
     */
//    private boolean isResponsive(Metadata metadata) throws IOException, ParseException {
//        // set true if search finds a match
//        boolean isResponsive = false;
//        // get culling parameters
//        String queryString = Project.getCurrentProject().getCullingAsTextBlock();
//        // TODO parse important parameters to mappers and reducers individually, not globally
//        IndexWriter writer = null;
//        ByteBuffersDirectory directory = null;
//        Analyzer analyzer = new StandardAnalyzer();
//        IndexWriterConfig config = new IndexWriterConfig(analyzer);
//
//        directory = new ByteBuffersDirectory();
//        // make a writer to create the index
//        writer = new IndexWriter(directory, config);
//        writer.addDocument(createDocument(metadata));
//        // close the writer to finish building the index
//        writer.close();
//
//        // TODO terrible!!! Side effect is putting file into Solr
//        // SolrIndex.getInstance().addBatchData(metadata);
//
//        if (queryString == null || queryString.trim().isEmpty()) {
//            return true;
//        }
//        DirectoryReader ireader = DirectoryReader.open(directory);
//        IndexSearcher isearcher = new IndexSearcher(ireader);
//        QueryParser parser = new QueryParser("content", analyzer);
//        String parsedQuery = parseQueryString(queryString);
//        Query query = parser.parse(parsedQuery);
//        TopDocs hits = isearcher.search(query, 1);
//        isResponsive = hits.scoreDocs.length > 0;
//        ireader.close();
//        directory.close();
//        return isResponsive;
//    }

    private void addToSolr(Metadata metadata) {
        SolrIndex.getInstance().addBatchData(metadata);
    }

    /**
     * Extracts document metadata. Text is part of it. Forensics information is
     * part of it.
     *
     * @return DocumentMetadata container receiving metadata.
     */
    private void extractMetadata(DiscoveryFile discoveryFile, DocumentMetadata metadata) {
        DocumentParser.getInstance().parse(discoveryFile, metadata);

        //OCR processing
        if (Project.getCurrentProject().isOcrEnabled()) {
            OCRProcessor ocrProcessor = OCRProcessor.createProcessor(Settings.getSettings().getOCRDir());
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

//    private void extractJlFields(DiscoveryFile discoveryFile) {
//        LineIterator it = null;
//        try {
//            it = FileUtils.lineIterator(discoveryFile.getPath(), "UTF-8");
//            while (it.hasNext()) {
//                DocumentMetadata metadata = new DocumentMetadata();
//                String jsonAsString = it.nextLine();
//                String htmlText = JsonParser.getJsonField(jsonAsString, "extracted_text");
//                String text = Jsoup.parse(htmlText).text();
//                // text metadata fields
//                metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);
//                metadata.setContentType("application/jl");
//                // other necessary metadata fields
//                metadata.setOriginalPath(getOriginalDocumentPath(discoveryFile));
//                metadata.setHasAttachments(discoveryFile.isHasAttachments());
//                metadata.setHasParent(discoveryFile.isHasParent());
//                metadata.setCustodian(Project.getCurrentProject().getCurrentCustodian());
//                writeMetadata(discoveryFile, metadata);
//                SolrIndex.getInstance().addBatchData(metadata);
//            }
//        } catch (Exception e) {
//            LOGGER.error("Problem with JSON line", e);
//        } finally {
//            it.close();
//        }
//    }

    private void enrichMetadata(DocumentMetadata metadata) {
        addPii(metadata);
        addSummary(metadata);
    }
    private void addPii(DocumentMetadata metadata) {
        // Extract PII if required
        Project project = Project.getCurrentProject();
        String documentText = metadata.getDocumentText();
        if (project.isPiiActive() &&
                Stats.getInstance().getPiiDocumentsProcessed() < project.getPiiLimit()) {
            String extractedPii = "";
            if (project.isPiiInabia()) {
                String piiToken = project.getPiiToken();
                ExtractPiiInabia extractor = new ExtractPiiInabia(piiToken);
                extractedPii = extractor.extractPiiAsString(documentText);
            } else {
                String key = Settings.getSettings().getAccessKeyId();
                String secretKey = Settings.getSettings().getSecretAccessKey();
                //new ExtractPiiAwsCLI().extractPiiAsString(documentText);
                extractedPii = new ExtractPiiAws(key, secretKey).extractPIIBySegment(documentText).toString();
            }
            if (!extractedPii.isEmpty()) {
                metadata.addField(DocumentMetadataKeys.EXTRACTED_PII, extractedPii);
                Stats.getInstance().incrementPiiDocsFound();
            }
            Stats.getInstance().incrementPiiDocs();
            // TODO make char count more precise
            Stats.getInstance().incrementPiiCharUnit();
        }
    }
    private void addSummary(DocumentMetadata metadata) {
        // Add summary if required
        Project project = Project.getCurrentProject();
        String documentText = metadata.getDocumentText();
        if (project.isSummarizeActive() &&
                Stats.getInstance().getSummaryDocumentsProcessed() < project.getSummarizeLimit()) {
            String summary = "";
            SummarizeText summarizer = new SummarizeText();
            summary = summarizer.summarizeText(documentText, project.getSummarizeModel());
            if (!summary.isEmpty()) {
                metadata.addField(DocumentMetadataKeys.SUMMARY, summary);
            }
            Stats.getInstance().incrementSummaryDocumentProcessed();
        }
    }
}
