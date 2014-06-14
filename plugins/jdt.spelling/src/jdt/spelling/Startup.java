package jdt.spelling;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		if (Plugin.isDebug()) {
			Plugin.logMessage("Started");
		}
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				Plugin.getDefault().initialise(workbench);
			}
		});
	}
}
