package jdt.spelling.checker;

import java.io.File;
import java.io.IOException;
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

import jdt.spelling.Plugin;
import jdt.spelling.Preferences;
import jdt.spelling.dictionary.PersistentSpellDictionary;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellDictionary;
import org.eclipse.jdt.internal.ui.text.spelling.engine.LocaleSensitiveSpellDictionary;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class CheckerFactory implements IPreferenceChangeListener {

	private static final String DICTIONARY_LOCATION = "dictionaries";
	private static final String DEFAULT_ADDED_DICTIONARY = "added.dic";
	private static final String DEFAULT_IGNORE_DICTIONARY = "ignored.dic";

	private final Set<ISpellDictionary> dictionaries;

	private Map<Locale, ISpellDictionary> localeDictionaries;

	private Checker checker;

	public CheckerFactory() {

		dictionaries = new HashSet<ISpellDictionary>();

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
			Plugin.log(exception);
		}
	}

	public synchronized final Checker getSpellChecker() throws IllegalStateException {
		if (dictionaries == null) {
			throw new IllegalStateException("spell checker has been shut down");
		}

		IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
		Locale locale = getCurrentLocale(store);

		if (checker != null && checker.getLocale().equals(locale)) {
			return checker;
		}

		resetSpellChecker();

		String defaultAddedDictionary = DEFAULT_ADDED_DICTIONARY;
		String defaultIgnoreDictionary = DEFAULT_IGNORE_DICTIONARY;
		PersistentSpellDictionary added = createDictionary(Preferences.JDT_SPELLING_ADDITIONS_DICTIONARY,
				defaultAddedDictionary);
		PersistentSpellDictionary ignored = createDictionary(Preferences.JDT_SPELLING_IGNORE_DICTIONARY,
				defaultIgnoreDictionary);

		checker = new Checker(added, ignored, locale);

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

	private PersistentSpellDictionary createDictionary(String preferenceKey, String defaultAddedDictionary) {
		URL url = getPath(preferenceKey);
		if (url == null) {
			url = getWorkspaceDictionaryLocation(defaultAddedDictionary);
		}
		return new PersistentSpellDictionary(url);
	}

	private URL getPath(String preferenceKey) {
		String preferenceValue = Preferences.getString(preferenceKey);
		if (!preferenceValue.isEmpty()) {
			IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
			try {
				String path = variableManager.performStringSubstitution(preferenceValue);
				if (path.length() > 0) {
					File file = new File(path);
					if (canCreateOrWriteTo(file)) {
						return file.toURI().toURL();
					}
				}
			} catch (CoreException e) {
				Plugin.log(e);
			} catch (MalformedURLException e) {
				Plugin.log(e);
			}
		}
		return null;
	}

	private boolean canCreateOrWriteTo(File file) {
		if (!file.exists() && (!file.isAbsolute() || !file.getParentFile().canWrite())) {
			return false;
		} else if (file.exists() && (!file.isFile() || !file.isAbsolute() || !file.canRead() || !file.canWrite())) {
			return false;
		}
		return true;
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
			Plugin.log(ex);
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
		final Plugin plugin = Plugin.getDefault();
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

	private synchronized void resetSpellChecker() {
		if (checker != null) {
			ISpellDictionary dictionary = localeDictionaries.get(checker.getLocale());
			if (dictionary != null) {
				dictionary.unload();
			}
		}
		checker = null;
	}

	private URL getWorkspaceDictionaryLocation(String dictionary) {
		Bundle bundle = Platform.getBundle(Plugin.getPluginId());
		IPath path = Platform.getStateLocation(bundle).append(DICTIONARY_LOCATION).append(dictionary);
		try {
			File file = path.toFile();
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			return new URL("file", null, path.toString());
		} catch (MalformedURLException e) {
			Plugin.log(e);
		} catch (IOException e) {
			Plugin.log(e);
		}
		return null;
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		String key = event.getKey();
		if (Preferences.JDT_SPELLING_ADDITIONS_DICTIONARY.equals(key)) {
			PersistentSpellDictionary dictionary = createDictionary(Preferences.JDT_SPELLING_ADDITIONS_DICTIONARY,
					DEFAULT_ADDED_DICTIONARY);
			getSpellChecker().setAdditionsDictionary(dictionary);
		} else if (Preferences.JDT_SPELLING_IGNORE_DICTIONARY.equals(key)) {
			PersistentSpellDictionary dictionary = createDictionary(Preferences.JDT_SPELLING_IGNORE_DICTIONARY,
					DEFAULT_IGNORE_DICTIONARY);
			getSpellChecker().setIgnoreDictionary(dictionary);
		}

	}
}
