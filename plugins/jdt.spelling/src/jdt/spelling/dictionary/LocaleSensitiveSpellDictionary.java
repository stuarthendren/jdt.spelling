package jdt.spelling.dictionary;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.eclipse.jdt.internal.ui.text.spelling.engine.AbstractSpellDictionary;

@SuppressWarnings("restriction")
public class LocaleSensitiveSpellDictionary extends AbstractSpellDictionary {

	private final Locale locale;

	private final URL location;

	private final String type;

	public LocaleSensitiveSpellDictionary(final Locale locale, final URL location, final String type) {
		this.location = location;
		this.locale = locale;
		this.type = type;
	}

	public final Locale getLocale() {
		return locale;
	}

	@Override
	protected final URL getURL() throws MalformedURLException {
		return new URL(location, locale.toString() + type);
	}

	@Override
	protected int getInitialSize() {
		return 32 * 1024;
	}
}
