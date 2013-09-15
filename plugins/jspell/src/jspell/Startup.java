package jspell;

import jspell.engine.JSpellEngine;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		new JSpellPlugin();

		JSpellEngine spellEngine = JSpellPlugin.getDefault().getSpellEngine();
		JavaCore.addElementChangedListener(spellEngine);
	}

}
