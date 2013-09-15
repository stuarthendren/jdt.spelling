package jspell.proposal;

import jspell.JSpellPlugin;
import jspell.JSpellPluginImages;
import jspell.engine.JSpellEngine;
import jspell.spelling.JSpellChecker;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Proposal to add the unknown word to the added dictionary.
 * 
 */
public class AddWordProposal implements IJavaCompletionProposal {

	private static final int RELEVANCE = 7;

	/** The invocation context */
	private final IQuickAssistInvocationContext context;

	/** The word to add */
	private final String word;

	private final IJavaElement element;

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
		this.context = context;
		this.element = element;
		this.word = word;
	}

	@Override
	public final void apply(final IDocument document) {

		JSpellPlugin plugin = JSpellPlugin.getDefault();
		JSpellChecker spellChecker = plugin.getSpellChecker();
		JSpellEngine spellEngine = plugin.getSpellEngine();

		spellChecker.addWord(word);
		spellEngine.checkElement(element);
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public final IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return jspell.messages.Messages.AddWordProposal_add + word;
	}

	@Override
	public Image getImage() {
		return JSpellPluginImages.getImage(JSpellPluginImages.ADD);
	}

	@Override
	public int getRelevance() {
		return RELEVANCE;
	}

	@Override
	public final Point getSelection(final IDocument document) {
		return new Point(context.getOffset(), context.getLength());
	}
}
