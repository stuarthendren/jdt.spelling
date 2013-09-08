package jspell;

import jspell.marker.JSpellMarkerFactory;
import jspell.processor.JSpellProcessor;
import jspell.spelling.JSpellChecker;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		new JSpellPlugin();

		JSpellChecker spellChecker = JSpellPlugin.getDefault().getSpellChecker();

		JSpellMarkerFactory markerFactory = new JSpellMarkerFactory();
		JSpellProcessor processor = new JSpellProcessor(markerFactory);

		JavaCore.addElementChangedListener(new JSpellElementChangedListener(spellChecker, processor));
	}

}
