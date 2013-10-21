package jspell;

import jspell.spelling.JavaNameType;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

public class JSpellPluginPrefs extends AbstractPreferenceInitializer {

	public static final String JSPELL_MARKER_COLOR = "jspell.marker.color";
	public static final String JSPELL_MARKER_HIGHLIGHT = "jspell.marker.highlight";
	public static final String JSPELL_MARKER_OVERVIEW = "jspell.marker.overview";
	public static final String JSPELL_MARKER_TEXT = "jspell.marker.text";
	public static final String JSPELL_MARKER_RULER = "jspell.marker.ruler";

	public static final String JSPELL_SINGLE_LETTER = "jspell.single.letter";

	@Override
	public void initializeDefaultPreferences() {
		JSpellPluginPrefs.restoreDefaults();
	}

	public static void restoreDefaults() {
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(JSpellPlugin.getPluginId());

		prefs.put(JavaType.TYPE.name(), JavaNameType.UPPER_CAMEL_CASE.name());
		prefs.put(JavaType.ENUM_TYPE.name(), JavaNameType.UPPER_CAMEL_CASE.name());
		prefs.put(JavaType.ANNOTATION.name(), JavaNameType.UPPER_CAMEL_CASE.name());
		prefs.put(JavaType.CONSTANT.name(), JavaNameType.UPPER.name());
		prefs.put(JavaType.METHOD.name(), JavaNameType.LOWER_CAMEL_CASE.name());
		prefs.put(JavaType.PACKAGE_DECLARATION.name(), JavaNameType.DOT.name());
		prefs.put(JavaType.FIELD.name(), JavaNameType.LOWER_CAMEL_CASE.name());
		prefs.put(JavaType.ENUM_INSTANCE.name(), JavaNameType.UPPER.name());
		prefs.put(JavaType.LOCAL_VARIABLE.name(), JavaNameType.LOWER_CAMEL_CASE.name());
		prefs.put(JavaType.FIELD.name(), JavaNameType.LOWER_CAMEL_CASE.name());
		prefs.putBoolean(JSPELL_SINGLE_LETTER, true);

		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			JSpellPlugin.log(e);
		}
	}

	public static boolean getBoolean(String prefId, boolean defaultValue) {
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(JSpellPlugin.getPluginId());
		return prefs.getBoolean(prefId, defaultValue);
	}

	public static int getInt(String prefId, int defaultValue) {
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(JSpellPlugin.getPluginId());
		return prefs.getInt(prefId, defaultValue);
	}

	public static String getString(String prefId, String defaultValue) {
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(JSpellPlugin.getPluginId());
		return prefs.get(prefId, defaultValue);
	}

	public static void setBoolean(String prefId, boolean value) throws BackingStoreException {
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(JSpellPlugin.getPluginId());
		prefs.putBoolean(prefId, value);
		prefs.flush();
	}

	public static void setInt(String prefId, int value) throws BackingStoreException {
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(JSpellPlugin.getPluginId());
		prefs.putInt(prefId, value);
		prefs.flush();
	}

	public static void setString(String prefId, String value) throws BackingStoreException {
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(JSpellPlugin.getPluginId());
		prefs.put(prefId, value);
		prefs.flush();
	}

	public static void setJavaNameType(JavaType javaType, JavaNameType javaNameType) throws BackingStoreException {
		setString(javaType.name(), javaNameType.name());
	}

	public static JavaNameType getJavaNameType(JavaType javaType) {
		String name = getString(javaType.name(), "");
		return JavaNameType.valueOf(name);
	}
}
