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

        // Mock PDF content with URL-safe characters (padding with - or _ if possible,
        // or just using getUrlEncoder)
        String pdfContent = "fake pdf content with special chars ???";
        // We use getUrlEncoder to simulate what FileProcessor does.
        // To ensure we have characters that differ, we can try to encode something that
        // produces + or / in standard,
        // but - or _ in URL safe.
        // Standard: 62 -> +, 63 -> /
        // URL Safe: 62 -> -, 63 -> _
        // We need input bytes that result in indices 62 or 63.
        // 00111111 -> 63 -> / or _
        // byte: 0xFB (11111011) ? No. Base64 group is 6 bits.
        // Let's just use the encoder method to be sure.

        String encodedPdf = Base64.getUrlEncoder().encodeToString(new byte[] { (byte) 0xFB, (byte) 0xF0 });
        // standard decoder might fail on this if it generates - or _

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

    @Test
    public void testProcessMapWithoutPdf() throws IOException, InterruptedException {
        Map<String, String> value = new HashMap<>();
        value.put(DocumentMetadataKeys.DOCUMENT_ORIGINAL_PATH, "/data/test.txt");
        value.put(DocumentMetadataKeys.DOCUMENT_TEXT, "test text");

        // No PDF content

        File tempFile = File.createTempFile("test", ".txt");
        tempFile.deleteOnExit();
        org.freeeed.main.DiscoveryFile discoveryFile = new org.freeeed.main.DiscoveryFile(tempFile.getAbsolutePath(),
                "test.txt");

        metadataWriter.processMap(value, discoveryFile);

        // Should not crash and should not add PDF
        // (If it crashes, this test fails)
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
