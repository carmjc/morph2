package net.carmgate.morph.actions.ui;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.UIEvent;
import net.carmgate.morph.actions.common.UIEvent.EventType;
import net.carmgate.morph.model.Model;

import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(keyboardActionAutoload = true)
public class ToggleDebugSelectViewMode implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(ToggleDebugSelectViewMode.class);

	@Override
	public void run() {
		UIEvent lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.KEYBOARD_UP
				|| lastEvent.getButton() != Keyboard.KEY_S
				|| !Model.getModel().getUiContext().isDebugMode()) {
			return;
		}

		LOGGER.debug("Toggle the rendering to selection mode");
		Model.getModel().getUiContext().toggleDebugSelectViewMode();

	}

}
