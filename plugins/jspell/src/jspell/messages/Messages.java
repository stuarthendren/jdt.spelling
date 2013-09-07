package jspell.messages;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import jspell.JSpellPlugin;

public class Messages {

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(JSpellPlugin.getPluginId());

	public static String JSpellPlugin_internal_error;

	/**
	 * Fetches a message for the specified key
	 * 
	 * @param key
	 *            - key to be translated
	 * @return - the message
	 */
	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Fetches a message for the specified key and inserts parameters
	 * 
	 * @param key
	 *            - key to be translated
	 * @param params
	 *            - parameters to be inserted into the message
	 * @return - the message
	 */
	public static String getString(String key, Object[] params) {
		if (params == null) {
			return getString(key);
		}
		try {
			return MessageFormat.format(getString(key), params);
		} catch (Exception e) {
			return "!" + key + "!";
		}
	}
}
