package jdt.spelling.dictionary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import jdt.spelling.Plugin;

import org.eclipse.jdt.internal.ui.text.spelling.engine.AbstractSpellDictionary;

/**
 * Persistent modifiable word-list based dictionary.
 * 
 * @since 3.0
 */
public class PersistentSpellDictionary extends AbstractSpellDictionary {

	/** The word list location */
	private final URL fLocation;

	/**
	 * Creates a new persistent spell dictionary.
	 * 
	 * @param url
	 *            the URL of the word list for this dictionary
	 */
	public PersistentSpellDictionary(final URL url) {
		fLocation = url;
		File file = new File(url.getFile());
		if (!file.exists()) {
			throw new RuntimeException("File must exist: " + url.getFile());
		}
	}

	/*
	 * @see org.eclipse.jdt.ui.text.spelling.engine.AbstractSpellDictionary#acceptsWords()
	 */
	@Override
	public boolean acceptsWords() {
		return true;
	}

	/*
	 * @see
	 * org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellDictionary#addWord(java.lang.String)
	 */
	@Override
	public void addWord(final String word) {
		if (isCorrect(word)) {
			return;
		}

		FileOutputStream fileStream = null;
		try {
			Charset charset = Charset.forName(getEncoding());
			ByteBuffer byteBuffer = charset.encode(word + "\n");
			int size = byteBuffer.limit();
			final byte[] byteArray;
			if (byteBuffer.hasArray()) {
				byteArray = byteBuffer.array();
			} else {
				byteArray = new byte[size];
				byteBuffer.get(byteArray);
			}

			fileStream = new FileOutputStream(fLocation.getPath(), true);

			// Encoding UTF-16 charset writes a BOM. In which case we need to cut it away if the
			// file isn't empty
			int bomCutSize = 0;
			if (!isEmpty() && "UTF-16".equals(charset.name())) {
				bomCutSize = 2;
			}

			fileStream.write(byteArray, bomCutSize, size - bomCutSize);
		} catch (IOException exception) {
			Plugin.log(exception);
			return;
		} finally {
			if (fileStream != null) {
				try {
					fileStream.close();
				} catch (IOException e) {
					Plugin.log(e);
				}
			}
		}

		hashWord(word);
	}

	public void clear() {
		File file = new File(fLocation.getPath());
		file.delete();
		unload();
	}

	@Override
	protected final URL getURL() {
		return fLocation;
	}
}
