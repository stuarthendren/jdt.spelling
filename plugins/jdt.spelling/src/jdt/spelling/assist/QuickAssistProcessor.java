package jdt.spelling.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jdt.spelling.Plugin;
import jdt.spelling.Preferences;
import jdt.spelling.checker.Checker;
import jdt.spelling.checker.SpellingEvent;
import jdt.spelling.local.LocalVariableDetector;
import jdt.spelling.proposal.AddWordProposal;
import jdt.spelling.proposal.IgnoreWordProposal;
import jdt.spelling.proposal.RenameRefactoringProposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

@SuppressWarnings("restriction")
public class QuickAssistProcessor implements IQuickAssistProcessor {

	@Override
	public boolean hasAssists(IInvocationContext context) throws CoreException {
		ASTNode node = context.getCoveredNode();
		if (!Preferences.getBoolean(Preferences.JDT_SPELLING_CHECK_LOCAL) && !(node instanceof SimpleName)) {
			return false;
		}

		SimpleName name = (SimpleName) node;
		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return false;
		}

		return true;
	}

	@Override
	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {

		if (!(context instanceof AssistContext)) {
			return null;
		}
		AssistContext assistContext = (AssistContext) context;

		ICompilationUnit compilationUnit = context.getCompilationUnit();
		int selectionOffset = context.getSelectionOffset();
		int selectionLength = context.getSelectionLength();

		List<IJavaElement> elements = new ArrayList<IJavaElement>();

		elements.add(compilationUnit.getElementAt(selectionOffset));

		if (Preferences.getBoolean(Preferences.JDT_SPELLING_CHECK_LOCAL)) {
			LocalVariableDetector localVariableDetector = new LocalVariableDetector(compilationUnit);
			localVariableDetector.process();
			elements.addAll(localVariableDetector.getLocalVariables());
		}

		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();

		for (IJavaElement element : elements) {
			if (element != null) {

				Checker spellChecker = Plugin.getDefault().getSpellChecker();

				Collection<SpellingEvent> events = new ArrayList<SpellingEvent>();
				spellChecker.execute(events, element);

				for (SpellingEvent event : events) {
					ISourceRange sourceRange = event.getJavaElementSourceRange();
					if (isSelectionInRange(selectionOffset, selectionLength, sourceRange)) {
						String word = event.getWord();
						for (String proposal : event.getProposals()) {
							String newName = event.getNewName(proposal);
							proposals.add(new RenameRefactoringProposal(assistContext, element, word, newName));
						}
						proposals.add(new IgnoreWordProposal(assistContext, element, word));
						proposals.add(new AddWordProposal(assistContext, element, word));
					}
				}
			}
		}
		return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
	}

	private boolean isSelectionInRange(int selectionOffset, int selectionLength, ISourceRange sourceRange) {
		return selectionOffset >= sourceRange.getOffset()
				&& selectionOffset <= sourceRange.getOffset() + sourceRange.getLength()
				&& selectionOffset + selectionLength <= sourceRange.getOffset() + sourceRange.getLength();
	}
}
