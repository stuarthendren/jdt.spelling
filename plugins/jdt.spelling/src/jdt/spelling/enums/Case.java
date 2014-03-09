package jdt.spelling.enums;

public enum Case {

	UPPER {
		@Override
		public String convert(String word) {
			return word.toUpperCase();
		}
	},

	LOWER {
		@Override
		public String convert(String word) {
			return word.toLowerCase();
		}
	},

	TITLE {
		@Override
		public String convert(String word) {
			StringBuilder builder = new StringBuilder(word.toLowerCase());
			builder.setCharAt(0, Character.toUpperCase(word.charAt(0)));
			return builder.toString();
		}
	};

	public String convert(String word) {
		return word;
	}

	public static boolean isUpper(String elementName) {
		for (char c : elementName.toCharArray()) {
			if (Character.isLetter(c) && Character.isLowerCase(c)) {
				return false;
			}
		}
		return true;
	}

}
