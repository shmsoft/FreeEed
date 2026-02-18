package org.freeeed.mr;

import org.freeeed.main.DocumentMetadataKeys;
import org.freeeed.main.ParameterProcessing;
import org.freeeed.main.ZipFileWriter;
import org.freeeed.metadata.ColumnMetadata;
import org.freeeed.services.Project;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class MetadataWriterTest {

    private TestMetadataWriter metadataWriter;
    private MockZipFileWriter mockZipFileWriter;

    @Before
    public void setUp() {
        metadataWriter = new TestMetadataWriter();
        mockZipFileWriter = new MockZipFileWriter();
        metadataWriter.zipFileWriter = mockZipFileWriter;
        metadataWriter.columnMetadata = new ColumnMetadata();
        metadataWriter.columnMetadata.setFieldSeparator("|");
        metadataWriter.columnMetadata.setAllMetadata("standard");

        Project.getCurrentProject().setMetadataCollect("standard");
        Project.getCurrentProject().setFieldSeparator("|");
        Project.getCurrentProject().setCurrentCustodian("custodian");
    }

    @Test
    public void testProcessMapWithPdf() throws IOException, InterruptedException {
        Map<String, String> value = new HashMap<>();
        value.put(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, "/data/test.txt");
        value.put(DocumentMetadataKeys.DOCUMENT_TEXT, "test text");

        // Mock PDF content
        String pdfContent = "fake pdf content";
        String encodedPdf = Base64.getEncoder().encodeToString(pdfContent.getBytes());
        value.put(ParameterProcessing.NATIVE_AS_PDF, encodedPdf);

        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        org.freeeed.main.DiscoveryFile discoveryFile = new org.freeeed.main.DiscoveryFile(tempFile.getAbsolutePath(),
                "test.txt");

        metadataWriter.processMap(value, discoveryFile);

        // Verify PDF was added to zip
        assertTrue("PDF file should be added to images folder",
                mockZipFileWriter.lastEntryName != null &&
                        mockZipFileWriter.lastEntryName.startsWith("images/") &&
                        mockZipFileWriter.lastEntryName.endsWith(".pdf"));
    }

    static class MockZipFileWriter extends ZipFileWriter {
        public String lastEntryName;
        public byte[] lastContent;

        @Override
        public void addBinaryFile(String entryName, byte[] fileContent, int length) throws IOException {
            this.lastEntryName = entryName;
            this.lastContent = fileContent;
        }

        @Override
        public void addTextFile(String entryName, String textContent) throws IOException {
            // ignore
        }
    }

    static class TestMetadataWriter extends MetadataWriter {
        // Override to avoid file I/O and NPE
        @Override
        protected void appendMetadata(String string) throws IOException {
            // Do nothing
        }
    }
}
