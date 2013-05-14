package net.carmgate.morph.actions.drag;

import net.carmgate.morph.actions.Activable;
import net.carmgate.morph.model.Model;

public class DraggedWorld implements Activable {

	private final DragContext dragContext;

	public DraggedWorld(DragContext dragContext) {
		this.dragContext = dragContext;

	}

	@Override
	public void run() {
		dragContext.reset();
		Model.getModel().getUIContext().getEventQueue().clear();
	}

}
