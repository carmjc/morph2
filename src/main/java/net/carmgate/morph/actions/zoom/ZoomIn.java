package net.carmgate.morph.actions.zoom;

import net.carmgate.morph.actions.Action;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;

import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoomIn implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZoomIn.class);

	@Override
	public void run() {
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getButton() != Keyboard.KEY_UP
				|| lastEvent.getEventType() != EventType.KEYBOARD_UP) {
			return;
		}

		ViewPort viewport = Model.getModel().getViewport();
		// viewport.setZoomFactor(viewport.getZoomFactor() * 2);
		Model.getModel().setPause();

		LOGGER.debug("Zoom factor: " + viewport.getZoomFactor());

	}

}
