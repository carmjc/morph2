package net.carmgate.morph.actions.zoom;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.UIEvent;
import net.carmgate.morph.actions.common.UIEvent.EventType;
import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.conf.Conf.ConfItem;
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
	private static final float ZOOM_VARIATION = Conf.getFloatProperty(ConfItem.ZOOM_VARIATIONFACTOR);
	private static final float ZOOM_MAX = Conf.getFloatProperty(ConfItem.ZOOM_MAX);

	@Override
	public void run() {
		UIEvent lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if ((lastEvent.getButton() != Keyboard.KEY_UP
				|| lastEvent.getEventType() != EventType.KEYBOARD_UP)
				&& (lastEvent.getEventType() != EventType.MOUSE_WHEEL
				|| lastEvent.getButton() < 0)) {
			return;
		}

		ViewPort viewport = Model.getModel().getViewport();

		// Correct max zoom level
		float zoomVariation = ZOOM_VARIATION;
		if (viewport.getZoomFactor() * zoomVariation > ZOOM_MAX) {
			zoomVariation = ZOOM_MAX / viewport.getZoomFactor();
		}

		viewport.setZoomFactor(viewport.getZoomFactor() * zoomVariation);
		Vect3D fromWindowCenterToMouse = new Vect3D(Model.getModel().getWindow().getWidth() / 2 - GameMouse.getX(),
				-Model.getModel().getWindow().getHeight() / 2 + GameMouse.getY(), 0);
		Model.getModel().getViewport().getFocalPoint().add(new Vect3D(fromWindowCenterToMouse).mult(1f / zoomVariation)).mult(zoomVariation)
		.substract(new Vect3D(fromWindowCenterToMouse).mult(zoomVariation));

		LOGGER.debug("Zoom factor: " + viewport.getZoomFactor());

	}

}
