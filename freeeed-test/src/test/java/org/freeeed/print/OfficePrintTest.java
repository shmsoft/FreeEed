package org.freeeed.print;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OfficePrintTest {
    private static final Logger logger = LoggerFactory.getLogger(OfficePrintTest.class);

    @Test
    public void testInit() {
        OfficePrint instance = OfficePrint.getInstance();
        // just getting the instance should not initialize office as yet

    }

}