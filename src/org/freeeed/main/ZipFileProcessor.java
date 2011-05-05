package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
public class ZipFileProcessor {

	private final int BUFFER = 4096;
	private String zipFileName;
	private Context context;

	public ZipFileProcessor(String fileName, Context context) {
		this.zipFileName = fileName;
		this.context = context;
	}

	public void process()
			throws IOException {
		// unpack the zip file
		FileInputStream fileInputStream = new FileInputStream(zipFileName);
		ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			try {
				processZipEntry(zipInputStream, zipEntry);
			} catch (InterruptedException e) {
				// TODO - add better error handling
				e.printStackTrace(System.out);
			}
		}
		zipInputStream.close();
	}

	private void processZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException, InterruptedException {
		// write the file
		String tempFile = writeZipEntry(zipInputStream, zipEntry);
		Metadata metadata = new Metadata();
		// start collecting eDiscovery metadata
		metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, zipEntry.getName());
		// extract text and metadata with Tika
		extractMetadata(tempFile, metadata);
		boolean isResponsive = isResponsive(metadata);
		if (isResponsive) {
			emitAsMap(tempFile, metadata);
		}
	}

	private String writeZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
		System.out.println("Extracting: " + zipEntry);
		Metadata metadata = new Metadata();
		metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, zipEntry.toString());
		int count;
		byte data[] = new byte[BUFFER];
		// write the file to the disk
		String tempFileName = "/tmp/" + createTempFileName(zipEntry);
		FileOutputStream fileOutputStream = new FileOutputStream(tempFileName);
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER);
		while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
			bufferedOutputStream.write(data, 0, count);
		}
		bufferedOutputStream.flush();
		bufferedOutputStream.close();
		return tempFileName;
	}

	private String createTempFileName(ZipEntry zipEntry) {
		String fileName = "temp." + Util.getExtension(zipEntry.getName());
		return fileName;
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
		boolean isResponsive = false;
		// TODO parse important parameters to mappers and reducers individually, not globally
		try {
			Configuration configuration = FreeEedMain.getInstance().getProcessingParameters();
			if (!configuration.containsKey("cull")) {
				return true;
			} 
			String queryString = configuration.getString("cull"); 
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
}
