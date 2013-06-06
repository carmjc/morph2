package net.carmgate.morph.actions;

import java.util.List;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.Event;
import net.carmgate.morph.actions.common.Event.EventType;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.InflictLaserDamage;
import net.carmgate.morph.model.behaviors.common.Movement;
import net.carmgate.morph.model.behaviors.steering.Orbit;
import net.carmgate.morph.model.entities.Planet;
import net.carmgate.morph.model.entities.Star;
import net.carmgate.morph.model.entities.common.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(mouseActionAutoload = true)
public class EnterOrbit implements Action {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(EnterOrbit.class);

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
		Entity target = Model.getModel().getActionSelection().getFirst();
		if (!(target instanceof Star || target instanceof Planet)) {
			return;
		}

		for (Entity selectable : Model.getModel().getSimpleSelection()) {
			if (selectable != target) {

				// Remove existing arrive and combat behaviors
				// TODO We should find a more systematic way of removing existing user triggered behaviors
				selectable.removeBehaviorsByClass(Movement.class);
				selectable.removeBehaviorsByClass(InflictLaserDamage.class);

				// Add new orbit behavior
				selectable.addBehavior(new Orbit(selectable, target, selectable.getPos().distance(target.getPos()) + 20, false));
			}
		}
	}
}
