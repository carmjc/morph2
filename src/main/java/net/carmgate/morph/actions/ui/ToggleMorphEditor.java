package net.carmgate.morph.actions.ui;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.player.Player.FOF;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;

import org.lwjgl.input.Keyboard;

@ActionHints(keyboardActionAutoload = true)
public class ToggleMorphEditor implements Action {

	@Override
	public void run() {
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.KEYBOARD_UP
				|| lastEvent.getButton() != Keyboard.KEY_E
				|| Model.getModel().getSimpleSelection().size() != 1) {
			return;
		}

		Ship selectedShip = (Ship) Model.getModel().getSimpleSelection().iterator().next();
		if (selectedShip.getPlayer().getFof() == FOF.SELF) {
			Model.getModel().togglePause();
			Model.getModel().toggleMorphEditor();
		}
	}

}
