package net.carmgate.morph.actions;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Selectable;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;
import net.carmgate.morph.ui.GameMouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveTo implements Action {

	private static Logger LOGGER = LoggerFactory.getLogger(MoveTo.class);

	@Override
	public void run() {
		Event lastEvent = Model.getModel().getInteractionStack().getLastEvent();
		if (lastEvent.getEventType() != EventType.MOUSE_BUTTON_UP
				|| lastEvent.getButton() != 1) {
			return;
		}

		for (Selectable selectable : Model.getModel().getSelection()) {
			if (selectable instanceof Ship) {
				Vect3D target = new Vect3D(GameMouse.getXInWorld(), GameMouse.getYInWorld(), 0);
				((Ship) selectable).setTarget(target);
				LOGGER.debug("target set to " + target);
			}
		}
	}

}
