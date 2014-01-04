package jdt.spelling.checker;

import java.util.List;

import jdt.spelling.enums.JavaNameType;
import jdt.spelling.messages.Messages;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.SourceRange;

public class SpellingEvent {

	public static final char SPACE = ' ';

	private final Checker javaSpellChecker;
	private final int index;
	private final String word;
	private final JavaName javaName;

	public SpellingEvent(Checker javaSpellChecker, int index, String word, JavaName javaName) {
		this.javaSpellChecker = javaSpellChecker;
		this.index = index;
		this.word = word;
		this.javaName = javaName;
	}

	public IJavaElement getJavaElement() {
		return javaName.getElement();
	}

	public String getMessage() {
		return word + SPACE + Messages.Checker_has_incorrect_spelling;
	}

	public int getOffset() {
		return javaName.getOffset(index);
	}

	public int getLength() {
		return word.length();
	}

	public List<String> getProposals() {
		return javaSpellChecker.getProposals(word);
	}

	public String getNewName(String replacement) {

		JavaNameType type = javaName.getType();
		replacement = type.getCase(index).convert(replacement);

		StringBuilder builder = new StringBuilder(javaName.getName());

		builder.replace(getOffset(), getOffset() + getLength(), replacement);

		return builder.toString();
	}

	public ISourceRange getJavaElementSourceRange() throws JavaModelException {
		IJavaElement javaElement = getJavaElement();

		if (javaElement instanceof ISourceReference) {
			ISourceReference sourceReference = (ISourceReference) javaElement;
			return sourceReference.getNameRange();

		} else {
			return null;
		}
	}

	public ISourceRange getSourceRange() throws JavaModelException {
		IJavaElement javaElement = getJavaElement();

		ISourceRange range = null;
		if (javaElement instanceof ISourceReference) {
			ISourceReference sourceReference = (ISourceReference) javaElement;
			range = sourceReference.getNameRange();
		}

		int start = range == null ? 0 : range.getOffset();
		start += getOffset();

		return new SourceRange(start, getLength());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(word);
		sb.append(SPACE);
		sb.append(Messages.SpellingEvent_incorrect);
		return sb.toString();
	}

	public String getWord() {
		return word;
	}

}
