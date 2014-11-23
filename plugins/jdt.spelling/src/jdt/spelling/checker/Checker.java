package jdt.spelling.checker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import jdt.spelling.Preferences;
import jdt.spelling.dictionary.CodeWordStatus;
import jdt.spelling.dictionary.LocaleSensitiveSpellDictionary;
import jdt.spelling.dictionary.PersistentSpellDictionary;
import jdt.spelling.enums.JavaNameType;
import jdt.spelling.enums.JavaType;

import org.eclipse.jdt.core.IJavaElement;
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
	 * The main dictionaries to used for spell checking
	 */
	private LocaleSensitiveSpellDictionary mainDictionary;

	/**
	 * An optional set of code words to allow
	 */
	private LocaleSensitiveSpellDictionary codeWordsDictionary;

	/**
	 * The dictionary used for added words.
	 */
	private PersistentSpellDictionary additionsDictionary;

	/**
	 * The dictionary used for ignored words
	 */
	private PersistentSpellDictionary ignoreDictionary;

	/**
	 * Ignore, suggest status of code words
	 *
	 */
	private CodeWordStatus codeWordStatus = CodeWordStatus.OFF;

	/**
	 * Creates a new default spell checker.
	 *
	 * @param preferences
	 *            the preference store for this spell checker
	 * @param locale
	 *            the locale
	 */
	public Checker(PersistentSpellDictionary additionsDictionary, PersistentSpellDictionary ignoreDictionary,
			LocaleSensitiveSpellDictionary mainDictionary, LocaleSensitiveSpellDictionary codeWordsDictionary) {
		this.additionsDictionary = additionsDictionary;
		this.ignoreDictionary = ignoreDictionary;
		this.mainDictionary = mainDictionary;
		this.codeWordsDictionary = codeWordsDictionary;
	}

	public synchronized void addWord(final String word) {
		final String addWord = word.toLowerCase();
		if (additionsDictionary.acceptsWords()) {
			additionsDictionary.addWord(addWord);
		} else {
			throw new IllegalStateException("Unable to add to dictionary");
		}
	}

	public void execute(Collection<SpellingEvent> events, IJavaElement element) {
		JavaType convert = JavaType.convert(element);
		if (convert == null) {
			// Ignore element
			return;
		}

		boolean ignoreSingleLetter = Preferences.getBoolean(Preferences.JDT_SPELLING_IGNORE_SINGLE_LETTER);
		JavaNameType javaNameType = Preferences.getJavaNameType(element.getElementName(), convert);
		JavaName javaName = new JavaName(javaNameType, element);

		String[] words = javaName.getWords();
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (word != null) {

				if (ignoreSingleLetter && word.length() == 1) {
					continue;
				}

				if (!isCorrect(word)) {
					events.add(new SpellingEvent(this, i, word, javaName));
				}
			}
		}
	}

	public List<String> getProposals(final String word) {
		final SortedSet<RankedWordProposal> proposals = new TreeSet<RankedWordProposal>();

		synchronized (this) {
			proposals.addAll(additionsDictionary.getProposals(word, false));
			proposals.addAll(mainDictionary.getProposals(word, false));
			if (CodeWordStatus.SUGGEST == codeWordStatus && codeWordsDictionary != null) {
				proposals.addAll(codeWordsDictionary.getProposals(word, false));
			}
		}

		List<String> words = new ArrayList<String>(proposals.size());
		for (RankedWordProposal proposal : proposals) {
			words.add(proposal.getText());
		}

		return words;
	}

	public synchronized void ignoreWord(final String word) {
		ignoreDictionary.addWord(word);
	}

	public final boolean isCorrect(final String word) {
		if (word == null || word.length() == 0 || isDigits(word)) {
			return true;
		}

		if (ignoreDictionary.isCorrect(word)) {
			return true;
		}

		if (additionsDictionary.isCorrect(word)) {
			return true;
		}

		if (CodeWordStatus.OFF != codeWordStatus && codeWordsDictionary != null && codeWordsDictionary.isCorrect(word)) {
			return true;
		}

		if (mainDictionary.isCorrect(word)) {
			return true;
		}

		return false;
	}

	public Locale getLocale() {
		return mainDictionary.getLocale();
	}

	public synchronized void clearIgnoredWords() {
		ignoreDictionary.clear();
	}

	public synchronized void clearAddedWords() {
		additionsDictionary.clear();
	}

	public synchronized void setAdditionsDictionary(PersistentSpellDictionary additionsDictionary) {
		if (additionsDictionary == null || !additionsDictionary.acceptsWords()) {
			throw new IllegalArgumentException("Additions dictionary must not be null and must accept new words");
		}
		this.additionsDictionary.unload();
		this.additionsDictionary = additionsDictionary;
	}

	public synchronized void setIgnoreDictionary(PersistentSpellDictionary ignoreDictionary) {
		if (ignoreDictionary == null || !ignoreDictionary.acceptsWords()) {
			throw new IllegalArgumentException("Ignore dictionary must not be null and must accept new words");
		}
		this.ignoreDictionary.unload();
		this.ignoreDictionary = ignoreDictionary;
	}

	public synchronized void setMainDictionary(LocaleSensitiveSpellDictionary mainDictionary) {
		if (mainDictionary == null) {
			throw new IllegalArgumentException("Main dictionary must not be null");
		}
		this.mainDictionary.unload();
		this.mainDictionary = mainDictionary;
	}

	public synchronized void setCodeWordDictionary(LocaleSensitiveSpellDictionary codeWordDictionary) {
		if (codeWordsDictionary != null) {
			codeWordsDictionary.unload();
		}
		codeWordsDictionary = codeWordDictionary;
	}

	public void setCodeWordsStatus(CodeWordStatus codeWordStatus) {
		if (codeWordStatus == null) {
			throw new IllegalArgumentException("Code word status can not be null");
		}
		this.codeWordStatus = codeWordStatus;
	}
}
