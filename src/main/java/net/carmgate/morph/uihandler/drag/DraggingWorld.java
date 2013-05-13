package net.carmgate.morph.uihandler.drag;

import net.carmgate.morph.model.GlobalModel;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.ui.GameMouse;

import org.lwjgl.input.Mouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DraggingWorld implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(DraggingWorld.class);

	private final DragContext dragContext;

	public DraggingWorld(DragContext dragContext) {
		this.dragContext = dragContext;

	}

	@Override
	public void run() {
		LOGGER.debug("huh");

		if (dragContext.getOldFP() == null) {
			dragContext.setOldFP(new Vect3D(GlobalModel.getModel().getViewport().getFocalPoint()));
			dragContext.setOldMousePosInWindow(new Vect3D(GameMouse.getX(), GameMouse.getY(), 0));
		}

		Vect3D oldFP = dragContext.getOldFP();
		Vect3D oldMousePosInWindow = dragContext.getOldMousePosInWindow();
		if (oldFP != null) {
			ViewPort viewport = GlobalModel.getModel().getViewport();
			Vect3D fp = viewport.getFocalPoint();
			fp.x = oldFP.x + (Mouse.getX() - oldMousePosInWindow.x) / GlobalModel.getModel().getViewport().getZoomFactor();
			fp.y = oldFP.y - (Mouse.getY() - oldMousePosInWindow.y) / GlobalModel.getModel().getViewport().getZoomFactor();
		}
	}
}
