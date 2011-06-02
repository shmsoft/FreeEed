package org.freeeed.main;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.configuration.Configuration;
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
        Metadata metadata = new Metadata();
        // start collecting eDiscovery metadata
        metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, originalFileName);
        // extract text and metadata with Tika
        extractMetadata(tempFile, metadata);
        boolean isResponsive = isResponsive(metadata);
        if (isResponsive) {
            emitAsMap(tempFile, metadata);
        }
    }

    @SuppressWarnings("unchecked")
    private void emitAsMap(String fileName, Metadata metadata) throws IOException, InterruptedException {
        MapWritable mapWritable = createMapWritable(metadata);
        MD5Hash key = MD5Hash.digest(new FileInputStream(fileName));
        context.write(key, mapWritable);
    }

    private MapWritable createMapWritable(Metadata metadata) {
        MapWritable mapWritable = new MapWritable();
        String[] names = metadata.names();
        for (String name : names) {
            mapWritable.put(new Text(name), new Text(metadata.get(name)));
        }
        return mapWritable;
    }

    private boolean isResponsive(Metadata metadata) {
        Configuration configuration = FreeEedMain.getInstance().getProcessingParameters();
        if (!configuration.containsKey("cull")) {
            return true;
        }

        String queryString = configuration.getString("cull");
        boolean isResponsive = false;
        // TODO parse important parameters to mappers and reducers individually, not globally
        try {
            // Construct a RAMDirectory to hold the in-memory representation of the index.
            RAMDirectory idx = new RAMDirectory();

            // Make an writer to create the index
            IndexWriter writer =
                    new IndexWriter(idx, new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);

            // Add some Document objects containing quotes
            String title = ""; // TODO use doc id?
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
        doc.add(new Field("title", title, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("content", content, Field.Store.NO, Field.Index.ANALYZED));
        return doc;
    }

    private static boolean search(Searcher searcher, String queryString)
            throws ParseException, IOException {
        QueryParser queryParser = new QueryParser(Version.LUCENE_30, queryString,
                new StandardAnalyzer(Version.LUCENE_30));
        // Build a Query object
        Query query = queryParser.parse(queryString);
        // Search for the query
        TopDocs topDocs = searcher.search(query, 1);
        return topDocs.totalHits > 0;
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
