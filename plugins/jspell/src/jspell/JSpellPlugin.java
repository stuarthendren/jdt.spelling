package jspell;

import java.util.Iterator;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.IJavaStatusConstants;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider;
import org.eclipse.jdt.internal.ui.propertiesfileeditor.PropertiesFileDocumentProvider;
import org.eclipse.jdt.internal.ui.text.java.ContentAssistHistory;
import org.eclipse.jdt.internal.ui.text.java.hover.JavaEditorTextHoverDescriptor;
import org.eclipse.jdt.internal.ui.text.spelling.SpellCheckEngine;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDescriptorRegistry;
import org.eclipse.jdt.internal.ui.viewsupport.ProblemMarkerManager;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ConfigurationElementSorter;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.osgi.framework.BundleContext;

/**
 * Represents the jspell plug-in. It provides a series of convenience methods such as access to the
 * workbench, keeps track of elements shared by all editors and viewers of the plug-in such as
 * document providers and find-replace-dialogs.
 */
public class JSpellPlugin extends AbstractUIPlugin {

	private static final String PLUGIN_ID = "jspell";

	private static JSpellPlugin jSpellPlugin;

	/**
	 * Property change listener on this plugin's preference store.
	 * 
	 * @since 3.0
	 */
	private IPropertyChangeListener fPropertyChangeListener;

	private JavaEditorTextHoverDescriptor[] fJavaEditorTextHoverDescriptors;

	/**
	 * The combined preference store.
	 * 
	 * @since 3.0
	 */
	private IPreferenceStore fCombinedPreferenceStore;

	/**
	 * The shared Java properties file document provider.
	 * 
	 * @since 3.1
	 */
	private IDocumentProvider fPropertiesFileDocumentProvider;

	/**
	 * Content assist history.
	 * 
	 * @since 3.2
	 */
	private ContentAssistHistory fContentAssistHistory;

	private ICompilationUnitDocumentProvider compilationUnitDocumentProvider;

	private ClassFileDocumentProvider classFileDocumentProvider;

	private ImageDescriptorRegistry imageDescriptorRegistry;

	private ProblemMarkerManager fProblemMarkerManager;

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

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, message, null));
	}

	public static void logErrorStatus(String message, IStatus status) {
		if (status == null) {
			logErrorMessage(message);
			return;
		}
		MultiStatus multi = new MultiStatus(getPluginId(), IJavaStatusConstants.INTERNAL_ERROR, message, null);
		multi.add(status);
		log(multi);
	}

	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IJavaStatusConstants.INTERNAL_ERROR,
				JavaUIMessages.JavaPlugin_internal_error, e));
	}

	public static boolean isDebug() {
		return getDefault().isDebugging();
	}

	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return getDefault().internalGetImageDescriptorRegistry();
	}

	public JSpellPlugin() {
		super();
		jSpellPlugin = this;
	}

	/*
	 * @see org.eclipse.core.runtime.Plugin#stop
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {

			if (fContentAssistHistory != null) {
				ContentAssistHistory.store(fContentAssistHistory, getPluginPreferences(),
						PreferenceConstants.CODEASSIST_LRU_HISTORY);
				fContentAssistHistory = null;
			}

			SpellCheckEngine.shutdownInstance();

		} finally {
			super.stop(context);
		}
	}

	private IWorkbenchPage internalGetActivePage() {
		IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		return window.getActivePage();
	}

	public synchronized ICompilationUnitDocumentProvider getCompilationUnitDocumentProvider() {
		if (compilationUnitDocumentProvider == null) {
			compilationUnitDocumentProvider = new CompilationUnitDocumentProvider();
		}
		return compilationUnitDocumentProvider;
	}

	/**
	 * Returns the shared document provider for Java properties files used by this plug-in instance.
	 * 
	 * @return the shared document provider for Java properties files
	 * @since 3.1
	 */
	public synchronized IDocumentProvider getPropertiesFileDocumentProvider() {
		if (fPropertiesFileDocumentProvider == null) {
			fPropertiesFileDocumentProvider = new PropertiesFileDocumentProvider();
		}
		return fPropertiesFileDocumentProvider;
	}

	public synchronized ClassFileDocumentProvider getClassFileDocumentProvider() {
		if (classFileDocumentProvider == null) {
			classFileDocumentProvider = new ClassFileDocumentProvider();
		}
		return classFileDocumentProvider;
	}

	public synchronized ProblemMarkerManager getProblemMarkerManager() {
		if (fProblemMarkerManager == null) {
			fProblemMarkerManager = new ProblemMarkerManager();
		}
		return fProblemMarkerManager;
	}

	/**
	 * Returns all Java editor text hovers contributed to the workbench.
	 * 
	 * @return an array of JavaEditorTextHoverDescriptor
	 * @since 2.1
	 */
	public synchronized JavaEditorTextHoverDescriptor[] getJavaEditorTextHoverDescriptors() {
		if (fJavaEditorTextHoverDescriptors == null) {
			fJavaEditorTextHoverDescriptors = JavaEditorTextHoverDescriptor.getContributedHovers();
			ConfigurationElementSorter sorter = new ConfigurationElementSorter() {
				/*
				 * @see
				 * org.eclipse.ui.texteditor.ConfigurationElementSorter#getConfigurationElement(
				 * java.lang.Object)
				 */
				@Override
				public IConfigurationElement getConfigurationElement(Object object) {
					return ((JavaEditorTextHoverDescriptor) object).getConfigurationElement();
				}
			};
			sorter.sort(fJavaEditorTextHoverDescriptors);

			// Move Best Match hover to front
			for (int i = 0; i < fJavaEditorTextHoverDescriptors.length - 1; i++) {
				if (PreferenceConstants.ID_BESTMATCH_HOVER.equals(fJavaEditorTextHoverDescriptors[i].getId())) {
					JavaEditorTextHoverDescriptor hoverDescriptor = fJavaEditorTextHoverDescriptors[i];
					for (int j = i; j > 0; j--) {
						fJavaEditorTextHoverDescriptors[j] = fJavaEditorTextHoverDescriptors[j - 1];
					}
					fJavaEditorTextHoverDescriptors[0] = hoverDescriptor;
					break;
				}

			}
		}

		return fJavaEditorTextHoverDescriptors;
	}

	/**
	 * Resets the Java editor text hovers contributed to the workbench.
	 * <p>
	 * This will force a rebuild of the descriptors the next time a client asks for them.
	 * </p>
	 * 
	 * @since 2.1
	 */
	public synchronized void resetJavaEditorTextHoverDescriptors() {
		fJavaEditorTextHoverDescriptors = null;
	}

	/**
	 * Registers the given Java template context.
	 * 
	 * @param registry
	 *            the template context type registry
	 * @param id
	 *            the context type id
	 * @param parent
	 *            the parent context type
	 * @since 3.4
	 */
	private static void registerJavaContext(ContributionContextTypeRegistry registry, String id,
			TemplateContextType parent) {
		TemplateContextType contextType = registry.getContextType(id);
		Iterator<TemplateVariableResolver> iter = parent.resolvers();
		while (iter.hasNext()) {
			contextType.addResolver(iter.next());
		}
	}

	private synchronized ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
		if (imageDescriptorRegistry == null) {
			imageDescriptorRegistry = new ImageDescriptorRegistry();
		}
		return imageDescriptorRegistry;
	}

}
