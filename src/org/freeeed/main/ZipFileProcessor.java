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
	
	private String fileName;

	public ZipFileProcessor(String fileName) {
		this.fileName = fileName;
	}

	public void process()
			throws IOException {
		// unpack the zip file
		BufferedOutputStream dest = null;
		FileInputStream fis = new FileInputStream(fileName);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			System.out.println("Extracting: " + entry);
			int count;
			byte data[] = new byte[BUFFER];
			// write the files to the disk
			FileOutputStream fos = new FileOutputStream(entry.getName());
			dest = new BufferedOutputStream(fos, BUFFER);
			while ((count = zis.read(data, 0, BUFFER))
					!= -1) {
				dest.write(data, 0, count);
			}
			dest.flush();
			dest.close();
		}
		zis.close();
	}
}
