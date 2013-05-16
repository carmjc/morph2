package net.carmgate.morph.actions;

import java.nio.IntBuffer;
import java.util.List;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Entity;
import net.carmgate.morph.model.view.Window;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;
import net.carmgate.morph.ui.Renderable;
import net.carmgate.morph.ui.Renderable.RenderingType;
import net.carmgate.morph.ui.Selectable;
import net.carmgate.morph.ui.rendering.RenderingSteps;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Select implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(Select.class);

	public Select() {
	}

	/** 
	 * Renders the scene for selection.
	 * Can be used directly for debugging purposes to show the pickable areas.
	 * @param zoomFactor
	 * @param glMode
	 */
	public void render(int glMode) {

		Vect3D focalPoint = Model.getModel().getViewport().getFocalPoint();
		float zoomFactor = Model.getModel().getViewport().getZoomFactor();
		GL11.glTranslatef(focalPoint.x, focalPoint.y, focalPoint.z);
		GL11.glRotatef(Model.getModel().getViewport().getRotation(), 0, 0, 1);
		GL11.glScalef(zoomFactor, zoomFactor, 1);

		// In select mode, we render the model elements in reverse order, because, the first items drawn will
		// be the first picked
		for (RenderingSteps renderingStep : RenderingSteps.reverseValues()) {
			for (Renderable renderable : Model.getModel().getEntitiesByRenderingType(renderingStep).values()) {
				GL11.glPushName(renderable.getClass().getAnnotation(Entity.class).entityType().ordinal());
				GL11.glPushName(((Selectable) renderable).getSelectionId());
				renderable.render(GL11.GL_SELECT, RenderingType.NORMAL);
				GL11.glPopName();
				GL11.glPopName();
			}
		}

		GL11.glScalef(1 / zoomFactor, 1 / zoomFactor, 1);
		GL11.glRotatef(-Model.getModel().getViewport().getRotation(), 0, 0, 1);
		GL11.glTranslatef(-focalPoint.x, -focalPoint.y, -focalPoint.z);
	}

	@Override
	public void run() {
		List<Event> lastEvents = Model.getModel().getInteractionStack().getLastEvents(2);
		if (lastEvents.get(1).getEventType() != EventType.MOUSE_BUTTON_DOWN
				|| lastEvents.get(1).getButton() != 0
				|| lastEvents.get(0).getEventType() != EventType.MOUSE_BUTTON_UP) {
			return;
		}

		// Clear the selection
		Model.getModel().clearSelection();

		// pick
		select(Mouse.getX() - Model.getModel().getWindow().getWidth() / 2, Mouse.getY() - Model.getModel().getWindow().getHeight() / 2, true);
		LOGGER.debug(Model.getModel().getSelection().toString());
	}

	/**
	 * Picks model elements.
	 * @param x
	 * @param y
	 * @param onlyOne true if the engine should select a unique model element (first encountered)
	 */
	public void select(int x, int y, boolean onlyOne) {

		LOGGER.debug("Picking at " + x + " " + y);

		float zoomFactor = Model.getModel().getViewport().getZoomFactor();

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
		Window window = Model.getModel().getWindow();
		// GLU.gluOrtho2D(0, Conf.getIntProperty("window.initialWidth"), 0, Conf.getIntProperty("window.initialHeight"));
		GL11.glOrtho(0, window.getWidth(), 0, window.getHeight(), 1, -1);
		GL11.glViewport(0, 0, window.getWidth(), window.getHeight());

		render(GL11.GL_SELECT);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glFlush();

		int hits = GL11.glRenderMode(GL11.GL_RENDER);

		// For debugging purpose only ...
		// This allows to see the select buffer
		String result = "[";
		for (int i = 0; i < selectBuf.capacity(); i++)
		{
			result += selectBuf.get(i) + ", ";
		}
		LOGGER.trace("hits: " + hits + ", result : " + result + "]");

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
			Object selectedObject = Model.getModel().getEntitiesByType(selectBuf.get(selectBufIndex++)).get(selectBuf.get(selectBufIndex++));
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
