package net.carmgate.morph.actions;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.entities.Selectable;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Attack implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(Attack.class);

	@Override
	public void run() {
		// TODO this is not enough.
		// We should check for no mouse move between mouse_down and mouse_up
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.MOUSE_BUTTON_UP
				|| lastEvent.getButton() != 1
				|| Model.getModel().getActionSelection().isEmpty()
				|| Model.getModel().getSimpleSelection().isEmpty()) {
			return;
		}

		// TODO Clean this : we use a Selectable, when we would need a Ship.
		// Therefore, we have an extraneous cast.
		// TODO We should prevent a ship from attacking itself
		Selectable targetShip = Model.getModel().getActionSelection().getFirst();
		for (Selectable selectable : Model.getModel().getSimpleSelection()) {
			if (selectable instanceof Ship) {
				((Ship) selectable).combat.setTarget((Ship) targetShip);
			}
		}
	}
}
