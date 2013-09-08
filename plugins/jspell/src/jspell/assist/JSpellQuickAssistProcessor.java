package jspell.assist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jspell.JSpellPlugin;
import jspell.proposal.RenameRefactoringProposal;
import jspell.spelling.JSpellChecker;
import jspell.spelling.JSpellEvent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.ui.IEditorPart;

public class JSpellQuickAssistProcessor implements IQuickAssistProcessor {

	@Override
	public boolean hasAssists(IInvocationContext context) throws CoreException {
		if (!(context instanceof AssistContext)) {
			return false;
		}
		IEditorPart editor = ((AssistContext) context).getEditor();
		if (!(editor instanceof JavaEditor)) {
			return false;
		}
		JavaEditor javaEditor = (JavaEditor) editor;

		ASTNode node = context.getCoveredNode();
		if (!(node instanceof SimpleName)) {
			return false;
		}
		SimpleName name = (SimpleName) node;
		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return false;
		}

		return true;
	}

	@SuppressWarnings("restriction")
	@Override
	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {

		if (!(context instanceof AssistContext)) {
			return null;
		}
		IEditorPart editor = ((AssistContext) context).getEditor();
		if (!(editor instanceof JavaEditor)) {
			return null;
		}
		JavaEditor javaEditor = (JavaEditor) editor;

		ICompilationUnit compilationUnit = context.getCompilationUnit();
		IJavaElement element = compilationUnit.getElementAt(context.getSelectionOffset());

		JSpellChecker spellChecker = JSpellPlugin.getDefault().getSpellChecker();

		Collection<JSpellEvent> events = new ArrayList<JSpellEvent>();
		spellChecker.execute(events, element);

		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
		for (JSpellEvent event : events) {
			if (event.isError()) {
				for (String proposal : event.getProposals()) {
					String word = event.getWord();
					String newName = event.getNewName(proposal);

					proposals.add(new RenameRefactoringProposal(javaEditor, compilationUnit, element, word, newName));
				}

			}
		}

		return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
	}
}
