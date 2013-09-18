package jspell.spelling;

public enum JavaNameType {

	UPPER_CAMEL_CASE("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])", 0),

	LOWER_CAMEL_CASE("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])", 0) {
		@Override
		public Case getCase(int position) {
			if (position == 0) {
				return Case.LOWER;
			}
			return super.getCase(position);
		}
	},

	DOT("\\.", 1) {

		@Override
		public Case getCase(int position) {
			return Case.LOWER;
		}

	},

	UPPER("_", 1) {
		@Override
		public Case getCase(int position) {
			return Case.UPPER;
		}
	};

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

	public Case getCase(int position) {
		return Case.TITLE;
	}

}
