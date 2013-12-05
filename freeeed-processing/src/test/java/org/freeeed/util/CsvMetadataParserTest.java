package org.freeeed.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CsvMetadataParserTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void testParseLines() {
        String header = "UPI,File Name,From,To";
        String line1 = "1,TestFile.eml,john@acme.com,smith@acme.com";
        
        List<String> lines = new ArrayList<>();
        lines.add(header);
        lines.add(line1);
        
        CsvMetadataParser parser = new CsvMetadataParser(",");
        
        Map<String, Map<String, String>> data = parser.parseLines(lines);
        Map<String, String> row = data.get("TestFile.eml");
        
        assertNotNull(row);
        
        assertEquals("1", row.get("UPI"));
        assertEquals("TestFile.eml", row.get("File Name"));
        assertEquals("john@acme.com", row.get("From"));
        assertEquals("smith@acme.com", row.get("To"));
    }
}
