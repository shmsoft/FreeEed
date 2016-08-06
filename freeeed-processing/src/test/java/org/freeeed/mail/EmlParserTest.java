package org.freeeed.mail;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;

public class EmlParserTest {

	@Test
    public void testParseEmailReferences() throws Exception {
		EmlParser parser = new EmlParser(new File("test-data/02-loose-files/docs/eml/test_references.eml"));
		System.out.println(Arrays.toString(parser.getReferencedMessageIds()));
		assertArrayEquals(new String [] { "<348F04F142D69C21-291E56D292BC@xxxx.net>", "<473FF3B8.9020707@xxx.org>" }, parser.getReferencedMessageIds());
	}
	
}
