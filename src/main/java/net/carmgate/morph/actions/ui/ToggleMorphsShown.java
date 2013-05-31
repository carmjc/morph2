package net.carmgate.morph.actions.ui;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.Event;
import net.carmgate.morph.actions.common.Event.EventType;
import net.carmgate.morph.model.Model;

import org.lwjgl.input.Keyboard;

// IMPROVE Remove this action and replace it with some proper visualization of any ship's features. 
@ActionHints(keyboardActionAutoload = true)
public class ToggleMorphsShown implements Action {

	@Override
	public void run() {
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.KEYBOARD_UP
				|| lastEvent.getButton() != Keyboard.KEY_S) {
			return;
		}

		Model.getModel().getUiContext().toggleMorphsShown();
	}

}
