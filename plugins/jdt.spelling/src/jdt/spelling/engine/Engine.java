package jdt.spelling.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import jdt.spelling.Plugin;
import jdt.spelling.Preferences;
import jdt.spelling.checker.Checker;
import jdt.spelling.checker.SpellingEvent;
import jdt.spelling.local.LocalVariableDetector;
import jdt.spelling.processor.Processor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ide.ResourceUtil;

public class Engine extends EditorTracker implements IElementChangedListener, IPreferenceChangeListener {

	private static final Collection<String> JAVA_EXTENSIONS = new HashSet<String>(Arrays.asList(JavaCore
			.getJavaLikeExtensions()));

	private final Checker checker;

	private final Processor processor;

	private IResource currentResource;

	public Engine(Checker checker, Processor processor) {
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

	private void checkResource(IResource resource) {
		if (shouldProcess(resource)) {
			checkElement(JavaCore.create(resource));
		}
	}

	private boolean shouldProcess(IResource resource) {
		return resource != null && resource.exists() && IResource.FILE == resource.getType()
				&& isJavaResource(resource);
	}

	private boolean isJavaResource(IResource resource) {
		return JAVA_EXTENSIONS.contains(resource.getFileExtension());
	}

	public void checkElement(IJavaElement element) {
		if (element == null) {
			return;
		}

		ICompilationUnit cu = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);

		if (cu == null) {
			return;
		}

		if (cu.getOwner() != null) {
			cu = cu.getPrimary();
		}

		handleParentSourceReference(cu);
	}

	private void handleParentSourceReference(ICompilationUnit element) {
		if (element instanceof ISourceReference) {
			element = (ICompilationUnit) getParentSourceReference(element);
		} else {
			return;
		}
		Collection<SpellingEvent> events = new ArrayList<SpellingEvent>();
		cascadeHandle(events, element);
		if (Preferences.getBoolean(Preferences.JDT_SPELLING_CHECK_LOCAL)) {
			LocalVariableDetector localVariableDetector = new LocalVariableDetector(element);
			localVariableDetector.process();
			for (ILocalVariable localVariable : localVariableDetector.getLocalVariables()) {
				handle(events, localVariable);
			}
		}
		processor.process(element, events);

	}

	private void cascadeHandle(Collection<SpellingEvent> events, IJavaElement element) {

		handle(events, element);
		if (element instanceof IParent) {
			IParent parent = (IParent) element;
			try {
				for (IJavaElement child : parent.getChildren()) {
					cascadeHandle(events, child);
				}
			} catch (JavaModelException e) {
				Plugin.log(e);
			}
		}
	}

	private void handle(Collection<SpellingEvent> events, IJavaElement element) {
		checker.execute(events, element);
	}

	@Override
	public void editorDeactivated(IEditorPart editor) {
		clearEditor(editor);
		IResource resource = getResource(editor);
		if (resource != null && resource.equals(currentResource)) {
			setCurrentResource(null);
		}
	}

	@Override
	public void editorActivated(IEditorPart editor) {
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
		checkResource(currentResource);
	}

	private void clearEditor(IEditorPart editor) {
		IResource resource = getResource(editor);
		if (shouldProcess(resource)) {
			processor.complete(resource);
		}
	}

	private IResource getResource(IEditorPart editor) {
		if (editor == null) {
			return null;
		}
		IEditorInput editorInput = editor.getEditorInput();
		return ResourceUtil.getResource(editorInput);
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		checkResource(currentResource);
	}

	public void clear() {
		if (shouldProcess(currentResource)) {
			processor.complete(currentResource);
		}
	}

}
