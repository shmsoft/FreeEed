package org.freeeed.main;

import java.util.ArrayList;
import org.apache.tika.metadata.Metadata;

public class ColumnMetadata {

	private ArrayList<String> headers = new ArrayList<String>();
	private ArrayList<String> values = new ArrayList<String>();

	/**
	 * @return the headers
	 */
	public ArrayList<String> getHeaders() {
		return headers;
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(ArrayList<String> headers) {
		this.headers = headers;
	}

	/**
	 * @return the values
	 */
	public ArrayList<String> getValues() {
		return values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(ArrayList<String> values) {
		this.values = values;
	}

	public void addMetadataValue(String header, String value) {
		// if we have this header, put the value in the right place
		if (headers.contains(header)) {
			int index = headers.indexOf(header);
			values.set(index, value);

		} else { // if we don't have such a header, add it
			headers.add(header);
			values.add(value);
		}
	}
	public void addMetadata(Metadata metadata) {
		String [] names = metadata.names();
		for (String name: names) {
			if (!name.equalsIgnoreCase(DocumentMetadataKeys.DOCUMENT_TEXT)) {
				addMetadataValue(name, metadata.get(name));			
			}			
		}
	}
	public String tabSeparatedValues() {
		StringBuilder builder = new StringBuilder();
		for (String value: values) {
			builder.append("\"").append(value).append("\"").append("\t");
		}
		return builder.toString();
	}
	public String tabSeparatedHeaders() {
		StringBuilder builder = new StringBuilder();
		for (String header: headers) {
			builder.append(header).append("\t");
		}
		return builder.toString();
	}
}
