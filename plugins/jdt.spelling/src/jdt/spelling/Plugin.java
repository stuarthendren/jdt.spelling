package jdt.spelling;

import jdt.spelling.checker.Checker;
import jdt.spelling.checker.CheckerFactory;
import jdt.spelling.dictionary.DictionaryFactory;
import jdt.spelling.engine.Engine;
import jdt.spelling.marker.MarkerFactory;
import jdt.spelling.messages.Messages;
import jdt.spelling.processor.Processor;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Represents the jdt.spelling plug-in.
 * 
 */
public class Plugin extends AbstractUIPlugin implements IPreferenceChangeListener {

	private static final String PLUGIN_ID = "jdt.spelling";

	private static final int INTERNAL_ERROR = 0;

	private static Plugin plugin;

	private CheckerFactory checkerFactory;

	private Checker checker;

	private Engine engine;

	private IWorkbench workbench;

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
	public void start(BundleContext context) throws Exception {
		super.start(context);
		DictionaryFactory dictionaryFactory = new DictionaryFactory();
		Preferences.setAvailableLocales(dictionaryFactory.getAvailableDictionaries());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			disable();
		} finally {
			if (workbench != null) {
				Preferences.removeListener(this);
				workbench = null;
			}
			if (Platform.isRunning()) {
				super.stop(context);
			}
		}
	}

	public Checker getSpellChecker() {
		return checker;
	}

	public Engine getSpellEngine() {
		return engine;
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

	public void initialise(IWorkbench workbench) {
		this.workbench = workbench;
		Preferences.addListener(this);
		updateStatus();
	}

	private synchronized void enable() {
		if (workbench != null) {
			DictionaryFactory dictionaryFactory = new DictionaryFactory();
			checkerFactory = new CheckerFactory(dictionaryFactory);
			checker = checkerFactory.getSpellChecker();

			MarkerFactory markerFactory = new MarkerFactory();
			Processor processor = new Processor(markerFactory);

			engine = new Engine(checker, processor);

			Preferences.addListener(checkerFactory);
			Preferences.addListener(engine);

			engine.track(workbench);
			JavaCore.addElementChangedListener(engine);
		}
	}

	private synchronized void disable() {
		if (workbench != null && engine != null) {
			engine.untrack(workbench);
			JavaCore.removeElementChangedListener(engine);
			Preferences.removeListener(engine);
			Preferences.removeListener(checkerFactory);
			checkerFactory = null;
			checker = null;
			engine = null;
		}
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		String key = event.getKey();
		if (Preferences.JDT_SPELLING_ENABLED.equals(key)) {
			updateStatus();
		}
	}

	private void updateStatus() {
		workbench.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				boolean enabled = Preferences.getBoolean(Preferences.JDT_SPELLING_ENABLED);
				if (enabled) {
					enable();
				} else {
					disable();
				}
			}
		});
	}
}