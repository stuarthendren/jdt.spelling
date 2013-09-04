package jspell;

import java.util.Collection;

import jspell.processor.JSpellProcessor;
import jspell.spelling.JSpellChecker;
import jspell.spelling.JSpellEvent;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;

public class JSpellElementChangedListener implements IElementChangedListener {

	private final JSpellChecker checker;

	private final JSpellProcessor processor;

	public JSpellElementChangedListener(JSpellChecker checker, JSpellProcessor processor) {
		this.checker = checker;
		this.processor = processor;
	}

	@Override
	public void elementChanged(ElementChangedEvent event) {
		IJavaElementDelta delta = event.getDelta();
		handleDelta(delta);
	}

	private IJavaElement getParentSourceReference(IJavaElement element) {
		IJavaElement parent = element.getParent();
		if (parent instanceof ISourceReference) {
			element = getParentSourceReference(parent);
		}
		return element;

	}

	protected void handleDelta(IJavaElementDelta delta) {
		if (IJavaElementDelta.REMOVED == delta.getKind()) {
			return;
		}

		IJavaElement element = delta.getElement();
		ICompilationUnit cu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);

		if (cu == null) {
			return;
		}

		if (cu.getOwner() != null) {
			cu = cu.getPrimary();
		}

		handleParentSourceReference(cu);
	}

	private void handleParentSourceReference(IJavaElement element) {
		if (element instanceof ISourceReference) {
			element = getParentSourceReference(element);
		} else {
			return;
		}
		processor.prepare(element);
		cascadeHandle(element);
	}

	private void cascadeHandle(IJavaElement element) {

		handle(element);
		if (element instanceof IParent) {
			IParent parent = (IParent) element;
			try {
				for (IJavaElement child : parent.getChildren()) {
					cascadeHandle(child);
				}
			} catch (JavaModelException e) {
				JSpellPlugin.log(e);
			}
		}
	}

	private void handle(IJavaElement element) {
		JavaType convert = JavaType.convert(element);
		if (convert == null) {
			// Ignore element
			return;
		}
		JavaNameType javaNameType = JSpellConfiguration.getInstance().getJavaNameType(convert);
		processJavaName(new JavaName(javaNameType, element));
	}

	private void processJavaName(JavaName name) {
		Collection<JSpellEvent> events = checker.execute(name);
		processor.process(events);
	}
}
