package jspell;

import org.eclipse.ui.IStartup;

public class JSpellStartup implements IStartup {

	@Override
	public void earlyStartup() {
		JSpellPlugin.logMessage("Started");
	}
}
