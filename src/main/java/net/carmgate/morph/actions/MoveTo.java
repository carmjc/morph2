package net.carmgate.morph.actions;

import java.util.List;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Selectable;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;
import net.carmgate.morph.ui.GameMouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(mouseActionAutoload = true)
public class MoveTo implements Action {

	private static Logger LOGGER = LoggerFactory.getLogger(MoveTo.class);

	@Override
	public void run() {
		List<Event> lastEvents = Model.getModel().getInteractionStack().getLastEvents(2);
		// LOGGER.debug("empty : " + Model.getModel().getActionSelection().isEmpty());
		if (lastEvents.get(0).getEventType() != EventType.MOUSE_BUTTON_UP
				|| lastEvents.get(0).getButton() != 1
				|| lastEvents.get(1).getEventType() != EventType.MOUSE_BUTTON_DOWN
				|| !Model.getModel().getActionSelection().isEmpty()) {
			return;
		}

		for (Selectable selectable : Model.getModel().getSimpleSelection()) {
			if (selectable instanceof Ship) {
				Vect3D target = new Vect3D(GameMouse.getXInWorld(), GameMouse.getYInWorld(), 0);
				((Ship) selectable).movement.setTarget(target);
				LOGGER.debug("target set to " + target);
			}
		}
	}

}
