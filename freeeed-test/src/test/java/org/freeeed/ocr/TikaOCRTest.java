package org.freeeed.ocr;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by nehaojha on 03/01/18.
 */
public class TikaOCRTest {

    @Test
    public void testOcrTikaParsing00() throws Exception {
        //read contents from 00 pdf and compare with expected text
        String text = ImageTextParser.parseContent("../test-data/ocr/00.pdf");
        Assert.assertNotNull(text);
        Assert.assertFalse(text.isEmpty());
        double match = OCRUtil.compareText(text, OCRUtil.readFileContent("../test-data/ocr/00.txt"));
        Assert.assertEquals(1.0, match, 0.1);
    }

    @Test
    public void testOcrTikaParsing01() throws Exception {
        //read contents from 01 pdf and compare with expected text
        String text = ImageTextParser.parseContent("../test-data/ocr/01.pdf");
        double match = OCRUtil.compareText(text, OCRUtil.readFileContent("../test-data/ocr/01.txt"));
        Assert.assertNotNull(text);
        Assert.assertFalse(text.isEmpty());
        Assert.assertEquals(1.0, match, 0.1);
    }

    @Test
    public void testOcrTikaParsing516() throws Exception {
        //read contents from 516 pdf and compare with expected text
        String text = ImageTextParser.parseContent("../test-data/ocr/516.pdf");
    }
}
