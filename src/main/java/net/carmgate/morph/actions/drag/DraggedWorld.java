package net.carmgate.morph.actions.drag;

import java.util.List;

import net.carmgate.morph.actions.Action;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;

public class DraggedWorld implements Action {

	private final DragContext dragContext;

	public DraggedWorld(DragContext dragContext) {
		this.dragContext = dragContext;

	}

	@Override
	public void run() {
		if (Model.getModel().getInteractionStack().size() < 3) {
			return;
		}

		List<Event> lastEvents = Model.getModel().getInteractionStack().getLastEvents(3);
		if (lastEvents.get(2).getEventType() != EventType.MOUSE_BUTTON_DOWN
				|| lastEvents.get(2).getButton() != 0
				|| lastEvents.get(1).getEventType() != EventType.MOUSE_MOVE
				|| lastEvents.get(0).getEventType() != EventType.MOUSE_BUTTON_UP) {
			return;
		}

		dragContext.reset();
	}

}
