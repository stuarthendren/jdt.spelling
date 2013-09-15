package jspell.dictionary;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class PersistentSpellDictionaryTest {

	private static final String TEST = "test";

	private File file;
	private PersistentSpellDictionary dictionary;

	@Before
	public void setUp() throws IOException {
		file = File.createTempFile(TEST, ".dic");
		file.deleteOnExit();

		dictionary = new PersistentSpellDictionary(file.toURI().toURL());
	}

	@After
	public void tearDown() throws IOException {
		file = null;
		dictionary = null;
	}

	@Test
	public void testAcceptsWords() {
		assertTrue(dictionary.acceptsWords());
	}

	@Test
	public void testAddWordString() {
		assertFalse(dictionary.isCorrect(TEST));
		dictionary.addWord(TEST);
		assertTrue(dictionary.isCorrect(TEST));
	}

	@Test
	public void testClear() {
		dictionary.addWord(TEST);
		dictionary.clear();
		assertFalse(dictionary.isCorrect(TEST));

	}

}
