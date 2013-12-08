package jdt.spelling;

import jdt.spelling.checker.Checker;
import jdt.spelling.checker.CheckerFactory;
import jdt.spelling.engine.Engine;
import jdt.spelling.marker.MarkerFactory;
import jdt.spelling.messages.Messages;
import jdt.spelling.processor.Processor;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Represents the jdt.spelling plug-in.
 * 
 */
public class Plugin extends AbstractUIPlugin {

	private static final String PLUGIN_ID = "jdt.spelling";

	private static final int INTERNAL_ERROR = 0;

	private static Plugin plugin;

	private CheckerFactory checkerFactory;

	private Checker checker;

	private Engine engine;

	public static Plugin getDefault() {
		return plugin;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public static void logMessage(String message) {
		log(new Status(IStatus.INFO, getPluginId(), message));
	}

	private static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), INTERNAL_ERROR, message, null));
	}

	public static void logErrorStatus(String message, IStatus status) {
		if (status == null) {
			logErrorMessage(message);
			return;
		}
		MultiStatus multi = new MultiStatus(getPluginId(), INTERNAL_ERROR, message, null);
		multi.add(status);
		log(multi);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), INTERNAL_ERROR, Messages.Plugin_internal_error, e));
	}

	public static boolean isDebug() {
		return getDefault().isDebugging();
	}

	public Plugin() {
		super();
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			PlatformUI.getWorkbench().removeWindowListener(engine);
			JavaCore.removeElementChangedListener(engine);
			Preferences.removeListener(engine);
			Preferences.removeListener(checkerFactory);
			checker = null;
			engine = null;
		} finally {
			super.stop(context);
		}
	}

	public Checker getSpellChecker() {
		return checker;
	}

	public Engine getSpellEngine() {
		return engine;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		checkerFactory = new CheckerFactory();
		checker = checkerFactory.getSpellChecker();

		MarkerFactory markerFactory = new MarkerFactory();
		Processor processor = new Processor(markerFactory);

		engine = new Engine(checker, processor);

		Preferences.addListener(checkerFactory);
		Preferences.addListener(engine);

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				Display.getDefault().syncExec(new Runnable() {
					@Override
					public void run() {
						if (PlatformUI.isWorkbenchRunning()) {
							IWorkbench workbench = PlatformUI.getWorkbench();
							engine.track(workbench);
						}
					}
				});
				JavaCore.addElementChangedListener(engine);
			}
		});
	}

	private IWorkbenchPage internalGetActivePage() {
		IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getActivePage();
	}

	public static ImageDescriptor imageDescriptorFromPlugin(String name) {
		return imageDescriptorFromPlugin(PLUGIN_ID, "icons/" + name);
	}

}
