package net.carmgate.morph.actions.ui;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.Event;
import net.carmgate.morph.actions.common.Event.EventType;
import net.carmgate.morph.model.Model;
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

		Entity lockedOnEntity = null;
		if (Model.getModel().getSimpleSelection().size() == 1) {
			lockedOnEntity = Model.getModel().getSimpleSelection().iterator().next();
			// IMPROVE add a else here to notify the user this command should not be used on something else
			// than a ship
		}

		if (Model.getModel().getViewport().getLockedOnEntity() == null
				|| lockedOnEntity != Model.getModel().getViewport().getLockedOnEntity()) {
			Model.getModel().getViewport().setLockedOnEntity(lockedOnEntity);
		} else {
			Model.getModel().getViewport().setLockedOnEntity(null);
		}
	}
}
