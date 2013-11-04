package jspell.processor;

import java.util.Collection;

import jspell.marker.JSpellMarkerFactory;
import jspell.spelling.JSpellEvent;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;

public class JSpellProcessor {

	private final JSpellMarkerFactory markerFactory;

	public JSpellProcessor(JSpellMarkerFactory markerFactory) {
		this.markerFactory = markerFactory;
	}

	public void process(Collection<JSpellEvent> events) {
		for (JSpellEvent event : events) {
			process(event);
		}
	}

	private void process(JSpellEvent event) {
		if (event.isError()) {
			markerFactory.create(event);
		}
	}

	public void prepare(IJavaElement element) {
		markerFactory.clear(element.getResource());
	}

	public void complete(IResource resource) {
		markerFactory.clear(resource);
	}
}
