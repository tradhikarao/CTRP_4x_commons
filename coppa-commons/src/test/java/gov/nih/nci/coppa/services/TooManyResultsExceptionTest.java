package gov.nih.nci.coppa.services;

import static org.junit.Assert.*;

import org.junit.Test;

public class TooManyResultsExceptionTest {

	@Test
	public void testGetMaxResults() {
		assertEquals(1, new TooManyResultsException(1).getMaxResults());
	}

}
