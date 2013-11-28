package net.carmgate.morph.actions.ui;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.UIEvent;
import net.carmgate.morph.actions.common.UIEvent.EventType;
import net.carmgate.morph.model.Model;

import org.lwjgl.input.Keyboard;

// IMPROVE Remove this action and replace it with some proper visualization of any ship's features.
/**
 * This action is for debugging purpose only.
 */
@ActionHints(keyboardActionAutoload = true)
public class ToggleDebugMorphsShown implements Action {

	@Override
	public void run() {
		UIEvent lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.KEYBOARD_UP
				|| lastEvent.getButton() != Keyboard.KEY_M
				|| !Model.getModel().getUiContext().isDebugMode()) {
			return;
		}

		Model.getModel().getUiContext().toggleDebugMorphsShown();
	}

}
