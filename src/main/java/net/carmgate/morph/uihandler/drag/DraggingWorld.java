package net.carmgate.morph.uihandler.drag;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.ui.GameMouse;

import org.lwjgl.input.Mouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DraggingWorld implements Runnable {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(DraggingWorld.class);

	private final DragContext dragContext;

	public DraggingWorld(DragContext dragContext) {
		this.dragContext = dragContext;

	}

	@Override
	public void run() {
		if (dragContext.getOldFP() == null) {
			dragContext.setOldFP(new Vect3D(Model.getModel().getViewport().getFocalPoint()));
			dragContext.setOldMousePosInWindow(new Vect3D(GameMouse.getX(), GameMouse.getY(), 0));
		}

		Vect3D oldFP = dragContext.getOldFP();
		Vect3D oldMousePosInWindow = dragContext.getOldMousePosInWindow();
		if (oldFP != null) {
			ViewPort viewport = Model.getModel().getViewport();
			Vect3D fp = viewport.getFocalPoint();
			fp.x = oldFP.x + (Mouse.getX() - oldMousePosInWindow.x) / Model.getModel().getViewport().getZoomFactor();
			fp.y = oldFP.y - (Mouse.getY() - oldMousePosInWindow.y) / Model.getModel().getViewport().getZoomFactor();
		}
	}
}
