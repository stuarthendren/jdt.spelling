package jspell.proposal;

import jspell.JSpellPluginImages;
import jspell.messages.Messages;
import jspell.spelling.JSpellChecker;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;

/**
 * Proposal to add the unknown word to the added dictionary.
 * 
 */
public class AddWordProposal extends AbstractWordProposal {

	private static final int RELEVANCE = 7;

	/**
	 * Creates a new add word proposal
	 * 
	 * @param word
	 *            The word to add
	 * @param context
	 *            The invocation context
	 * @param element
	 *            the java element to re-evaluate
	 */
	public AddWordProposal(IQuickAssistInvocationContext context, IJavaElement element, String word) {
		super(context, element, RELEVANCE, Messages.AddWordProposal_add, JSpellPluginImages.ADD, word);
	}

	@Override
	protected void process(JSpellChecker spellChecker, String word) {
		spellChecker.addWord(word);
	}

}
