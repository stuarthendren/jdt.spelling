package jspell;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public abstract class JSpellPluginImages {

	public static final String MARKER = "marker.gif";
	public static final String HELP_ICON = "help.gif";
	public static final String CORRECT = "correct.gif";

	public static Image getImage(String key) {

		ImageRegistry imageRegistry = JSpellPlugin.getDefault().getImageRegistry();
		Image image = imageRegistry.get(key);
		if (image != null) {
			return image;
		}
		imageRegistry.put(key, JSpellPlugin.imageDescriptorFromPlugin(key));
		return imageRegistry.get(key);
	}
}
