package jspell.spelling;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jspell.JSpellPlugin;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellDictionary;
import org.eclipse.jdt.internal.ui.text.spelling.engine.LocaleSensitiveSpellDictionary;
import org.eclipse.jdt.internal.ui.text.spelling.engine.PersistentSpellDictionary;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

public class JSpellCheckerFactory {

	private static final String DICTIONARY_LOCATION = "dictionaries";

	private final Set<ISpellDictionary> dictionaries;

	private Map<Locale, ISpellDictionary> localeDictionaries;

	private PersistentSpellDictionary userDictionary;

	private JSpellChecker checker;

	public JSpellCheckerFactory() {

		dictionaries = new HashSet<ISpellDictionary>();
		// dictionaries.add(new TaskTagDictionary());

		try {

			Locale locale = null;
			final Enumeration<URL> locations = getDictionaryLocations();

			localeDictionaries = new HashMap<Locale, ISpellDictionary>();

			while (locations != null && locations.hasMoreElements()) {
				URL location = locations.nextElement();

				for (final Iterator<Locale> iterator = getLocalesWithInstalledDictionaries(location).iterator(); iterator
						.hasNext();) {

					locale = iterator.next();
					localeDictionaries.put(locale, new LocaleSensitiveSpellDictionary(locale, location));
				}
			}

		} catch (IOException exception) {
			JSpellPlugin.log(exception);
		}
	}

	public synchronized final JSpellChecker getSpellChecker() throws IllegalStateException {
		if (dictionaries == null) {
			throw new IllegalStateException("spell checker has been shut down");
		}

		IPreferenceStore store = JSpellPlugin.getDefault().getPreferenceStore();
		Locale locale = getCurrentLocale(store);
		if (userDictionary == null && "".equals(locale.toString())) {
			return null;
		}

		if (checker != null && checker.getLocale().equals(locale)) {
			return checker;
		}

		resetSpellChecker();

		checker = new JSpellChecker(store, locale);
		resetUserDictionary();

		for (Iterator<ISpellDictionary> iterator = dictionaries.iterator(); iterator.hasNext();) {
			ISpellDictionary dictionary = iterator.next();
			checker.addDictionary(dictionary);
		}

		ISpellDictionary dictionary = findDictionary(checker.getLocale());
		if (dictionary != null) {
			checker.addDictionary(dictionary);
		}

		return checker;
	}

	public ISpellDictionary findDictionary(Locale locale) {
		ISpellDictionary dictionary = localeDictionaries.get(locale);
		if (dictionary != null) {
			return dictionary;
		}

		// Try same language
		String language = locale.getLanguage();
		Iterator<Entry<Locale, ISpellDictionary>> iter = localeDictionaries.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Locale, ISpellDictionary> entry = iter.next();
			Locale dictLocale = entry.getKey();
			if (dictLocale.getLanguage().equals(language)) {
				return entry.getValue();
			}
		}

		return null;
	}

	/**
	 * Returns the locales for which this spell check engine has dictionaries in certain location.
	 * 
	 * @param location
	 *            dictionaries location
	 * @return The available locales for this engine
	 */
	private static Set<Locale> getLocalesWithInstalledDictionaries(URL location) {
		String[] fileNames;
		try {
			URL url = FileLocator.toFileURL(location);
			File file = new File(url.getFile());
			if (!file.isDirectory()) {
				return Collections.emptySet();
			}
			fileNames = file.list();
			if (fileNames == null) {
				return Collections.emptySet();
			}
		} catch (IOException ex) {
			JSpellPlugin.log(ex);
			return Collections.emptySet();
		}

		Set<Locale> localesWithInstalledDictionaries = new HashSet<Locale>();
		int fileNameCount = fileNames.length;
		for (int i = 0; i < fileNameCount; i++) {
			String fileName = fileNames[i];
			int localeEnd = fileName.indexOf(".dictionary"); //$NON-NLS-1$
			if (localeEnd > 1) {
				String localeName = fileName.substring(0, localeEnd);
				int languageEnd = localeName.indexOf('_');
				if (languageEnd == -1) {
					localesWithInstalledDictionaries.add(new Locale(localeName));
				} else if (languageEnd == 2 && localeName.length() == 5) {
					localesWithInstalledDictionaries
							.add(new Locale(localeName.substring(0, 2), localeName.substring(3)));
				} else if (localeName.length() > 6 && localeName.charAt(5) == '_') {
					localesWithInstalledDictionaries.add(new Locale(localeName.substring(0, 2), localeName.substring(3,
							5), localeName.substring(6)));
				}
			}
		}

		return localesWithInstalledDictionaries;
	}

	public static Enumeration<URL> getDictionaryLocations() throws IOException {
		final JSpellPlugin plugin = JSpellPlugin.getDefault();
		if (plugin != null) {
			return plugin.getBundle().getResources("/" + DICTIONARY_LOCATION);
		}
		return null;
	}

	/**
	 * Returns the current locale of the spelling preferences.
	 * 
	 * @param store
	 *            the preference store
	 * @return The current locale of the spelling preferences
	 */
	private Locale getCurrentLocale(IPreferenceStore store) {
		return convertToLocale(store.getString(PreferenceConstants.SPELLING_LOCALE));
	}

	public static Locale convertToLocale(String locale) {
		Locale defaultLocale = Locale.getDefault();
		if (locale.equals(defaultLocale.toString())) {
			return defaultLocale;
		}

		int length = locale.length();
		if (length >= 5) {
			return new Locale(locale.substring(0, 2), locale.substring(3, 5));
		}

		if (length == 2 && locale.indexOf('_') == -1) {
			return new Locale(locale);
		}

		if (length == 3 && locale.charAt(0) == '_') {
			return new Locale("", locale.substring(1));
		}

		return defaultLocale;
	}

	private synchronized void resetUserDictionary() {
		if (checker == null) {
			return;
		}

		// Update user dictionary
		if (userDictionary != null) {
			checker.removeDictionary(userDictionary);
			userDictionary.unload();
			userDictionary = null;
		}

		IPreferenceStore store = JavaPlugin.getDefault().getPreferenceStore();
		String filePath = store.getString(PreferenceConstants.SPELLING_USER_DICTIONARY);

		if (filePath.length() > 0) {
			try {
				File file = new File(filePath);
				if (!file.exists() && !file.createNewFile()) {
					return;
				}

				final URL url = new URL("file", null, filePath); //$NON-NLS-1$
				InputStream stream = url.openStream();
				if (stream != null) {
					try {
						userDictionary = new PersistentSpellDictionary(url);
						checker.addDictionary(userDictionary);
					} finally {
						stream.close();
					}
				}
			} catch (MalformedURLException exception) {
				// Do nothing
			} catch (IOException exception) {
				// Do nothing
			}
		}
	}

	private synchronized void resetSpellChecker() {
		if (checker != null) {
			ISpellDictionary dictionary = localeDictionaries.get(checker.getLocale());
			if (dictionary != null) {
				dictionary.unload();
			}
		}
		checker = null;
	}

}
