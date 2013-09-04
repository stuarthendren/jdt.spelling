package jspell;

import jspell.marker.JSpellMarkerFactory;
import jspell.processor.JSpellProcessor;
import jspell.spelling.JSpellChecker;
import jspell.spelling.JSpellCheckerFactory;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		new JSpellPlugin();
		JSpellCheckerFactory checkerFactory = new JSpellCheckerFactory();
		JSpellMarkerFactory markerFactory = new JSpellMarkerFactory();
		JSpellChecker spellChecker = checkerFactory.getSpellChecker();
		JSpellProcessor processor = new JSpellProcessor(markerFactory);
		JavaCore.addElementChangedListener(new JSpellElementChangedListener(spellChecker, processor));
	}

}
