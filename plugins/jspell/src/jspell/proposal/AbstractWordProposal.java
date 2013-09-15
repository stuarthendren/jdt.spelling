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

public abstract class AbstractWordProposal implements IJavaCompletionProposal {

	private final IQuickAssistInvocationContext context;

	private final IJavaElement element;

	private final int relevance;

	private final String word;

	private final String displayString;

	private final String image;

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
	public AbstractWordProposal(IQuickAssistInvocationContext context, IJavaElement element, int relevance,
			String displayString, String image, String word) {
		this.context = context;
		this.element = element;
		this.relevance = relevance;
		this.displayString = displayString;
		this.image = image;
		this.word = word;
	}

	@Override
	public final void apply(final IDocument document) {

		JSpellPlugin plugin = JSpellPlugin.getDefault();
		JSpellChecker spellChecker = plugin.getSpellChecker();
		JSpellEngine spellEngine = plugin.getSpellEngine();

		process(spellChecker, word);

		spellEngine.checkElement(element);
	}

	protected abstract void process(JSpellChecker spellChecker, String word);

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
		return displayString + word;
	}

	@Override
	public Image getImage() {
		return JSpellPluginImages.getImage(image);
	}

	@Override
	public int getRelevance() {
		return relevance;
	}

	@Override
	public final Point getSelection(final IDocument document) {
		return new Point(context.getOffset(), context.getLength());
	}
}
