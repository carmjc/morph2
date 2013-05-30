package net.carmgate.morph.actions;

import java.util.List;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.InflictLaserDamage;
import net.carmgate.morph.model.behaviors.Movement;
import net.carmgate.morph.model.behaviors.steering.Follow;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Selectable;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(mouseActionAutoload = true)
public class FollowAndInflictDamage implements Action {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(FollowAndInflictDamage.class);

	@Override
	public void run() {
		List<Event> lastEvents = Model.getModel().getInteractionStack().getLastEvents(2);
		if (lastEvents.get(0).getEventType() != EventType.MOUSE_BUTTON_UP
				|| lastEvents.get(0).getButton() != 1
				|| lastEvents.get(1).getEventType() != EventType.MOUSE_BUTTON_DOWN
				|| Model.getModel().getActionSelection().isEmpty()
				|| Model.getModel().getSimpleSelection().isEmpty()) {
			return;
		}

		// IMPROVE Clean this : we use a Selectable, when we would need a Ship.
		// Therefore, we have an extraneous cast.
		// However, we might attack something else than ships ...
		Selectable targetShip = Model.getModel().getActionSelection().getFirst();
		for (Selectable selectable : Model.getModel().getSimpleSelection()) {
			if (selectable instanceof Ship && selectable != targetShip) {
				Ship ship = (Ship) selectable;

				// Remove existing arrive and combat behaviors
				ship.removeBehaviorsByClass(Movement.class);
				ship.removeBehaviorsByClass(InflictLaserDamage.class);

				// Add new arrive behavior
				ship.addBehavior(new Follow((Ship) selectable, (Ship) targetShip, InflictLaserDamage.MAX_RANGE * 0.5f));

				// Add new combat behavior
				ship.addBehavior(new InflictLaserDamage(ship, (Ship) targetShip));
			}
		}
	}
}
