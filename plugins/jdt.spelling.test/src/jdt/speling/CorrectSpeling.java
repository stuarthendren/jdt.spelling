package jdt.speling;

@Anotation
public class CorrectSpeling implements IncorectSpelling {

	public static final String CONSTENT_TEST = "JDT SPELLING";

	private final int feildTest = 10;

	@Override
	public void methdTest() {

		String loaclVariale = "JDT Spelling";
		for (int i = 0; i < feildTest; i++) {
			System.out.println(loaclVariale);
		}

	}

}
