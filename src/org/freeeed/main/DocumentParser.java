package org.freeeed.main;

import java.io.File;
import java.io.IOException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
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
        TikaInputStream inputStream = null;
        try {                     
            // the given input stream is closed by the parseToString method (see Tika documentation)
            // we will close it just in case :)            
            inputStream = TikaInputStream.get(new File(fileName));
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
                try {
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
            }
        }
    }
    public static void main(String [] argv) {
        String fileName = "test-data/01-one-time-test/215.eml";
        Metadata metadata = new Metadata();
        getInstance().parse(fileName, metadata);
        System.out.println(metadata);                
    }
}
