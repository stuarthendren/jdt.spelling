package jdt.spelling.checker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jdt.spelling.Preferences;
import jdt.spelling.dictionary.PersistentSpellDictionary;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.text.spelling.engine.ISpellDictionary;
import org.eclipse.jdt.internal.ui.text.spelling.engine.RankedWordProposal;

@SuppressWarnings("restriction")
public class Checker {

	/**
	 * Does this word contain digits?
	 * 
	 * @param word
	 *            the word to check
	 * @return <code>true</code> iff this word contains digits, <code>false></code> otherwise
	 */
	protected static boolean isDigits(final String word) {
		for (int index = 0; index < word.length(); index++) {
			if (!Character.isDigit(word.charAt(index))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Does this word contain digits?
	 * 
	 * @param word
	 *            the word to check
	 * @return <code>true</code> iff this word contains digits, <code>false></code> otherwise
	 */
	protected static boolean containsDigits(final String word) {

		for (int index = 0; index < word.length(); index++) {

			if (Character.isDigit(word.charAt(index))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * The dictionaries to use for spell checking. Synchronized to avoid concurrent modifications.
	 */
	private final Set<ISpellDictionary> dictionaries = Collections.synchronizedSet(new HashSet<ISpellDictionary>());

	/**
	 * The dictionary used for added words.
	 */
	private final PersistentSpellDictionary additionsDictionary;

	/**
	 * The dictionary used for ignored words
	 */
	private final PersistentSpellDictionary ignoreDictionary;

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
	public Checker(PersistentSpellDictionary additionsDictionary, PersistentSpellDictionary ignoreDictionary,
			Locale locale) {
		this.additionsDictionary = additionsDictionary;
		this.ignoreDictionary = ignoreDictionary;
		Assert.isLegal(locale != null);

		this.locale = locale;

		addDictionary(this.additionsDictionary);
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

	public void addWord(final String word) {
		// synchronizing is necessary as this is a write access
		synchronized (additionsDictionary) {
			final String addable = word.toLowerCase();
			if (additionsDictionary.acceptsWords()) {
				additionsDictionary.addWord(addable);
			} else {
				throw new IllegalStateException("Unable to add to dictionary");
			}
		}

	}

	public void execute(Collection<SpellingEvent> events, IJavaElement element) {
		JavaType convert = JavaType.convert(element);
		if (convert == null) {
			// Ignore element
			return;
		}

		boolean ignoreSingleLetter = Preferences.getBoolean(Preferences.JDT_SPELLING_IGNORE_SINGLE_LETTER);
		JavaNameType javaNameType = Preferences.getJavaNameType(convert);
		JavaName javaName = new JavaName(javaNameType, element);

		String[] words = javaName.getWords();
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (word != null) {

				if (ignoreSingleLetter && word.length() == 1) {
					continue;
				}

				// synchronizing is necessary as this is called inside the reconciler
				if (!isCorrect(word)) {
					events.add(new SpellingEvent(this, i, word, javaName));
				}
			}
		}
	}

	public List<String> getProposals(final String word) {

		// synchronizing might not be needed here since getProposals is
		// a read-only access and only called in the same thread as
		// the modifing methods add/removeDictionary (?)
		Set<ISpellDictionary> copy;
		synchronized (dictionaries) {
			copy = new HashSet<ISpellDictionary>(dictionaries);
		}

		ISpellDictionary dictionary = null;
		final SortedSet<RankedWordProposal> proposals = new TreeSet<RankedWordProposal>();

		for (final Iterator<ISpellDictionary> iterator = copy.iterator(); iterator.hasNext();) {

			dictionary = iterator.next();
			proposals.addAll(dictionary.getProposals(word, false));
		}

		List<String> words = new ArrayList<String>(proposals.size());
		for (RankedWordProposal proposal : proposals) {
			words.add(proposal.getText());
		}

		return words;
	}

	public final void ignoreWord(final String word) {
		// synchronizing is necessary as this is a write access
		synchronized (ignoreDictionary) {
			ignoreDictionary.addWord(word);
		}
	}

	public final boolean isCorrect(final String word) {
		if (word == null || word.length() == 0 || isDigits(word)) {
			return true;
		}

		if (ignoreDictionary.isCorrect(word)) {
			return true;
		}

		// synchronizing is necessary as this is called from execute
		Set<ISpellDictionary> copy;
		synchronized (dictionaries) {
			copy = new HashSet<ISpellDictionary>(dictionaries);
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

	public final void removeDictionary(final ISpellDictionary dictionary) {
		// synchronizing is necessary as this is a write access
		dictionaries.remove(dictionary);
	}

	public Locale getLocale() {
		return locale;
	}

	public void clearIgnoredWords() {
		synchronized (ignoreDictionary) {
			ignoreDictionary.clear();
		}
	}

	public void clearAddedWords() {
		synchronized (additionsDictionary) {
			additionsDictionary.clear();
		}
	}
}
