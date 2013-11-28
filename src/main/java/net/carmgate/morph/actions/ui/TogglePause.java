package net.carmgate.morph.actions.ui;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.UIEvent;
import net.carmgate.morph.actions.common.UIEvent.EventType;
import net.carmgate.morph.model.Model;

import org.lwjgl.input.Keyboard;

@ActionHints(keyboardActionAutoload = true)
public class TogglePause implements Action {

	@Override
	public void run() {
		UIEvent lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.KEYBOARD_UP
				|| lastEvent.getButton() != Keyboard.KEY_P) {
			return;
		}

		Model.getModel().getUiContext().togglePaused();
	}

}
