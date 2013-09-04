package jspell.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jspell.JSpellPlugin;
import net.sf.eclipsecs.core.jobs.RunCheckstyleOnFilesJob;
import net.sf.eclipsecs.core.projectconfig.filters.UnOpenedFilesFilter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorReference;
import org.eclipse.ui.part.FileEditorInput;

public class CheckFileOnOpenPartListener implements IPartListener2 {
	public void partsOpened(Collection<IWorkbenchPartReference> parts) {
		List filesToCheck = new ArrayList();

		for (IWorkbenchPartReference partRef : parts) {
			IFile editorFile = getEditorFile(partRef);
			if (editorFile != null) {
				UnOpenedFilesFilter.addOpenedFile(editorFile);

			}

			if ((editorFile != null) && (isFileAffected(editorFile))) {
				filesToCheck.add(editorFile);
			}
		}

		RunCheckstyleOnFilesJob job = new RunCheckstyleOnFilesJob(filesToCheck);
		job.schedule();
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		partsOpened(Collections.singleton(partRef));
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		IFile editorFile = getEditorFile(partRef);
		if (editorFile != null) {
			UnOpenedFilesFilter.removeOpenedFile(editorFile);

		}

		if ((editorFile == null) || (!(isFileAffected(editorFile)))) {
			return;
		}
		try {
			editorFile.deleteMarkers("net.sf.eclipsecs.core.CheckstyleMarker", true, 2);
		} catch (CoreException e) {
			JSpellPlugin.log(e);
		}
	}

	private IFile getEditorFile(IWorkbenchPartReference partRef) {
		if (!(partRef instanceof IEditorReference)) {
			return null;
		}

		IFile file = null;
		IWorkbenchPart part = partRef.getPart(false);

		IEditorInput input = null;

		if ((part != null) && (part instanceof IEditorPart)) {
			IEditorPart editor = (IEditorPart) part;
			input = editor.getEditorInput();

		} else {
			EditorReference editRef = (EditorReference) partRef;
			input = getRestoredInput(editRef);
		}

		if (input instanceof FileEditorInput) {
			file = ((FileEditorInput) input).getFile();
		}

		return file;
	}

	private IEditorInput getRestoredInput(EditorReference e) {
		IMemento editorMem = e.getMemento();
		if (editorMem == null) {
			return null;
		}
		IMemento inputMem = editorMem.getChild("input");
		String factoryID = null;
		if (inputMem != null) {
			factoryID = inputMem.getString("factoryID");
		}
		if (factoryID == null) {
			return null;
		}
		IAdaptable input = null;

		IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryID);
		if (factory == null) {
			return null;
		}

		input = factory.createElement(inputMem);
		if (input == null) {
			return null;
		}

		if (!(input instanceof IEditorInput)) {
			return null;
		}
		return ((IEditorInput) input);
	}

	private boolean isFileAffected(IFile file) {
		boolean affected = false;

		IProject project = file.getProject();

		try {
			if ((project.isAccessible()) && (project.hasNature("net.sf.eclipsecs.core.CheckstyleNature"))) {
				affected = true;
			}
		} catch (CoreException e) {
			JSpellPlugin.log(e);
		}

		return affected;
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

	}
}
