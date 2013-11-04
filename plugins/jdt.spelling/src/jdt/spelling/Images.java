package jdt.spelling;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public abstract class Images {

	public static final String MARKER = "marker.gif";
	public static final String HELP_ICON = "help.gif";
	public static final String CORRECT = "correct.gif";
	public static final String ADD = "add.gif";
	public static final String REMOVE = "remove.gif";

	public static Image getImage(String key) {

		ImageRegistry imageRegistry = Plugin.getDefault().getImageRegistry();
		Image image = imageRegistry.get(key);
		if (image != null) {
			return image;
		}
		imageRegistry.put(key, Plugin.imageDescriptorFromPlugin(key));
		return imageRegistry.get(key);
	}
}
