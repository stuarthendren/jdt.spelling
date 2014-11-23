package jdt.spelling.checker;

import java.util.Locale;

import jdt.spelling.Preferences;
import jdt.spelling.dictionary.DictionaryFactory;
import jdt.spelling.dictionary.LocaleSensitiveSpellDictionary;
import jdt.spelling.dictionary.PersistentSpellDictionary;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;

public class CheckerFactory implements IPreferenceChangeListener {

	private final DictionaryFactory dictionaryFactory;

	private Checker checker;

	public CheckerFactory(DictionaryFactory dictionaryFactory) {
		this.dictionaryFactory = dictionaryFactory;
	}

	public synchronized final Checker getSpellChecker() throws IllegalStateException {
		Locale dictionaryLocale = Preferences.getDictionaryLocale();

		if (checker != null) {
			return checker;
		}

		PersistentSpellDictionary added = dictionaryFactory.createAdded();
		PersistentSpellDictionary ignored = dictionaryFactory.createIgnored();
		LocaleSensitiveSpellDictionary mainDictionary = dictionaryFactory.createDictionary(dictionaryLocale);
		LocaleSensitiveSpellDictionary codeWords = dictionaryFactory.createCodeWords(dictionaryLocale);

		checker = new Checker(added, ignored, mainDictionary, codeWords);
		checker.setCodeWordsStatus(Preferences.getCodeWordStatus());
		return checker;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		String key = event.getKey();
		if (Preferences.JDT_SPELLING_ADDITIONS_DICTIONARY.equals(key)) {
			getSpellChecker().setAdditionsDictionary(dictionaryFactory.createAdded());
		} else if (Preferences.JDT_SPELLING_IGNORE_DICTIONARY.equals(key)) {
			getSpellChecker().setIgnoreDictionary(dictionaryFactory.createIgnored());
		} else if (Preferences.JDT_SPELLING_CODE_WORD_STATUS.equals(key)) {
			getSpellChecker().setCodeWordsStatus(Preferences.getCodeWordStatus());
		} else if (Preferences.JDT_SPELLING_LOCALE_DICTIONARY.equals(key)) {
			getSpellChecker().setMainDictionary(dictionaryFactory.createDictionary(Preferences.getDictionaryLocale()));
			getSpellChecker().setCodeWordDictionary(
					dictionaryFactory.createCodeWords(Preferences.getDictionaryLocale()));
		}

	}
}
