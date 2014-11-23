package jdt.spelling.dictionary;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import jdt.spelling.Plugin;
import jdt.spelling.Preferences;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.osgi.framework.Bundle;

public class DictionaryFactory {

	private static final String DICTIONARY_LOCATION = "dictionaries";
	private static final String DICTIONARY_TYPE = ".dictionary";
	private static final String CODE_WORDS_TYPE = ".code_words";
	private static final String DEFAULT_ADDED_DICTIONARY = "added.dic";
	private static final String DEFAULT_IGNORE_DICTIONARY = "ignored.dic";

	private Set<Locale> dictionaryLocales = new HashSet<Locale>();
	private Set<Locale> codeWordLocales = new HashSet<Locale>();
	private URL dictionariesLocation;

	public DictionaryFactory() {
		try {
			dictionariesLocation = getDictionaryLocation();
			dictionaryLocales = getLocalesWithInstalledDictionaries(dictionariesLocation, DICTIONARY_TYPE);
			codeWordLocales = getLocalesWithInstalledDictionaries(dictionariesLocation, CODE_WORDS_TYPE);
		} catch (IOException e) {
			Plugin.log(e);
		}
	}

	public PersistentSpellDictionary createAdded() {
		return createDictionary(Preferences.JDT_SPELLING_ADDITIONS_DICTIONARY, DEFAULT_ADDED_DICTIONARY);
	}

	public PersistentSpellDictionary createIgnored() {
		return createDictionary(Preferences.JDT_SPELLING_IGNORE_DICTIONARY, DEFAULT_IGNORE_DICTIONARY);
	}

	public LocaleSensitiveSpellDictionary createDictionary(Locale locale) {
		return new LocaleSensitiveSpellDictionary(locale, dictionariesLocation, DICTIONARY_TYPE);
	}

	public LocaleSensitiveSpellDictionary createCodeWords(Locale locale) {
		if (codeWordLocales.contains(locale)) {
			return new LocaleSensitiveSpellDictionary(locale, dictionariesLocation, CODE_WORDS_TYPE);
		}
		return null;
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

	/**
	 * Returns the locales for which this spell check engine has dictionaries in certain location.
	 *
	 * @param location
	 *            dictionaries location
	 * @return The available locales for this engine
	 */
	private Set<Locale> getLocalesWithInstalledDictionaries(URL location, String type) {
		String[] fileNames = getDictionaryFileNames(location);
		if (fileNames == null) {
			return Collections.emptySet();
		}

		Set<Locale> localesWithInstalledDictionaries = new HashSet<Locale>();

		int fileNameCount = fileNames.length;
		for (int i = 0; i < fileNameCount; i++) {
			String fileName = fileNames[i];
			int localeEnd = fileName.indexOf(type);
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

	private String[] getDictionaryFileNames(URL location) {
		String[] fileNames = null;
		try {
			URL url = FileLocator.toFileURL(location);
			File file = new File(url.getFile());

			if (file.isDirectory()) {
				fileNames = file.list();
			}
		} catch (Exception ex) {
			Plugin.log(ex);
		}
		return fileNames;
	}

	private URL getDictionaryLocation() throws IOException {
		return Plugin.getDefault().getBundle().getResource("/" + DICTIONARY_LOCATION);
	}

	private URL getWorkspaceDictionaryLocation(String dictionary) {
		Bundle bundle = Platform.getBundle(Plugin.getPluginId());
		IPath stateLocation = Platform.getStateLocation(bundle);
		IPath path = stateLocation.append(DICTIONARY_LOCATION).append(dictionary);
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

	public Set<Locale> getAvailableDictionaries() {
		return dictionaryLocales;
	}

	public Set<Locale> getAvailableCodeWords() {
		return codeWordLocales;
	}
}
