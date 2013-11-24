package jdt.spelling;

import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		if (Plugin.isDebug()) {
			Plugin.logMessage("Started");
		}
	}
}
