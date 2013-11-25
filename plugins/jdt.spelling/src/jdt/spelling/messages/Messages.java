package jdt.spelling.messages;

import org.eclipse.osgi.util.NLS;

public class Messages {

	public static String AddWordProposal_add;

	public static String Checker_has_incorrect_spelling;

	public static String IgnoreWordProposal_ignore;

	public static String Plugin_internal_error;

	public static String RenameRefactoringProposal_Change_to;

	public static String RenameRefactoringProposal_additionalInfo;

	public static String SpellingEvent_incorrect;

	public static String SpellingPreferencePage_ignore_single_letter;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

}
