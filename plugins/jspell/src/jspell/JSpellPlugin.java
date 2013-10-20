package jspell;

import jspell.engine.JSpellEngine;
import jspell.marker.JSpellMarkerFactory;
import jspell.messages.Messages;
import jspell.processor.JSpellProcessor;
import jspell.spelling.JSpellChecker;
import jspell.spelling.JSpellCheckerFactory;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Represents the jspell plug-in.
 * 
 */
public class JSpellPlugin extends AbstractUIPlugin {

	private static final String PLUGIN_ID = "jspell";

	private static final int INTERNAL_ERROR = 0;

	private static JSpellPlugin jSpellPlugin;

	private final JSpellChecker spellChecker;

	private final JSpellEngine spellEngine;

	public static JSpellPlugin getDefault() {
		return jSpellPlugin;
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
		log(new Status(IStatus.ERROR, getPluginId(), INTERNAL_ERROR, Messages.JSpellPlugin_internal_error, e));
	}

	public static boolean isDebug() {
		return getDefault().isDebugging();
	}

	public JSpellPlugin() {
		super();
		jSpellPlugin = this;

		JSpellCheckerFactory checkerFactory = new JSpellCheckerFactory();
		spellChecker = checkerFactory.getSpellChecker();

		JSpellMarkerFactory markerFactory = new JSpellMarkerFactory();
		JSpellProcessor processor = new JSpellProcessor(markerFactory);

		spellEngine = new JSpellEngine(spellChecker, processor);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			JavaCore.removeElementChangedListener(spellEngine);
		} finally {
			super.stop(context);
		}
	}

	public JSpellChecker getSpellChecker() {
		return spellChecker;
	}

	public JSpellEngine getSpellEngine() {
		return spellEngine;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		JavaCore.addElementChangedListener(spellEngine);
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
