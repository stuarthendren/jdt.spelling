package jspell.engine;

import java.util.ArrayList;
import java.util.Collection;

import jspell.JSpellPlugin;
import jspell.processor.JSpellProcessor;
import jspell.spelling.JSpellChecker;
import jspell.spelling.JSpellEvent;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;

public class JSpellEngine extends EditorTracker implements IElementChangedListener {

	private final JSpellChecker checker;

	private final JSpellProcessor processor;

	private IResource currentResource;

	public JSpellEngine(JSpellChecker checker, JSpellProcessor processor) {
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

		if (isCurrent(element)) {
			checkElement(element);
		}

	}

	public void checkElement(IJavaElement element) {
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
		Collection<JSpellEvent> events = new ArrayList<JSpellEvent>();
		cascadeHandle(events, element);
		processor.prepare(element);
		processor.process(events);

	}

	private void cascadeHandle(Collection<JSpellEvent> events, IJavaElement element) {

		handle(events, element);
		if (element instanceof IParent) {
			IParent parent = (IParent) element;
			try {
				for (IJavaElement child : parent.getChildren()) {
					cascadeHandle(events, child);
				}
			} catch (JavaModelException e) {
				JSpellPlugin.log(e);
			}
		}
	}

	private void handle(Collection<JSpellEvent> events, IJavaElement element) {
		checker.execute(events, element);
	}

	@Override
	public void editorOpened(IEditorPart editor) {
		setCurrentResource(editor);
	}

	@Override
	public void editorClosed(IEditorPart editor) {
		clearEditor(editor);
		setCurrentResource(null);
	}

	@Override
	public void editorActivated(IEditorPart editor) {
		setCurrentResource(editor);
	}

	@Override
	public void editorBroughtToTop(IEditorPart editor) {
		setCurrentResource(editor);
	}

	private boolean isCurrent(IJavaElement element) {
		IResource resource = element.getResource();
		if (resource == null) {
			return false;
		}
		return resource.equals(currentResource);
	}

	private void setCurrentResource(IEditorPart editor) {
		currentResource = getResource(editor);
	}

	private void clearEditor(IEditorPart editor) {
		IResource resource = getResource(editor);
		if (resource != null) {
			processor.complete(resource);
		}
	}

	private IResource getResource(IEditorPart editor) {
		if (editor != null) {
			IEditorInput editorInput = editor.getEditorInput();
			return ResourceUtil.getResource(editorInput);
		}
		return null;
	}

}
