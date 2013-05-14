package net.carmgate.morph.uihandler;

import java.nio.IntBuffer;
import java.util.Map;

import net.carmgate.morph.Main;
import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.entities.Entity;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.ui.GameMouse;
import net.carmgate.morph.ui.renderer.Renderer.RenderingType;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Select implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Select.class);

	/**
	 * Picks model elements.
	 * @param x
	 * @param y
	 */
	public void pick(int x, int y) {

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
		// TODO use model window width and height instead of conf values
		GLU.gluOrtho2D(0, Conf.getIntProperty("window.initialWidth"), 0, Conf.getIntProperty("window.initialHeight"));

		// TODO Replace this with more standard world rendering
		Map<Integer, Ship> shipsMap = Model.getModel().getEntityMap(Ship.class.getAnnotation(Entity.class).uniqueId());
		for (Ship ship : shipsMap.values()) {
			Main.entityServicesMap.get(Ship.class).getRenderer().render(GL11.GL_SELECT, RenderingType.NORMAL, ship);
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
		LOGGER.debug("hits: " + hits + ", result : " + result + "]");

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
			Model.getModel().getSelection().add(Model.getModel().getEntities().get(selectBuf.get(selectBufIndex++)).get(selectBuf.get(selectBufIndex++)));

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

	@Override
	public void run() {
		Model.getModel().getSelection().clear();
		pick(GameMouse.getXInWorld(), GameMouse.getYInWorld());
		LOGGER.debug(Model.getModel().getSelection().toString());
	}

}
