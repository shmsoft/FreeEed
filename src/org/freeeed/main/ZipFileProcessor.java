package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.tika.metadata.Metadata;

public class ZipFileProcessor {

	private final int BUFFER = 4096;
	private String zipFileName;

	public ZipFileProcessor(String fileName) {
		this.zipFileName = fileName;
	}

	public void process()
			throws IOException {
		// unpack the zip file
		FileInputStream fileInputStream = new FileInputStream(zipFileName);
		ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			processZipEntry(zipInputStream, zipEntry);
		}
		zipInputStream.close();
	}

	private void processZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
		// write the file
		String tempFile = writeZipEntry(zipInputStream, zipEntry);		
		Metadata metadata = new Metadata();
		// start collecting eDiscovery metadata
		metadata.set(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, zipEntry.getName());
		// extract text and metadata with Tika
		extractMetadata(tempFile, metadata);		
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
}
