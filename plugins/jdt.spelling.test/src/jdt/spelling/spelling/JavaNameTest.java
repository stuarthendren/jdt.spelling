package jdt.spelling.spelling;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import jdt.spelling.checker.JavaName;
import jdt.spelling.checker.JavaNameType;

import org.eclipse.jdt.core.IJavaElement;
import org.junit.Before;
import org.junit.Test;

public class JavaNameTest {

	private static final String name = "JAVA_NAME";
	private static final String[] split = new String[] { "JAVA", "NAME" };

	private IJavaElement element;

	private JavaNameType type;

	private JavaName javaName;

	@Before
	public void setUp() {
		element = mock(IJavaElement.class);
		type = mock(JavaNameType.class);
		javaName = new JavaName(type, element);
		when(element.getElementName()).thenReturn(name);
		when(type.getWords(name)).thenReturn(split);
		when(type.getSeparatorLength()).thenReturn(1);
	}

	@Test
	public void testGetElement() {
		assertEquals(element, javaName.getElement());
	}

	@Test
	public void testGetType() {
		assertEquals(type, javaName.getType());
	}

	@Test
	public void testGetWords() {
		assertArrayEquals(split, javaName.getWords());
	}

	@Test
	public void testGetName() {
		assertEquals(name, javaName.getName());
	}

	@Test
	public void testGetOffset() {
		assertEquals(0, javaName.getOffset(0));
		assertEquals(5, javaName.getOffset(1));
	}

}
