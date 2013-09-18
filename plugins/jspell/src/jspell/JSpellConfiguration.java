package jspell;

import jspell.spelling.JavaNameType;

public class JSpellConfiguration {

	private static final JSpellConfiguration INSTANCE = new JSpellConfiguration();

	public static JSpellConfiguration getInstance() {
		return INSTANCE;
	}

	private JSpellConfiguration() {
	}

	public JavaNameType getJavaNameType(JavaType javaType) {
		return JSpellPluginPrefs.getJavaNameType(javaType);
	}

}
