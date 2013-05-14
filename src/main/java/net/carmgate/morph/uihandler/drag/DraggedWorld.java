package net.carmgate.morph.uihandler.drag;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.uihandler.Action;

public class DraggedWorld implements Action {

	private final DragContext dragContext;

	public DraggedWorld(DragContext dragContext) {
		this.dragContext = dragContext;

	}

	@Override
	public void run(Event event) {
		dragContext.reset();
		Model.getModel().getUIContext().getEventQueue().clear();
	}

}
