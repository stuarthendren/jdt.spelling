package jdt.spelling.dictionary;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import jdt.spelling.Preferences;

import org.junit.Before;
import org.junit.Test;

public class DictionaryFactoryTest {

	private DictionaryFactory dictionaryFactory;

	@Before
	public void setup() throws IOException {
		dictionaryFactory = new DictionaryFactory();
	}

	@Test
	public void ensureDefaultLocaleHasADictionary() {
		assertNotNull(dictionaryFactory.createDictionary(Preferences.getDictionaryLocale()));
	}

	@Test
	public void ensureAddedDictionaryIsCreated() {
		assertNotNull(dictionaryFactory.createAdded());
	}

	@Test
	public void ensureIgnoredDictionaryIsCreated() {
		assertNotNull(dictionaryFactory.createIgnored());
	}

}
