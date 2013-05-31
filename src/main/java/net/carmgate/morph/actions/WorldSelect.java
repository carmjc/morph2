package net.carmgate.morph.actions;

import java.nio.IntBuffer;
import java.util.List;

import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.Event;
import net.carmgate.morph.actions.common.Event.EventType;
import net.carmgate.morph.actions.common.SelectionType;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityHints;
import net.carmgate.morph.model.ui.Window;
import net.carmgate.morph.ui.common.RenderingSteps;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints
public class WorldSelect implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorldSelect.class);

	public WorldSelect() {
	}

	/** 
	 * Renders the scene for selection.
	 * Can also be used directly for debugging purposes to show the pickable areas.
	 * @param zoomFactor
	 * @param glMode
	 */
	public void render(int glMode) {

		Vect3D focalPoint = Model.getModel().getViewport().getFocalPoint();
		float zoomFactor = Model.getModel().getViewport().getZoomFactor();
		GL11.glTranslatef(-focalPoint.x, -focalPoint.y, -focalPoint.z);
		GL11.glRotatef(Model.getModel().getViewport().getRotation(), 0, 0, 1);
		GL11.glScalef(zoomFactor, zoomFactor, 1);

		// In select mode, we render the model elements in reverse order, because, the first items drawn will
		// be the first picked
		for (RenderingSteps renderingStep : RenderingSteps.values()) {
			if (Model.getModel().getEntitiesByRenderingType(renderingStep) != null) {
				for (Entity entity : Model.getModel().getEntitiesByRenderingType(renderingStep).values()) {
					GL11.glPushName(entity.getClass().getAnnotation(EntityHints.class).entityType().ordinal());
					GL11.glPushName(entity.getId());
					entity.render(GL11.GL_SELECT);
					GL11.glPopName();
					GL11.glPopName();
				}
			}
		}

		GL11.glScalef(1 / zoomFactor, 1 / zoomFactor, 1);
		GL11.glRotatef(-Model.getModel().getViewport().getRotation(), 0, 0, 1);
		GL11.glTranslatef(focalPoint.x, focalPoint.y, focalPoint.z);
	}

	@Override
	public void run() {
		// Simple selection
		List<Event> lastEvents = Model.getModel().getInteractionStack().getLastEvents(2);
		if (lastEvents.get(1).getEventType() == EventType.MOUSE_BUTTON_DOWN
				&& lastEvents.get(1).getButton() == 0
				&& lastEvents.get(0).getEventType() == EventType.MOUSE_BUTTON_UP
				&& !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {

			// Clear the selection
			Model.getModel().clearSimpleSelection();

			// pick
			select(Mouse.getX() - Model.getModel().getWindow().getWidth() / 2, Mouse.getY() - Model.getModel().getWindow().getHeight() / 2,
					SelectionType.SIMPLE, true);
			LOGGER.debug("New simple selection: " + Model.getModel().getSimpleSelection().toString());
		}

		// Action selection
		if (lastEvents.get(1).getEventType() == EventType.MOUSE_BUTTON_DOWN
				&& lastEvents.get(1).getButton() == 1
				&& lastEvents.get(0).getEventType() == EventType.MOUSE_BUTTON_UP
				&& !Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {

			// Clear the selection
			Model.getModel().clearActionSelection();

			// pick
			select(Mouse.getX() - Model.getModel().getWindow().getWidth() / 2, Mouse.getY() - Model.getModel().getWindow().getHeight() / 2,
					SelectionType.ACTION, true);
			LOGGER.debug("New action selection: " + Model.getModel().getActionSelection().toString());
		}
	}

	/**
	 * Picks model elements.
	 * @param x
	 * @param y
	 * @param onlyOne true if the engine should select a unique model element (first encountered)
	 */
	protected void select(int x, int y, SelectionType selectionType, boolean onlyOne) {

		LOGGER.debug("Picking at " + x + " " + y + "(" + selectionType + ")");

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
		GL11.glOrtho(0, window.getWidth(), 0, -window.getHeight(), 1, -1);
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

		// The picked entity if any
		Entity pickedEntity = null;

		// Iterate over the hits
		for (int i = 0; i < hits; i++) {
			// get the number of names on this part of the stack
			int nbNames = selectBuf.get(selectBufIndex++);

			// jump over the two extremes of the picking z-index range
			selectBufIndex += 2;

			// get the matching element in the model
			Entity entity = Model.getModel().getEntitiesByType(selectBuf.get(selectBufIndex++)).get(selectBuf.get(selectBufIndex++));
			if (!entity.getClass().getAnnotation(EntityHints.class).selectable()) {
				continue;
			}

			// if we were asked a unique selection, clear the selection before adding the new selected element
			if (onlyOne) {
				pickedEntity = entity;
			} else {
				if (selectionType == SelectionType.SIMPLE) {
					Model.getModel().getSimpleSelection().add(entity);
					entity.setSelected(true);
				} else {
					Model.getModel().getActionSelection().add(entity);
				}
			}

			// Jump over the other ones if needed
			for (int j = 2; j < nbNames; j++) {
				selectBufIndex++;
			}
		}

		if (onlyOne && pickedEntity != null) {
			if (selectionType == SelectionType.SIMPLE) {
				Model.getModel().getSimpleSelection().add(pickedEntity);
				pickedEntity.setSelected(true);
			} else {
				Model.getModel().getActionSelection().add(pickedEntity);
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
