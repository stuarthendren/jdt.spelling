package jspell.spelling;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CaseTest {

	@Test
	public void testLower() {
		assertEquals("lower", Case.LOWER.convert("LOWER"));
		assertEquals("lower", Case.LOWER.convert("lOWER"));
		assertEquals("lower", Case.LOWER.convert("LoWER"));
		assertEquals("lower", Case.LOWER.convert("LOwer"));
	}

	@Test
	public void testUpper() {
		assertEquals("UPPER", Case.UPPER.convert("upper"));
		assertEquals("UPPER", Case.UPPER.convert("Upper"));
		assertEquals("UPPER", Case.UPPER.convert("uPper"));
		assertEquals("UPPER", Case.UPPER.convert("upPER"));
	}

	@Test
	public void testTitle() {
		assertEquals("Title", Case.TITLE.convert("title"));
		assertEquals("Title", Case.TITLE.convert("TITLE"));
		assertEquals("Title", Case.TITLE.convert("tITLe"));
		assertEquals("Title", Case.TITLE.convert("TitlE"));
	}

}
