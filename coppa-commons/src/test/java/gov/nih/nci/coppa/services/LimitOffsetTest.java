package gov.nih.nci.coppa.services;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class LimitOffsetTest {

	@Test
	public void structor() {
		LimitOffset limitOffset = new LimitOffset(1, 2);
		assertEquals(1, limitOffset.getLimit());
		assertEquals(2, limitOffset.getOffset());
	}
	
	@Test
	public void next() {
		LimitOffset limitOffset = new LimitOffset(1, 2);
		limitOffset.next();
		assertEquals(3, limitOffset.getOffset());
	}
	
	@Test
	public void prev() {
		LimitOffset limitOffset = new LimitOffset(1, 2);
		limitOffset.previous();
		assertEquals(1, limitOffset.getOffset());
	}
	
}
