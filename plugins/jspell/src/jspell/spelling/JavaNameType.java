package jspell.spelling;

public enum JavaNameType {

	UPPER_CAMEL_CASE("UpperCamelCase", "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])", 0),

	LOWER_CAMEL_CASE("lowerCamelCase", "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])", 0) {
		@Override
		public Case getCase(int position) {
			if (position == 0) {
				return Case.LOWER;
			}
			return super.getCase(position);
		}
	},

	DOT("lower.package.name", "\\.", 1) {

		@Override
		public Case getCase(int position) {
			return Case.LOWER;
		}

	},

	UPPER("UPPER_CASE", "_", 1) {
		@Override
		public Case getCase(int position) {
			return Case.UPPER;
		}
	};

	private final String split;

	private final int l;

	private final String displayName;

	private JavaNameType(String displayName, String split, int l) {
		this.displayName = displayName;
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

	public String getDisplayName() {
		return displayName;
	}

}
