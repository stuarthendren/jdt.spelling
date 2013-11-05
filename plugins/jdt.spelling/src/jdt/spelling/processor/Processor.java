package jdt.spelling.processor;

import java.util.Collection;

import jdt.spelling.checker.SpellingEvent;
import jdt.spelling.marker.MarkerFactory;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;

public class Processor {

	private final MarkerFactory markerFactory;

	public Processor(MarkerFactory markerFactory) {
		this.markerFactory = markerFactory;
	}

	public void process(Collection<SpellingEvent> events) {
		for (SpellingEvent event : events) {
			process(event);
		}
	}

	private void process(SpellingEvent event) {
		markerFactory.create(event);
	}

	public void prepare(IJavaElement element) {
		markerFactory.clear(element.getResource());
	}

	public void complete(IResource resource) {
		markerFactory.clear(resource);
	}
}
