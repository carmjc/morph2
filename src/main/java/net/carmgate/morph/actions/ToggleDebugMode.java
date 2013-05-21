package net.carmgate.morph.actions;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;

import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(keyboardActionAutoload = true)
public class ToggleDebugMode implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(ToggleDebugMode.class);

	@Override
	public void run() {
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.KEYBOARD_UP
				|| lastEvent.getButton() != Keyboard.KEY_D) {
			return;
		}

		LOGGER.debug("Toggle debug rendering mode");
		Model.getModel().toggleDebugMode();

	}

}
