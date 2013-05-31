package net.carmgate.morph.actions;

import java.nio.IntBuffer;
import java.util.List;

import net.carmgate.morph.Main;
import net.carmgate.morph.actions.common.Action;
import net.carmgate.morph.actions.common.ActionHints;
import net.carmgate.morph.actions.common.Event;
import net.carmgate.morph.actions.common.Event.EventType;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.UiState;
import net.carmgate.morph.model.entities.Morph;
import net.carmgate.morph.model.ui.Window;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ActionHints(uiState = UiState.SHIP_EDITOR)
public class ShipEditorSelect implements Action {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShipEditorSelect.class);

	public ShipEditorSelect() {
	}

	@Override
	public void run() {
		// Simple selection
		List<Event> lastEvents = Model.getModel().getInteractionStack().getLastEvents(2);
		if (lastEvents.get(1).getEventType() == EventType.MOUSE_BUTTON_DOWN
				&& lastEvents.get(1).getButton() == 0
				&& lastEvents.get(0).getEventType() == EventType.MOUSE_BUTTON_UP) {

			// Clear the selection
			Model.getModel().clearMorphSelection();

			// pick
			select(Mouse.getX() - Model.getModel().getWindow().getWidth() / 2, Mouse.getY() - Model.getModel().getWindow().getHeight() / 2,
					true);
			LOGGER.debug("New morph selection: " + Model.getModel().getMorphSelection().toString());
		}

	}

	/**
	 * Picks model elements.
	 * @param x
	 * @param y
	 * @param onlyOne true if the engine should select a unique model element (first encountered)
	 */
	protected void select(int x, int y, boolean onlyOne) {

		LOGGER.debug("Picking at " + x + " " + y);

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

		Main.shipEditorRender(Model.getModel().getSelfShip(), GL11.GL_SELECT);

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
		Morph pickedMorph = null;

		// Iterate over the hits
		for (int i = 0; i < hits; i++) {
			// get the number of names on this part of the stack
			int nbNames = selectBuf.get(selectBufIndex++);

			// jump over the two extremes of the picking z-index range
			selectBufIndex += 2;

			// get the matching element in the model
			pickedMorph = Model.getModel().getSelfShip().getMorphById(selectBuf.get(selectBufIndex++));

			// Jump over the other ones if needed
			for (int j = 2; j < nbNames; j++) {
				selectBufIndex++;
			}
		}

		// Really select the morph
		if (pickedMorph != null) {
			pickedMorph.setSelected(true);
			Model.getModel().getMorphSelection().add(pickedMorph);
		}

	}
}
