package jdt.spelling.marker;

import jdt.spelling.Plugin;
import jdt.spelling.checker.SpellingEvent;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;

public class MarkerFactory {

	public static final String JDT_SPELLING_MARKER = "jdt.spelling.marker";

	public static final String JDT_SPELLING_MARKER_WORD = "jdt.spelling.marker.word";

	public void create(SpellingEvent event) {
		try {
			IJavaElement javaElement = event.getJavaElement();
			IResource resource = javaElement.getResource();
			ISourceRange sourceRange = event.getSourceRange();

			scheduleWorkspaceJob(event.getMessage(), resource, sourceRange.getOffset(), sourceRange.getOffset()
					+ sourceRange.getLength(), event.getWord());

		} catch (CoreException e) {
			Plugin.log(e);
		}

	}

	private void scheduleWorkspaceJob(final String message, final IResource resource, final int start, final int end,
			String word)
			throws CoreException {
		IMarker marker = create(resource);
		if (marker != null) {
			marker.setAttributes(new String[] { IMarker.MESSAGE, IMarker.CHAR_START, IMarker.CHAR_END,
					IMarker.SOURCE_ID, JDT_SPELLING_MARKER_WORD },
					new Object[] { message, new Integer(start), new Integer(end), Plugin.getPluginId(), word });
		}
	}

	private IMarker create(IResource resource) {
		try {
			if (resource.exists()) {
				return resource.createMarker(JDT_SPELLING_MARKER);
			} else {
				return null;
			}
		} catch (CoreException e) {
			Plugin.log(e);
			return null;
		}
	}

	public IMarker[] find(IResource target) {
		try {
			return target.findMarkers(JDT_SPELLING_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			Plugin.log(e);
			return null;
		}
	}

	public void clear(final IResource resource) {
		try {
			resource.deleteMarkers(JDT_SPELLING_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			Plugin.log(e);
		}
	}
}
