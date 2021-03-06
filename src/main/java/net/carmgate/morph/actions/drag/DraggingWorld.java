package net.carmgate.morph.actions.drag;

import java.util.List;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.UIEvent;
import net.carmgate.morph.actions.common.UIEvent.EventType;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.ui.GameMouse;
import net.carmgate.morph.ui.ViewPort;

import org.lwjgl.input.Mouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(dragAction = true, mouseActionAutoload = true)
public class DraggingWorld implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(DraggingWorld.class);

	private DragContext dragContext;

	@Override
	public void run() {
		List<UIEvent> lastEvents = Model.getModel().getInteractionStack().getLastEvents(2);
		if (lastEvents.get(1).getEventType() != EventType.MOUSE_BUTTON_DOWN
				|| lastEvents.get(1).getButton() != 0
				|| lastEvents.get(0).getEventType() != EventType.MOUSE_MOVE) {
			return;
		}

		Model.getModel().getViewport().setLockedOnEntity(null);

		if (dragContext.getOldFP() == null) {
			dragContext.setOldFP(new Vect3D(Model.getModel().getViewport().getFocalPoint()));
			dragContext.setOldMousePosInWindow(new Vect3D(GameMouse.getX(), GameMouse.getY(), 0));
		}

		Vect3D oldFP = dragContext.getOldFP();
		Vect3D oldMousePosInWindow = dragContext.getOldMousePosInWindow();
		if (oldFP != null) {
			ViewPort viewport = Model.getModel().getViewport();
			Vect3D fp = viewport.getFocalPoint();
			fp.x = oldFP.x - (Mouse.getX() - oldMousePosInWindow.x);// / Model.getModel().getViewport().getZoomFactor();
			fp.y = oldFP.y + (Mouse.getY() - oldMousePosInWindow.y);// / Model.getModel().getViewport().getZoomFactor();
		}

		LOGGER.debug(Model.getModel().getViewport().getFocalPoint() + " - " + GameMouse.getPosInWord());
	}

}
