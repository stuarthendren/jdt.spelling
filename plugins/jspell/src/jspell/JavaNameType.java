package jspell;

public enum JavaNameType {

	UPPER_CAMEL_CASE("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])", 0), LOWER_CAMEL_CASE(
			"(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])", 0), DOT("\\.", 1), UPPER("_", 1);

	private final String split;
	private final int l;

	private JavaNameType(String split, int l) {
		this.split = split;
		this.l = l;
	}

	public String[] getWords(String name) {
		return name.split(split);
	}

	public int getSeparatorLength() {
		return l;
	}

}
