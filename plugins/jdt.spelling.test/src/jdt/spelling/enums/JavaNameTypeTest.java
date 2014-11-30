package jdt.spelling.enums;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class JavaNameTypeTest {

	@Test
	public void testDotWords() {
		assertArrayEquals(new String[] { "basic", "dot", "case" }, JavaNameType.DOT.getWords("basic.dot.case"));
		assertArrayEquals(new String[] { "single", "d", "case" }, JavaNameType.DOT.getWords("single.d.case"));
		assertArrayEquals(new String[] { "single", "number9", "dot", "case" },
				JavaNameType.DOT.getWords("single.number9.dot.case"));
		assertArrayEquals(new String[] { "multi", "number99987", "dot", "case" },
				JavaNameType.DOT.getWords("multi.number99987.dot.case"));
		assertArrayEquals(new String[] { "middle", "num22ber", "dot", "case" },
				JavaNameType.DOT.getWords("middle.num22ber.dot.case"));
	}

	@Test
	public void testLowerCamelCaseWords() {
		assertArrayEquals(new String[] { "basic", "Lower", "Case" },
				JavaNameType.LOWER_CAMEL_CASE.getWords("basicLowerCase"));
		assertArrayEquals(new String[] { "single", "L", "Case" }, JavaNameType.LOWER_CAMEL_CASE.getWords("singleLCase"));
		assertArrayEquals(new String[] { "multi", "L", "C", "C", "Case" },
				JavaNameType.LOWER_CAMEL_CASE.getWords("multiLCCCase"));
		assertArrayEquals(new String[] { "single", "Number", "9", "Case" },
				JavaNameType.LOWER_CAMEL_CASE.getWords("singleNumber9Case"));
		assertArrayEquals(new String[] { "multi", "Number", "8", "7", "9", "Case" },
				JavaNameType.LOWER_CAMEL_CASE.getWords("multiNumber879Case"));
		assertArrayEquals(new String[] { "m", "_", "this", "Is", "A", "Test" },
				JavaNameType.LOWER_CAMEL_CASE.getWords("m_thisIsATest"));
		assertArrayEquals(new String[] { "_", "this", "Is", "A", "Test" },
				JavaNameType.LOWER_CAMEL_CASE.getWords("_thisIsATest"));
		assertArrayEquals(new String[] { "$", "this", "Is", "A", "Test" },
				JavaNameType.LOWER_CAMEL_CASE.getWords("$thisIsATest"));
		assertArrayEquals(new String[] { "$", "_", "this", "Is", "A", "Test" },
				JavaNameType.LOWER_CAMEL_CASE.getWords("$_thisIsATest"));
	}

	@Test
	public void testUpperCamelCaseWords() {
		assertArrayEquals(new String[] { "Basic", "Upper", "Case" },
				JavaNameType.UPPER_CAMEL_CASE.getWords("BasicUpperCase"));
		assertArrayEquals(new String[] { "Single", "U", "Case" }, JavaNameType.LOWER_CAMEL_CASE.getWords("SingleUCase"));
		assertArrayEquals(new String[] { "Multi", "U", "C", "C", "Case" },
				JavaNameType.UPPER_CAMEL_CASE.getWords("MultiUCCCase"));
		assertArrayEquals(new String[] { "Single", "Number", "9", "Case" },
				JavaNameType.UPPER_CAMEL_CASE.getWords("SingleNumber9Case"));
		assertArrayEquals(new String[] { "Multi", "Number", "8", "7", "9", "Case" },
				JavaNameType.UPPER_CAMEL_CASE.getWords("MultiNumber879Case"));
		assertArrayEquals(new String[] { "Multi", "Number", "8", "7", "9", "Case" },
				JavaNameType.UPPER_CAMEL_CASE.getWords("MultiNumber879Case"));
		assertArrayEquals(new String[] { "f", "_", "This", "Is", "A", "Test" },
				JavaNameType.UPPER_CAMEL_CASE.getWords("f_ThisIsATest"));
		assertArrayEquals(new String[] { "$", "_", "This", "Is", "A", "Test" },
				JavaNameType.UPPER_CAMEL_CASE.getWords("$_ThisIsATest"));
	}

	@Test
	public void testUpperWords() {
		assertArrayEquals(new String[] { "BASIC", "UPPER", "CASE" }, JavaNameType.UPPER.getWords("BASIC_UPPER_CASE"));
		assertArrayEquals(new String[] { "SINGLE", "U", "CASE" }, JavaNameType.UPPER.getWords("SINGLE_U_CASE"));
		assertArrayEquals(new String[] { "SINGLE", "NUMBER9", "CASE" },
				JavaNameType.UPPER.getWords("SINGLE_NUMBER9_CASE"));
		assertArrayEquals(new String[] { "MULTI", "87NUMBER", "CASE" },
				JavaNameType.UPPER.getWords("MULTI_87NUMBER_CASE"));
		assertArrayEquals(new String[] { "MULTI", "87NUMBER", "CASE" },
				JavaNameType.UPPER.getWords("MULTI_87NUMBER_CASE"));
		assertArrayEquals(new String[] { "", "THIS", "IS", "A", "TEST" },
				JavaNameType.UPPER.getWords("_THIS_IS_A_TEST"));
		assertArrayEquals(new String[] { "", "", "THIS", "IS", "A", "TEST" },
				JavaNameType.UPPER.getWords("$_THIS_IS_A_TEST"));
	}

}
