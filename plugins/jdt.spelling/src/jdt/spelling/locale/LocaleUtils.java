package jdt.spelling.locale;

import java.util.Locale;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

public class LocaleUtils {

	/**
	 * Returns the current locale of the spelling preferences.
	 * 
	 * @param store
	 *            the preference store
	 * @return The current locale of the spelling preferences
	 */
	public static Locale getCurrentLocale(IPreferenceStore store) {
		return org.apache.commons.lang.LocaleUtils.toLocale(store.getString(PreferenceConstants.SPELLING_LOCALE));
	}

	public static boolean isSameLanguage(Locale firstLocale, Locale secondLocale) {
		String firstLanguage = firstLocale.getLanguage();
		String secondLanguage = secondLocale.getLanguage();
		return firstLanguage.equals(secondLanguage);
	}

}
