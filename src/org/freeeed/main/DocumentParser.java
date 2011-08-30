package org.freeeed.main;

import java.io.FileInputStream;
import java.io.IOException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;

/**
 * This class is separate to have all Tika-related stuff in a one place
 * It may contain more parsing specifics later on
 */
public class DocumentParser {
    private static DocumentParser instance = new DocumentParser();
    private Tika tika;
    
    public static DocumentParser getInstance() {
        return instance;
    }    
    
    private DocumentParser() {
        tika = new Tika();
        tika.setMaxStringLength(10 * 1024 * 1024);
    }
    public void parse(String fileName, Metadata metadata) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(fileName);            
            String text = tika.parseToString(inputStream, metadata);
            metadata.set(DocumentMetadataKeys.DOCUMENT_TEXT, text);            
        } catch (IOException e) {
            // TODO deal with each exception in its own way
            // e.printStackTrace(System.out);
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
        } catch (TikaException e) {
            // TODO deal with each exception in its own way
            // e.printStackTrace(System.out);
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
        } catch (Exception e) {
            // the show must still go on
            // e.printStackTrace(System.out);
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
        } finally {
            if (inputStream != null) {
                //
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }
}
