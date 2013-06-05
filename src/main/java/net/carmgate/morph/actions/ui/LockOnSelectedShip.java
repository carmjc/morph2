package net.carmgate.morph.actions.ui;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.Event;
import net.carmgate.morph.actions.common.Event.EventType;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Entity;

import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(keyboardActionAutoload = true)
public class LockOnSelectedShip implements Action {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(LockOnSelectedShip.class);

	@Override
	public void run() {
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.KEYBOARD_UP
				|| lastEvent.getButton() != Keyboard.KEY_L) {
			return;
		}

		Entity lockedOnShip = null;
		if (Model.getModel().getSimpleSelection().size() == 1) {
			Entity entity = Model.getModel().getSimpleSelection().iterator().next();
			if (entity instanceof Ship) {
				lockedOnShip = entity;
			}
			// IMPROVE add a else here to notify the user this command should not be used on something else
			// than a ship
		}

		if (Model.getModel().getViewport().getLockedOnShip() == null
				|| lockedOnShip != Model.getModel().getViewport().getLockedOnShip()) {
			Model.getModel().getViewport().setLockedOnShip(lockedOnShip);
		} else {
			Model.getModel().getViewport().setLockedOnShip(null);
		}
	}
}
