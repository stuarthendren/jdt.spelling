package jspell.messages;

import org.eclipse.osgi.util.NLS;

public class Messages {

	public static String AddWordProposal_add;

	public static String IgnoreWordProposal_ignore;

	public static String RenameRefactoringProposal_Change_to;

	public static String RenameRefactoringProposal_additionalInfo;

	public static String JSpellPlugin_internal_error;

	public static String JSpellChecker_has_incorrect_spelling;

	public static String JSpellPluginPrefs_single_letter;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

}
