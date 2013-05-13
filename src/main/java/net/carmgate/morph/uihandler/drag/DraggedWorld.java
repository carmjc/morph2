package net.carmgate.morph.uihandler.drag;

import net.carmgate.morph.model.GlobalModel;

public class DraggedWorld implements Runnable {

	private final DragContext dragContext;

	public DraggedWorld(DragContext dragContext) {
		this.dragContext = dragContext;

	}

	@Override
	public void run() {
		dragContext.reset();
		GlobalModel.getModel().getUIContext().getEventQueue().clear();
	}

}
