package net.carmgate.morph.actions.ui;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.Event;
import net.carmgate.morph.actions.common.Event.EventType;
import net.carmgate.morph.model.Model;

import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(keyboardActionAutoload = true)
public class ToggleSelectViewMode implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(ToggleSelectViewMode.class);

	@Override
	public void run() {
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.KEYBOARD_UP
				|| lastEvent.getButton() != Keyboard.KEY_S
				|| !Model.getModel().getUiContext().isDebugMode()) {
			return;
		}

		LOGGER.debug("Toggle debug rendering mode");
		Model.getModel().getUiContext().toggleSelectViewMode();

	}

}
