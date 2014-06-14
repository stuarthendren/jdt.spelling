package jdt.spelling.checker;

import java.util.Locale;

import jdt.spelling.Preferences;
import jdt.spelling.dictionary.DictionaryFactory;
import jdt.spelling.dictionary.PersistentSpellDictionary;

import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.internal.ui.text.spelling.engine.LocaleSensitiveSpellDictionary;

@SuppressWarnings("restriction")
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

		checker = new Checker(added, ignored, mainDictionary);

		return checker;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		String key = event.getKey();
		if (Preferences.JDT_SPELLING_ADDITIONS_DICTIONARY.equals(key)) {
			getSpellChecker().setAdditionsDictionary(dictionaryFactory.createAdded());
		} else if (Preferences.JDT_SPELLING_IGNORE_DICTIONARY.equals(key)) {
			getSpellChecker().setIgnoreDictionary(dictionaryFactory.createIgnored());
		} else if (Preferences.JDT_SPELLING_LOCALE_DICTIONARY.equals(key)) {
			getSpellChecker().setMainDictionary(dictionaryFactory.createDictionary(Preferences.getDictionaryLocale()));
		}

	}
}
