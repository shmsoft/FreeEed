package org.freeeed.ocr;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by nehaojha on 03/01/18.
 */
public class TikaOCRTest {

    @Ignore("Time consuming parse")
    @Test
    public void testOcrTikaParsing00() throws Exception {
        //read contents from 00 pdf and compare with expected text
        String text = Document.parseContent("test-data/ocr/00.pdf");
        Assert.assertNotNull(text);
        Assert.assertFalse(text.isEmpty());
        double match = OCRUtil.compareText(text, OCRUtil.readFileContent("test-data/ocr/00.txt"));
        Assert.assertEquals(1.0, match, 0.1);
    }

    @Ignore("Time consuming parse")
    @Test
    public void testOcrTikaParsing01() throws Exception {
        //read contents from 01 pdf and compare with expected text
        String text = Document.parseContent("test-data/ocr/01.pdf");
        double match = OCRUtil.compareText(text, OCRUtil.readFileContent("test-data/ocr/01.txt"));
        Assert.assertNotNull(text);
        Assert.assertFalse(text.isEmpty());
        Assert.assertEquals(1.0, match, 0.1);
    }

    @Ignore("Time consuming parse")
    @Test
    public void testOcrTikaParsing516() throws Exception {
        //read contents from 01 pdf and compare with expected text
        String text = Document.parseContent("/Users/nehaojha/Documents/MyProjects/FreeEed/freeeed-processing/test-data/02-loose-files/docs/ocr/516.pdf");
        System.out.println("text = " + text);
    }
}
