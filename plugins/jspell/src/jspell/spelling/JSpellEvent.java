package jspell.spelling;

import java.util.List;

import jspell.JavaName;
import jspell.JavaNameType;

import org.eclipse.jdt.core.IJavaElement;

public class JSpellEvent {

	private static final String CORRECT = "correct";
	private static final String INCORRECT = "incorrect";

	private final JSpellChecker javaSpellChecker;
	private final int index;
	private final String word;
	private final JavaName javaName;
	private final boolean error;

	public JSpellEvent(JSpellChecker javaSpellChecker, int index, String word, JavaName javaName, boolean error) {
		this.javaSpellChecker = javaSpellChecker;
		this.index = index;
		this.word = word;
		this.javaName = javaName;
		this.error = error;
	}

	public boolean isError() {
		return error;
	}

	public IJavaElement getJavaElement() {
		return javaName.getElement();
	}

	public String getMessage() {
		return word + " has incorrect spelling";
	}

	public int getOffset() {
		return javaName.getOffset(index);
	}

	public int getLength() {
		return word.length();
	}

	public List<String> getProposals() {
		return javaSpellChecker.getProposals(word, false);
	}

	public String getNewName(String replacement) {

		JavaNameType type = javaName.getType();
		replacement = type.getCase(index).convert(replacement);

		StringBuilder builder = new StringBuilder(javaName.getName());

		builder.replace(getOffset(), getOffset() + getLength(), replacement);

		return builder.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(word);
		sb.append(" ");
		if (error) {
			sb.append(INCORRECT);
		} else {
			sb.append(CORRECT);
		}

		return sb.toString();
	}

	public String getWord() {
		return word;
	}

}
