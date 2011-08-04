package org.freeeed.main;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.configuration.Configuration;
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
import org.freeeed.util.History;

/**
 * Opens the zip file, reads all entries, and processes them for eDiscovery
 */
public abstract class FileProcessor {

    private String zipFileName;
    private String singleFileName;
    private Context context;

    public FileProcessor(Context context) {
        this.context = context;
    }

    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }

    public void setSingleFileName(String singleFileName) {
        this.singleFileName = singleFileName;
    }

    abstract public void process() throws IOException, InterruptedException;

    public void processFileEntry(String tempFile, String originalFileName)
            throws IOException, InterruptedException {
        History.appendToHistory("Processing: " + originalFileName);
        boolean isResponsive = false;
        String exceptionMessage = null;
        Metadata metadata = new Metadata();
        try {
            // start collecting eDiscovery metadata
            metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, originalFileName);
            // extract text and metadata with Tika
            extractMetadata(tempFile, metadata);
            isResponsive = isResponsive(metadata);
        } catch (Exception e) {
            e.printStackTrace(System.out);
            exceptionMessage = e.getMessage();
        }
        if (exceptionMessage != null) {
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, exceptionMessage);
        }
        if (isResponsive || exceptionMessage != null) {
            emitAsMap(tempFile, metadata);
        }
        History.appendToHistory("Responsive: " + isResponsive);
    }

    @SuppressWarnings("unchecked")
    private void emitAsMap(String fileName, Metadata metadata) throws IOException, InterruptedException {
        MapWritable mapWritable = createMapWritable(metadata, fileName);
        MD5Hash key = MD5Hash.digest(new FileInputStream(fileName));
        context.write(key, mapWritable);
    }

    private MapWritable createMapWritable(Metadata metadata, String fileName) {
        MapWritable mapWritable = new MapWritable();
        String[] names = metadata.names();
        for (String name : names) {
            mapWritable.put(new Text(name), new Text(metadata.get(name)));
        }
        byte[] bytes = Util.getFileContent(fileName);
        mapWritable.put(new Text("native"), new BytesWritable(bytes));
        return mapWritable;
    }

    private boolean isResponsive(Metadata metadata) {
        Configuration configuration = FreeEedMain.getInstance().getProcessingParameters();
        if (!configuration.containsKey(ParameterProcessing.CULLING)) {
            return true;
        }

        String queryString = configuration.getString(ParameterProcessing.CULLING);
        boolean isResponsive = false;
        // TODO parse important parameters to mappers and reducers individually, not globally
        try {
            // Construct a RAMDirectory to hold the in-memory representation of the index.
            RAMDirectory idx = new RAMDirectory();

            // Make an writer to create the index
            IndexWriter writer =
                    new IndexWriter(idx, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);

            // Add some Document objects containing quotes
            String title = metadata.get(ParameterProcessing.TITLE);
            writer.addDocument(createDocument(title, metadata.get(DocumentMetadataKeys.DOCUMENT_TEXT)));
            // Optimize and close the writer to finish building the index
            writer.optimize();
            writer.close();

            // Build an IndexSearcher using the in-memory index
            Searcher searcher = new IndexSearcher(idx);
            isResponsive = search(searcher, queryString);
            searcher.close();
        } catch (Exception e) {
            // TODO handle this better
            // if anything happens - don't stop processing
            e.printStackTrace(System.out);
        }
        return isResponsive;
    }

    private static Document createDocument(String title, String content) {
        Document doc = new Document();
        doc.add(new Field(ParameterProcessing.TITLE, title.toLowerCase(), Field.Store.YES, Field.Index.ANALYZED));
        if (content != null) {
            doc.add(new Field(ParameterProcessing.CONTENT, content.toLowerCase(), Field.Store.NO, Field.Index.ANALYZED));
        }
        return doc;
    }

    private static boolean search(Searcher searcher, String queryString)
            throws ParseException, IOException {
        String parsedQuery = parseQueryString(queryString);
        QueryParser queryParser = new QueryParser(Version.LUCENE_30, parsedQuery,
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
     * @param tempFile
     * @return DocumentMetadata
     */
    private void extractMetadata(String tempFile, Metadata metadata) {
        FreeEedParser parser = new FreeEedParser();
        parser.parse(tempFile, metadata);
    }

    public String getZipFileName() {
        return zipFileName;
    }

    public String getSingleFileName() {
        return singleFileName;
    }

    public Context getContext() {
        return context;
    }
}
