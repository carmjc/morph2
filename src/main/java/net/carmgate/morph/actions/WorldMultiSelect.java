package net.carmgate.morph.actions;

import java.util.List;

import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.Event;
import net.carmgate.morph.actions.common.SelectionType;
import net.carmgate.morph.actions.common.Event.EventType;
import net.carmgate.morph.model.Model;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints
public class WorldMultiSelect extends WorldSelect {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorldMultiSelect.class);

	public WorldMultiSelect() {
	}

	@Override
	public void run() {
		List<Event> lastEvents = Model.getModel().getInteractionStack().getLastEvents(2);
		if (lastEvents.get(1).getEventType() != EventType.MOUSE_BUTTON_DOWN
				|| lastEvents.get(1).getButton() != 0
				|| lastEvents.get(0).getEventType() != EventType.MOUSE_BUTTON_UP
				|| !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			return;
		}

		// pick
		select(Mouse.getX() - Model.getModel().getWindow().getWidth() / 2, Mouse.getY() - Model.getModel().getWindow().getHeight() / 2, SelectionType.SIMPLE,
				true);
		LOGGER.debug(Model.getModel().getSimpleSelection().toString());
	}
}
