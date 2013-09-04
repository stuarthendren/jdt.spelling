package jspell.marker;

import jspell.JSpellPlugin;
import jspell.spelling.JSpellEvent;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class JSpellMarkerFactory {

	private static final String JSPELL_MARKER = "jspell.marker";

	public IMarker create(IResource resource) {
		try {
			IMarker marker = resource.createMarker(JSPELL_MARKER);
			return marker;
		} catch (CoreException e) {
			JSpellPlugin.log(e);
			return null;
		}
	}

	public IMarker create(JSpellEvent event) {
		try {
			IJavaElement javaElement = event.getJavaElement();
			IResource resource = javaElement.getResource();

			IMarker marker = create(resource);

			ISourceRange range = null;
			if (javaElement instanceof IMember) {
				IMember member = (IMember) javaElement;
				try {
					range = member.getNameRange();
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

			marker.setAttributes(new String[] { IMarker.MESSAGE, IMarker.CHAR_START, IMarker.CHAR_END,
					IMarker.SOURCE_ID }, new Object[] { event.getMessage(), new Integer(start), new Integer(end),
					JSpellPlugin.getPluginId() });

			return marker;
		} catch (CoreException e) {
			JSpellPlugin.log(e);
		}
		return null;
	}

	public IMarker[] find(IResource target) {
		try {
			return target.findMarkers(JSPELL_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			JSpellPlugin.log(e);
			return null;
		}
	}

	public void clear(IResource resource) {
		try {
			resource.deleteMarkers(JSPELL_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			JSpellPlugin.log(e);
		}

	}
}
