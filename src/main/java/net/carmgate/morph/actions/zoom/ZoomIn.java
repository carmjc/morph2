package net.carmgate.morph.actions.zoom;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.Event;
import net.carmgate.morph.actions.common.Event.EventType;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.ui.ViewPort;
import net.carmgate.morph.ui.GameMouse;

import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(mouseActionAutoload = true, keyboardActionAutoload = true)
public class ZoomIn implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZoomIn.class);
	private static final float ZOOM_VARIATION = 1.5f;

	@Override
	public void run() {
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if ((lastEvent.getButton() != Keyboard.KEY_UP
				|| lastEvent.getEventType() != EventType.KEYBOARD_UP)
				&& (lastEvent.getEventType() != EventType.MOUSE_WHEEL
				|| lastEvent.getButton() < 0)) {
			return;
		}

		ViewPort viewport = Model.getModel().getViewport();
		viewport.setZoomFactor(viewport.getZoomFactor() * ZOOM_VARIATION);
		Vect3D fromWindowCenterToMouse = new Vect3D(Model.getModel().getWindow().getWidth() / 2 - GameMouse.getX(),
				-Model.getModel().getWindow().getHeight() / 2 + GameMouse.getY(), 0);
		Model.getModel().getViewport().getFocalPoint().add(new Vect3D(fromWindowCenterToMouse).mult(1f / ZOOM_VARIATION)).mult(ZOOM_VARIATION)
				.substract(new Vect3D(fromWindowCenterToMouse).mult(ZOOM_VARIATION));

		LOGGER.debug("Zoom factor: " + viewport.getZoomFactor());

	}

}
