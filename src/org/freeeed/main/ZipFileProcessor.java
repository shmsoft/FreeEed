package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
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
		emitAsMap(tempFile, metadata);
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
		String fileName = "temp." + getExtension(zipEntry.getName());
		return fileName;
	}

	private String getExtension(String fileName) {
		int dot = fileName.lastIndexOf(".");
		if (dot < 0) {
			return "";
		}
		String extension = fileName.substring(dot + 1);
		if (extension.length() > 10) {
			return "";
		}
		return extension;
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
		String [] names = metadata.names();
		for (String name: names) {			
			mapWritable.put(new Text(name), new Text(metadata.get(name)));
		}		
		return mapWritable;
	}
}
