package net.carmgate.morph.actions;

import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.entities.Entity;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;
import net.carmgate.morph.ui.GameMouse;
import net.carmgate.morph.ui.Renderable.RenderingType;
import net.carmgate.morph.ui.Selectable;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Select implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(Select.class);

	public Select() {
	}

	@Override
	public void run() {
		// TODO remove these sort of things by filling the stack at init with NOOP interactions
		if (Model.getModel().getInteractionStack().size() < 2) {
			return;
		}

		List<Event> lastEvents = Model.getModel().getInteractionStack().getLastEvents(2);
		if (lastEvents.get(1).getEventType() != EventType.MOUSE_BUTTON_DOWN
				|| lastEvents.get(1).getButton() != 0
				|| lastEvents.get(0).getEventType() != EventType.MOUSE_BUTTON_UP) {
			return;
		}

		// Clear the selection
		// TODO find a way to do it more safely. We have to know that the clear must not be done alone, that's not safe.
		Model.getModel().clearSelection();

		// pick
		select(GameMouse.getXInWorld(), GameMouse.getYInWorld(), true);
		LOGGER.debug(Model.getModel().getSelection().toString());
	}

	/**
	 * Picks model elements.
	 * @param x
	 * @param y
	 * @param onlyOne true if the engine should select a unique model element (first encountered)
	 */
	public void select(int x, int y, boolean onlyOne) {

		// LOGGER.debug("Picking at " + x + " " + y);

		// get viewport
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

		IntBuffer selectBuf = BufferUtils.createIntBuffer(512);
		GL11.glSelectBuffer(selectBuf);
		GL11.glRenderMode(GL11.GL_SELECT);

		GL11.glInitNames();

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glLoadIdentity();

		GLU.gluPickMatrix(x, y, 6.0f, 6.0f, viewport);
		// TODO use model window width and height instead of conf values
		GLU.gluOrtho2D(0, Conf.getIntProperty("window.initialWidth"), 0, Conf.getIntProperty("window.initialHeight"));

		// TODO Replace this with more standard world rendering
		Map<Integer, Ship> shipsMap = Model.getModel().getEntityMap(Ship.class.getAnnotation(Entity.class).uniqueId());
		for (Ship ship : shipsMap.values()) {
			ship.render(GL11.GL_SELECT, RenderingType.NORMAL);
		}

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glFlush();

		int hits = GL11.glRenderMode(GL11.GL_RENDER);

		if (hits == 0) {
			// TODO Unselect if needed
		}

		// For debugging purpose only ...
		// This allows to see the select buffer
		String result = "[";
		for (int i = 0; i < selectBuf.capacity(); i++)
		{
			result += selectBuf.get(i) + ", ";
		}
		LOGGER.info("hits: " + hits + ", result : " + result + "]");

		// Get the model elements picked
		// The current index we are looking for in the select buffer
		int selectBufIndex = 0;
		// Iterate over the hits
		for (int i = 0; i < hits; i++) {
			// get the number of names on this part of the stack
			int nbNames = selectBuf.get(selectBufIndex++);

			// jump over the two extremes of the picking z-index range
			selectBufIndex += 2;

			// get the matching element in the model
			Object selectedObject = Model.getModel().getEntities().get(selectBuf.get(selectBufIndex++)).get(selectBuf.get(selectBufIndex++));
			if (selectedObject instanceof Selectable) {
				Selectable selectable = (Selectable) selectedObject;
				selectable.setSelected(true);

				// if we were asked a unique selection, clear the selection before adding the new selected element
				if (onlyOne) {
					Model.getModel().clearSelection();
					Model.getModel().getSelection().add(selectable);
				} else {
					Model.getModel().getSelection().add(selectable);
				}
			}

			// Jump over the other ones if needed
			for (int j = 2; j < nbNames; j++) {
				selectBufIndex++;
			}

		}
		// int j = 0;
		// Ship lastSelectedShip = globalModel.getSelectedShip();
		// int index = selectBuf.get(j + 4);
		// globalModel.setSelectedShip(index);
		// if (lastSelectedShip != null && lastSelectedShip ==
		// globalModel.getSelectedShip()) {
		// globalModel.getSelectedShip().toggleSelectedMorph(selectBuf.get(j + 5));
		// }
	}

}
