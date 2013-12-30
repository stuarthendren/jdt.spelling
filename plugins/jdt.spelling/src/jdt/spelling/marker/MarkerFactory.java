package jdt.spelling.marker;

import jdt.spelling.Plugin;
import jdt.spelling.checker.SpellingEvent;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

@SuppressWarnings("restriction")
public class MarkerFactory {

	public static final String JDT_SPELLING_MARKER = "jdt.spelling.marker";

	public void create(SpellingEvent event) {
		try {
			IJavaElement javaElement = event.getJavaElement();
			IResource resource = javaElement.getResource();

			ISourceRange range = null;
			if (javaElement instanceof ISourceReference) {
				ISourceReference sourceReference = (ISourceReference) javaElement;
				try {
					range = sourceReference.getNameRange();
				} catch (JavaModelException e) {
					if (e.getJavaModelStatus().getCode() != IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
						throw e;
					}
					if (!CharOperation.equals(javaElement.getElementName().toCharArray(),
							TypeConstants.PACKAGE_INFO_NAME)) {
						throw e;
					}
					// else silently swallow the exception as the synthetic interface type
					// package-info has no source range really.
					// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=258145
				}
			}

			int start = range == null ? 0 : range.getOffset();
			start += event.getOffset();

			int end = start + event.getLength();

			scheduleWorkspaceJob(event.getMessage(), resource, start, end);

		} catch (CoreException e) {
			Plugin.log(e);
		}

	}

	private void scheduleWorkspaceJob(final String message, final IResource resource, final int start, final int end)
			throws CoreException {
		IMarker marker = create(resource);
		if (marker != null) {
			marker.setAttributes(new String[] { IMarker.MESSAGE, IMarker.CHAR_START, IMarker.CHAR_END,
					IMarker.SOURCE_ID },
					new Object[] { message, new Integer(start), new Integer(end), Plugin.getPluginId() });
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

	public void clear(final IResource resource) throws CoreException {
		if (resource.exists()) {
			resource.deleteMarkers(JDT_SPELLING_MARKER, true, IResource.DEPTH_INFINITE);
		}
	}
}
