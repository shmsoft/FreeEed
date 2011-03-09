package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileProcessor {

	private final int BUFFER = 2048;
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
		System.out.println("Extracting: " + zipEntry);
		DocumentMetadata fileMetadata = new DocumentMetadata();
		fileMetadata.put(DocumentMetadata.ORIGINAL_FILE_PATH, zipEntry.toString());
		int count;
		byte data[] = new byte[BUFFER];
		// write the file to the disk
		String fileName = createTempFileName(zipEntry);
		FileOutputStream fileOutputStream = new FileOutputStream("/tmp/" + fileName);
		BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER);
		while ((count = zipInputStream.read(data, 0, BUFFER)) != -1) {
			bufferedOutputStream.write(data, 0, count);
		}
		bufferedOutputStream.flush();
		bufferedOutputStream.close();
	}
	private String createTempFileName(ZipEntry zipEntry) {
		String fileName = "temp." + getExtension(zipEntry.getName());
		return fileName;
	}
	private String getExtension(String fileName) {
		return "txt";
	}
}
