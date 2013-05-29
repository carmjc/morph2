package net.carmgate.morph.actions;

import java.util.List;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.FollowAndInflictDamage;
import net.carmgate.morph.model.behaviors.steering.Arrive;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Selectable;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(mouseActionAutoload = true)
public class Attack implements Action {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(Attack.class);

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
				ship.removeBehaviorsByClass(Arrive.class);
				ship.removeBehaviorsByClass(FollowAndInflictDamage.class);

				// Add new arrive behavior
				ship.addBehavior(new Arrive((Ship) selectable, (Ship) targetShip));

				// Add new combat behavior
				ship.addBehavior(new FollowAndInflictDamage(ship, (Ship) targetShip));
			}
		}
	}
}
