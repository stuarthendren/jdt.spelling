package jdt.spelling;

import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		Plugin.logMessage("Started");
	}
}
