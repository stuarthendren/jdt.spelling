package jspell.spelling;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import jspell.JavaName;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellDictionary;
import org.eclipse.jdt.internal.ui.text.spelling.engine.RankedWordProposal;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;

@SuppressWarnings("restriction")
public class JSpellChecker {

	/**
	 * Does this word contain digits?
	 * 
	 * @param word
	 *            the word to check
	 * @return <code>true</code> iff this word contains digits, <code>false></code> otherwise
	 */
	protected static boolean isDigits(final String word) {

		for (int index = 0; index < word.length(); index++) {

			if (Character.isDigit(word.charAt(index))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Does this word contain upper-case letters only?
	 * 
	 * @param word
	 *            The word to check
	 * @return <code>true</code> iff this word only contains upper-case letters, <code>false</code>
	 *         otherwise
	 */
	protected static boolean isUpperCase(final String word) {

		for (int index = word.length() - 1; index >= 0; index--) {

			if (Character.isLowerCase(word.charAt(index))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The dictionaries to use for spell checking. Synchronized to avoid concurrent modifications.
	 */
	private final Set<ISpellDictionary> dictionaries = Collections.synchronizedSet(new HashSet<ISpellDictionary>());

	/**
	 * The words to be ignored. Synchronized to avoid concurrent modifications.
	 */
	private final Set<String> ignored = Collections.synchronizedSet(new HashSet<String>());

	/**
	 * The preference store. Assumes the <code>IPreferenceStore</code> implementation is thread
	 * safe.
	 */
	private final IPreferenceStore preferences;

	/**
	 * The locale of this checker.
	 * 
	 * @since 3.3
	 */
	private final Locale locale;

	/**
	 * Creates a new default spell checker.
	 * 
	 * @param preferences
	 *            the preference store for this spell checker
	 * @param locale
	 *            the locale
	 */
	public JSpellChecker(IPreferenceStore preferences, Locale locale) {
		Assert.isLegal(preferences != null);
		Assert.isLegal(locale != null);

		this.preferences = preferences;
		this.locale = locale;
	}

	/*
	 * @see
	 * org.eclipse.spelling.done.ISpellChecker#addDictionary(org.eclipse.spelling.done.ISpellDictionary
	 * )
	 */
	public final void addDictionary(final ISpellDictionary dictionary) {
		// synchronizing is necessary as this is a write access
		dictionaries.add(dictionary);
	}

	public boolean acceptsWords() {
		// synchronizing might not be needed here since acceptWords is
		// a read-only access and only called in the same thread as
		// the modifying methods add/checkWord (?)
		Set<ISpellDictionary> copy;
		synchronized (dictionaries) {
			copy = new HashSet<ISpellDictionary>(dictionaries);
		}

		ISpellDictionary dictionary = null;
		for (final Iterator<ISpellDictionary> iterator = copy.iterator(); iterator.hasNext();) {

			dictionary = iterator.next();
			if (dictionary.acceptsWords()) {
				return true;
			}
		}
		return false;
	}

	public void addWord(final String word) {
		// synchronizing is necessary as this is a write access
		Set<ISpellDictionary> copy;
		synchronized (dictionaries) {
			copy = new HashSet<ISpellDictionary>(dictionaries);
		}

		final String addable = word.toLowerCase();
		for (final Iterator<ISpellDictionary> iterator = copy.iterator(); iterator.hasNext();) {
			ISpellDictionary dictionary = iterator.next();
			if (dictionary.acceptsWords()) {
				dictionary.addWord(addable);
			}
		}

	}

	public final void checkWord(final String word) {
		// synchronizing is necessary as this is a write access
		ignored.remove(word.toLowerCase());
	}

	public Collection<JSpellEvent> execute(Collection<JSpellEvent> events, JavaName javaName) {
		// final boolean ignoreDigits =
		// preferences.getBoolean(PreferenceConstants.SPELLING_IGNORE_DIGITS);
		// final boolean ignoreUpper =
		// preferences.getBoolean(PreferenceConstants.SPELLING_IGNORE_UPPER);
		// final boolean ignoreNonLetters =
		// preferences.getBoolean(PreferenceConstants.SPELLING_IGNORE_NON_LETTERS);
		final boolean ignoreSingleLetters = preferences.getBoolean(PreferenceConstants.SPELLING_IGNORE_SINGLE_LETTERS);

		javaName.setIgnoreSingleLetters(ignoreSingleLetters);

		String[] words = javaName.getWords();
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (word != null) {

				// synchronizing is necessary as this is called inside the reconciler
				if (!ignored.contains(word)) {
					if (!isCorrect(word)) {
						events.add(new JSpellEvent(this, i, word, javaName, true));
					} else {
						events.add(new JSpellEvent(this, i, word, javaName, false));
					}
				}
			}

		}
		return events;
	}

	public Set<RankedWordProposal> getProposals(final String word, final boolean sentence) {

		// synchronizing might not be needed here since getProposals is
		// a read-only access and only called in the same thread as
		// the modifing methods add/removeDictionary (?)
		Set<ISpellDictionary> copy;
		synchronized (dictionaries) {
			copy = new HashSet<ISpellDictionary>(dictionaries);
		}

		ISpellDictionary dictionary = null;
		final HashSet<RankedWordProposal> proposals = new HashSet<RankedWordProposal>();

		for (final Iterator<ISpellDictionary> iterator = copy.iterator(); iterator.hasNext();) {

			dictionary = iterator.next();
			proposals.addAll(dictionary.getProposals(word, sentence));
		}
		return proposals;
	}

	public final void ignoreWord(final String word) {
		// synchronizing is necessary as this is a write access
		ignored.add(word.toLowerCase());
	}

	public final boolean isCorrect(final String word) {
		// synchronizing is necessary as this is called from execute
		Set<ISpellDictionary> copy;
		synchronized (dictionaries) {
			copy = new HashSet<ISpellDictionary>(dictionaries);
		}

		if (ignored.contains(word.toLowerCase())) {
			return true;
		}

		ISpellDictionary dictionary = null;
		for (final Iterator<ISpellDictionary> iterator = copy.iterator(); iterator.hasNext();) {

			dictionary = iterator.next();
			if (dictionary.isCorrect(word)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.spelling.done.ISpellChecker#removeDictionary(org.eclipse.spelling.done.
	 * ISpellDictionary)
	 */
	public final void removeDictionary(final ISpellDictionary dictionary) {
		// synchronizing is necessary as this is a write access
		dictionaries.remove(dictionary);
	}

	public Locale getLocale() {
		return locale;
	}
}
