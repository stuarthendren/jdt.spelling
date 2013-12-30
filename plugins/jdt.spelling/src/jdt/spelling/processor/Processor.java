package jdt.spelling.processor;

import java.util.Collection;

import jdt.spelling.checker.SpellingEvent;
import jdt.spelling.marker.MarkerFactory;
import jdt.spelling.marker.MarkerJob;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;

public class Processor {

	private final MarkerFactory markerFactory;

	public Processor(MarkerFactory markerFactory) {
		this.markerFactory = markerFactory;
	}

	public void process(final IJavaElement element, final Collection<SpellingEvent> events) {
		final IResource resource = element.getResource();
		MarkerJob job = new MarkerJob(resource, new MarkerJob.MarkerRunnable() {

			@Override
			public void run() throws CoreException {
				prepare(resource);
				for (SpellingEvent event : events) {
					process(event);
				}
			}
		});
		job.schedule();
	}

	private void process(SpellingEvent event) {
		markerFactory.create(event);
	}

	private void prepare(IResource resource) throws CoreException {
		markerFactory.clear(resource);
	}

	public void complete(final IResource resource) {
		MarkerJob job = new MarkerJob(resource, new MarkerJob.MarkerRunnable() {

			@Override
			public void run() throws CoreException {
				markerFactory.clear(resource);
			}
		});
		job.schedule();
	}
}
