package jspell;

public enum JavaNameType {

	UPPER_CAMEL_CASE("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])", null, 0),

	LOWER_CAMEL_CASE("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])", null, 0) {
		@Override
		public Case getCase(int position) {
			if (position == 0) {
				return Case.LOWER;
			}
			return super.getCase(position);
		}
	},

	DOT("\\.", '.', 1) {

		@Override
		public Case getCase(int position) {
			return Case.LOWER;
		}

	},

	UPPER("_", '_', 1) {
		@Override
		public Case getCase(int position) {
			return Case.UPPER;
		}
	};

	private final String split;

	private final int l;

	private final Character separator;

	private JavaNameType(String split, Character separator, int l) {
		this.split = split;
		this.separator = separator;
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
