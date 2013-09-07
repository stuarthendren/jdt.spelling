package jspell.marker;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IMarkerUpdater;

public class JSpellMarkerUpdater implements IMarkerUpdater {

	@Override
	public String getMarkerType() {
		return JSpellMarkerFactory.JSPELL_MARKER;
	}

	@Override
	public String[] getAttribute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean updateMarker(IMarker marker, IDocument document, Position position) {
		// TODO Auto-generated method stub
		return true;
	}

}
