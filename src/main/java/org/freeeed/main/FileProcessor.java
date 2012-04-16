package org.freeeed.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.metadata.Metadata;
import org.freeeed.main.PlatformUtil.PLATFORM;
import org.freeeed.print.OfficePrint;
import org.freeeed.services.FreeEedUtil;
import org.freeeed.services.History;
import org.freeeed.services.Project;
import org.freeeed.services.Stats;

/**
 * Opens the file, creates Lucene index and searches, then updates Hadoop map
 */
public abstract class FileProcessor {

    private String zipFileName;
    private String singleFileName;
    private Context context;            // Hadoop processing result context
    protected int docCount;

    public String getZipFileName() {
        return zipFileName;
    }

    public String getSingleFileName() {
        return singleFileName;
    }

    public Context getContext() {
        return context;
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
    public FileProcessor(Context context) {
        this.context = context;
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
        if (project.checkSkip()) {
            return;
        }
        // update application log
        History.appendToHistory("FileProcess.processFileEntry: " + originalFileName);
        // set to true if file matches any query params
        boolean isResponsive = false;
        // exception message to place in output if error occurs
        String exceptionMessage = null;
        // Tika metadata class
        Metadata metadata = new Metadata();
        try {
            metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH,
                    getOriginalDocumentPath(tempFile, originalFileName));
            // extract file contents with Tika
            // Tika metadata class contains references to metadata and file text
            extractMetadata(tempFile, metadata);
            if (project.isRemoveSystemFiles() && FreeEedUtil.isSystemFile(metadata)) {
                // TODO should we log denisting?
                return;
            }
            metadata.set(DocumentMetadataKeys.CUSTODIAN, project.getCurrentCustodian());
            // search through Tika results using Lucene
            isResponsive = isResponsive(metadata);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            History.appendToHistory("Exception: " + e.getMessage());
            exceptionMessage = e.getMessage();
        }
        // update exception message if error
        if (exceptionMessage != null) {
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, exceptionMessage);
        }        
        if (isResponsive || exceptionMessage != null) {
            createImage(tempFile, metadata);
            emitAsMap(tempFile, metadata);
        }
        History.appendToHistory("Responsive: " + isResponsive);
    }

    private boolean isPdf() {
        return Project.getProject().isCreatePDF();
    }

    private void createImage(String fileName, Metadata metadata) {
        if (isPdf()) {
            OfficePrint.createPdf(fileName, fileName + ".pdf");
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
    private void emitAsMap(String fileName, Metadata metadata) throws IOException, InterruptedException {
        MapWritable mapWritable = createMapWritable(metadata, fileName);
        // use MD5 of the input file as Hadoop key        
        FileInputStream fileInputStream = new FileInputStream(fileName);
        MD5Hash key = MD5Hash.digest(fileInputStream);
        fileInputStream.close();
        // emit map
        if ((PlatformUtil.getPlatform() == PLATFORM.LINUX) || (PlatformUtil.getPlatform() == PLATFORM.MACOSX)) {
            context.write(key, mapWritable);
        } else if (PlatformUtil.getPlatform() == PLATFORM.WINDOWS) {
            ArrayList<MapWritable> values = new ArrayList<MapWritable>();
            values.add(mapWritable);
            WindowsReduce.getInstance().reduce(key, values, null);
        }
        // update stats
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
        byte[] bytes = new File(fileName).length() < ParameterProcessing.ONE_GIG ? 
                FreeEedUtil.getFileContent(fileName) :
                "File too large".getBytes();
        mapWritable.put(new Text(ParameterProcessing.NATIVE), new BytesWritable(bytes));

        if (isPdf()) {
            String pdfFileName = fileName + ".pdf";
            if (new File(pdfFileName).exists()) {
                byte[] pdfBytes = FreeEedUtil.getFileContent(pdfFileName);
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
        
        if (queryString == null || queryString.trim().isEmpty()) {
            return true;
        }
        // TODO parse important parameters to mappers and reducers individually, not globally
        IndexWriter writer = null;
        RAMDirectory idx = null;
        try {
            // construct a RAMDirectory to hold the in-memory representation of the index.
            idx = new RAMDirectory();

            // make a writer to create the index
            writer = new IndexWriter(idx, new StandardAnalyzer(Version.LUCENE_30),
                    true, IndexWriter.MaxFieldLength.UNLIMITED);

            // add some Document objects containing quotes
            String title = metadata.get(ParameterProcessing.TITLE);
            // TODO - where is my title?
            if (title == null) {
                title = "";
            }
            writer.addDocument(createDocument(title, metadata.get(DocumentMetadataKeys.DOCUMENT_TEXT)));

            // optimize and close the writer to finish building the index
            writer.optimize();
            writer.close();

            // build an IndexSearcher using the in-memory index
            Searcher searcher = new IndexSearcher(idx);
            // search directory
            isResponsive = search(searcher, queryString);

            searcher.close();
        } catch (Exception e) {
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
    private static Document createDocument(String title, String content) {
        Document doc = new Document();
        doc.add(new Field(ParameterProcessing.TITLE, title.toLowerCase(), Field.Store.YES, Field.Index.ANALYZED));
        if (content != null) {
            doc.add(new Field(ParameterProcessing.CONTENT, content.toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
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
    private static boolean search(Searcher searcher, String queryString)
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
    private void extractMetadata(String tempFile, Metadata metadata) {
        DocumentParser.getInstance().parse(tempFile, metadata);
        //System.out.println(Util.toString(metadata));
    }

    abstract String getOriginalDocumentPath(String tempFile, String originalFileName);
}
