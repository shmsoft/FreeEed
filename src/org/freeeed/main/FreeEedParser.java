package org.freeeed.main;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;

/**
 * This class is separate to have all Tika-related stuff in a separate place
 * It may contain more parsing specifics later on
 */
public class FreeEedParser {

	public void parse(String fileName, Metadata metadata) {		 		
		try {
			FileInputStream inputStream = new FileInputStream(fileName);			
			Tika tika = new Tika();
			String text = tika.parseToString(inputStream, metadata);
			metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);			
		} catch (IOException e) {
			// TODO deal with each exception in its own way
			e.printStackTrace(System.out);
			metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
		} catch (TikaException e) {
			// TODO deal with each exception in its own way
			e.printStackTrace(System.out);
			metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
		} catch (Exception e) {
			// the show must still go on
			e.printStackTrace(System.out);
			metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
		} 
	}
}
