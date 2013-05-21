package net.carmgate.morph.actions.zoom;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;

import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(mouseActionAutoload = true, keyboardActionAutoload = true)
public class ZoomOut implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZoomOut.class);

	@Override
	public void run() {
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if ((lastEvent.getButton() != Keyboard.KEY_DOWN
				|| lastEvent.getEventType() != EventType.KEYBOARD_UP)
				&& (lastEvent.getEventType() != EventType.MOUSE_WHEEL
				|| lastEvent.getButton() < 0)) {
			return;
		}

		ViewPort viewport = Model.getModel().getViewport();
		viewport.setZoomFactor(viewport.getZoomFactor() / 2);

		LOGGER.debug("Zoom factor: " + viewport.getZoomFactor());
	}

}
