package org.freeeed.mr;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class WordCountTest extends TestCase {

    public void testMain() throws Exception {
        String [] args = { "test-data/mr/sonnet18.txt", "output/mr_output" };
        if (new File("output/mr_output").exists()) {
            FileUtils.deleteDirectory(new File("output/mr_output"));
        }
        WordCount.main(args);
    }
}