package org.freeeed.main;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Scanner;

public class Util {

	private static final int BUFFER_SIZE = 4096;
	private static final String fEncoding = "UTF8";
	private static final String NL = System.getProperty("line.separator");

	public static String getExtension(String fileName) {
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

	public static byte[] getFileContent(String fileName) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(fileName));

			byte[] buffer = new byte[BUFFER_SIZE];
			int n = in.read(buffer, 0, BUFFER_SIZE);
			while (n >= 0) {
				out.write(buffer, 0, n);
				n = in.read(buffer, 0, BUFFER_SIZE);
			}
		} catch (Exception e) {
			// TODO better error handling
			e.printStackTrace(System.out);
		} finally { // always close input stream
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}
		return out.toByteArray();
	}

	/**	 
	 * @param fileName
	 * @return content of the file
	 * * (Credit: http://www.javapractices.com/topic/TopicAction.do?Id=42)
	 */
	public static String readTextFile(String fileName) throws Exception {
		StringBuilder text = new StringBuilder();		
		Scanner scanner = null;
		try {
			scanner = new Scanner(new FileInputStream(fileName), fEncoding);
			while (scanner.hasNextLine()) {
				text.append(scanner.nextLine()).append(NL);
			}
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return text.toString();
	}

	public static void writeTextFile(String fileName, String content) throws Exception {
		Writer out = new OutputStreamWriter(new FileOutputStream(fileName), fEncoding);
		try {
			out.write(content + NL);
		} finally {
			out.close();
		}
	}

	public static void appendToTextFile(String fileName, String content) throws Exception {
		Writer out = new OutputStreamWriter(new FileOutputStream(fileName, true), fEncoding);
		try {
			out.write(content + NL);
		} finally {
			out.close();
		}
	}
}
