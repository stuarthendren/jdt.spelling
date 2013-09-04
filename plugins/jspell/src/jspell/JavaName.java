package jspell;

import org.eclipse.jdt.core.IJavaElement;

public class JavaName {

	private final IJavaElement element;

	private final JavaNameType type;

	public JavaName(JavaNameType type, IJavaElement element) {
		this.type = type;
		this.element = element;

	}

	public IJavaElement getElement() {
		return element;
	}

	public String[] getWords() {
		return type.getWords(element.getElementName());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(element.getElementName());
		sb.append(":");
		// sb.append(element.getClass().getName());
		sb.append(" ");
		sb.append(type.toString());
		sb.append(" ");
		for (String word : getWords()) {
			sb.append(word);
			sb.append(", ");
		}
		return sb.toString();
	}

	public void setIgnoreSingleLetters(boolean ignoreSingleLetters) {
		// TODO Auto-generated method stub

	}

	public int getOffset(int i) {
		int offset = 0;
		String[] words = getWords();
		for (int j = 0; j < i; j++) {
			offset += words[j].length();
			offset += type.getSeparatorLength();
		}
		return offset;
	}
}
