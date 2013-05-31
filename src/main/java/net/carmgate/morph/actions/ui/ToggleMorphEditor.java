package net.carmgate.morph.actions.ui;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.UiContext;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;

import org.lwjgl.input.Keyboard;

@ActionHints(keyboardActionAutoload = true, uiContext = { UiContext.NORMAL, UiContext.SHIP_EDITOR })
public class ToggleMorphEditor implements Action {

	private boolean alreadyPaused = false;

	@Override
	public void run() {
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.KEYBOARD_UP
				|| lastEvent.getButton() != Keyboard.KEY_E) {
			return;
		}

		if (Model.getModel().getUiContext() == UiContext.NORMAL) {
			alreadyPaused = Model.getModel().isPaused();
			if (!alreadyPaused) {
				Model.getModel().togglePaused();
			}
			Model.getModel().setUiContext(UiContext.SHIP_EDITOR);
		} else {
			if (!alreadyPaused) {
				Model.getModel().togglePaused();
			}
			Model.getModel().setUiContext(UiContext.NORMAL);
		}
	}

}
