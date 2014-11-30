package jdt.spelling.enums;

import java.util.regex.Pattern;

/*
 * Numbers can be separated when the separator size is zero, this means they are easier to handle later
 * but other numbers are included in words to maintain the separator lengths and calculate offsets
 *
 */
public enum JavaNameType {

	UPPER_CAMEL_CASE("UpperCamelCase", "(?<!^)((?<=[$_])|(?=[$_]))|(?<!^)(?=[A-Z0-9][a-z]*)", 0),

	LOWER_CAMEL_CASE("lowerCamelCase", "(?<!^)((?<=[$_])|(?=[$_]))|(?<!^)(?=[A-Z0-9][a-z]*)", 0) {
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

	UPPER("UPPER_CASE", "[$_]", 1) {
		@Override
		public Case getCase(int position) {
			return Case.UPPER;
		}
	};

	private final int l;

	private final String displayName;

	private final Pattern pattern;

	private JavaNameType(String displayName, String regex, int l) {
		this.displayName = displayName;
		this.l = l;
		pattern = Pattern.compile(regex);
	}

	public String[] getWords(String name) {
		return pattern.split(name, 0);
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
