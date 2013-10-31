package jspell.engine;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public abstract class EditorTracker implements IWindowListener, IPageListener, IPartListener {

	/**
	 * <p>
	 * <strong>Must be called on the UI thread</strong>
	 * <p>
	 * Track will add listeners to workbench to track Editors.
	 */
	public void track(IWorkbench workbench) {
		IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			activeWorkbenchWindow.addPageListener(this);
			IWorkbenchPage[] pages = activeWorkbenchWindow.getPages();
			for (IWorkbenchPage page : pages) {
				page.addPartListener(this);
			}
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage != null) {
				pageActivated(activePage);
				IWorkbenchPart activePart = activePage.getActivePart();
				if (activePart != null) {
					partActivated(activePart);
				}
			}
		}
		workbench.addWindowListener(this);
	}

	// --- Window listener

	@Override
	public void windowActivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowDeactivated(IWorkbenchWindow window) {
	}

	@Override
	public void windowClosed(IWorkbenchWindow window) {
		window.removePageListener(this);
	}

	@Override
	public void windowOpened(IWorkbenchWindow window) {
		window.addPageListener(this);
	}

	// ---- IPageListener

	@Override
	public void pageActivated(IWorkbenchPage page) {
	}

	@Override
	public void pageClosed(IWorkbenchPage page) {
		page.removePartListener(this);
	}

	@Override
	public void pageOpened(IWorkbenchPage page) {
		page.addPartListener(this);
	}

	// ---- Part Listener

	@Override
	public void partActivated(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorActivated((IEditorPart) part);
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorBroughtToTop((IEditorPart) part);
		}
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorClosed((IEditorPart) part);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			editorOpened((IEditorPart) part);
		}
	}

	public abstract void editorOpened(IEditorPart part);

	public abstract void editorActivated(IEditorPart part);

	public abstract void editorBroughtToTop(IEditorPart part);

	public abstract void editorClosed(IEditorPart part);

}
