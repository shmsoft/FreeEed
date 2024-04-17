package org.freeeed.print;

import org.freeeed.main.FreeEedMain;
import org.freeeed.util.LogFactory;
import org.junit.Test;

import static org.junit.Assert.*;

public class OfficePrintTest {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(OfficePrintTest.class.getName());

    @Test
    public void testInit() {
        OfficePrint instance = OfficePrint.getInstance();
        // just getting the instance should not initialize office as yet

    }

}