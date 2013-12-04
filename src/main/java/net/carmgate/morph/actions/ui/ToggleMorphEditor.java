package net.carmgate.morph.actions.ui;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.UIEvent;
import net.carmgate.morph.actions.common.UIEvent.EventType;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.ui.UIState;

import org.lwjgl.input.Keyboard;

@ActionHints(keyboardActionAutoload = true, uiState = { UIState.NORMAL, UIState.SHIP_EDITOR })
public class ToggleMorphEditor implements Action {

	private boolean alreadyPaused = false;

	@Override
	public void run() {
		UIEvent lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.KEYBOARD_UP
				|| lastEvent.getButton() != Keyboard.KEY_E) {
			return;
		}

		if (Model.getModel().getUiContext().getUiState() == UIState.NORMAL) {
			alreadyPaused = Model.getModel().getUiContext().isPaused();
			if (!alreadyPaused) {
				Model.getModel().getUiContext().togglePaused();
			}
			Model.getModel().getUiContext().setUiState(UIState.SHIP_EDITOR);
		} else {
			if (!alreadyPaused) {
				Model.getModel().getUiContext().togglePaused();
			}
			Model.getModel().getUiContext().setUiState(UIState.NORMAL);
		}
	}

}
