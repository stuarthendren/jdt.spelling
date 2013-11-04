package jdt.spelling.proposal;

import jdt.spelling.Images;
import jdt.spelling.checker.Checker;
import jdt.spelling.messages.Messages;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;

/**
 * Proposal to ignore the unknown word.
 * 
 */
public class IgnoreWordProposal extends AbstractWordProposal {

	private static final int RELEVANCE = 6;

	/**
	 * Creates a new add word proposal
	 * 
	 * @param word
	 *            The word to ignore
	 * @param context
	 *            The invocation context
	 * @param element
	 *            the java element to re-evaluate
	 */
	public IgnoreWordProposal(IQuickAssistInvocationContext context, IJavaElement element, String word) {
		super(context, element, RELEVANCE, Messages.IgnoreWordProposal_ignore, Images.REMOVE, word);
	}

	@Override
	protected void process(Checker spellChecker, String word) {
		spellChecker.ignoreWord(word);
	}

}
