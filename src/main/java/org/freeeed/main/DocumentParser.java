package org.freeeed.main;

import java.io.File;
import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.freeeed.services.History;

/**
 * This class is separate to have all Tika-related stuff in a one place It may
 * contain more parsing specifics later on
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
        } catch (Exception e) {
            // the show must still go on
            History.appendToHistory("Exception: " + e.getMessage());
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, e.getMessage());
        } catch (OutOfMemoryError m) {
            History.appendToHistory("Memory Exception: " + m.getMessage());
            metadata.set(DocumentMetadataKeys.PROCESSING_EXCEPTION, m.getMessage());            
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

    public static void main(String[] argv) {
        String fileName = "test-data/01-one-time-test/215.eml";
        Metadata metadata = new Metadata();
        getInstance().parse(fileName, metadata);
        System.out.println(metadata);
    }
}
