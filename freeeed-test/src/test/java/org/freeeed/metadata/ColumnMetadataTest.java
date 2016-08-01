package org.freeeed.metadata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class ColumnMetadataTest {

	@Test
	public void testNoDuplicates() {
		IMetadataSource source = mock(IMetadataSource.class);
		when(source.getKeys()).thenReturn(Arrays.asList(new String[] { "1", "2", "3" }));
		when(source.getKeyValues("1")).thenReturn(new String[] { "subject", "time received" } );
		when(source.getKeyValues("2")).thenReturn(new String[] { "time received" } );
		when(source.getKeyValues("3")).thenReturn(new String[] { "date received", "time received" } );
		ColumnMetadata metadata = new ColumnMetadata(source);
		metadata.addMetadataValue("subject", "Test1");
		metadata.addMetadataValue("time received", "Test5");
		metadata.addMetadataValue("time received", "Test3");
		metadata.addMetadataValue("date received", "Test2");
		metadata.addMetadataValue("date received", "Test4");
		metadata.setFieldSeparator("|");
		Assert.assertEquals("subject|time received|date received", metadata.delimiterSeparatedHeaders());
		Assert.assertEquals("Test1|Test3|Test4", metadata.delimiterSeparatedValues());
	}
	
}
