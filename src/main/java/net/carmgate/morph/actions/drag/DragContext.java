package net.carmgate.morph.actions.drag;

import net.carmgate.morph.actions.common.UIEvent;
import net.carmgate.morph.model.common.Vect3D;

public class DragContext {
	private Vect3D oldFP;
	private Vect3D oldMousePosInWindow;

	public Vect3D getOldFP() {
		return oldFP;
	}

	public Vect3D getOldMousePosInWindow() {
		return oldMousePosInWindow;
	}

	public boolean isDragging(UIEvent event) {
		return oldMousePosInWindow != null
				&& (event.getPositionInWindow()[0] != oldMousePosInWindow.x || event.getPositionInWindow()[1] != oldMousePosInWindow.y);
	}

	public void reset() {
		oldFP = null;
		oldMousePosInWindow = null;
	}

	public void setOldFP(Vect3D oldFP) {
		this.oldFP = oldFP;
	}

	public void setOldMousePosInWindow(Vect3D oldMousePosInWindow) {
		this.oldMousePosInWindow = oldMousePosInWindow;
	}
}
