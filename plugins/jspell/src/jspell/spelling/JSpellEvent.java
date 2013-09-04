package jspell.spelling;

import jspell.JavaName;

import org.eclipse.jdt.core.IJavaElement;

public class JSpellEvent {

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(word);
		sb.append(" ");
		if (error) {
			sb.append("incorrect");
		} else {
			sb.append("correct");
		}

		return sb.toString();
	}

}
