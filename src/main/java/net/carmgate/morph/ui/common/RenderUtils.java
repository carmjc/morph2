package net.carmgate.morph.ui.common;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;

public class RenderUtils {

	/**
	 * Renders a gauge at the given position
	 * @param width
	 * @param yGaugePosition
	 * @param percentage
	 * @param alarmThreshold
	 * @param color
	 */
	public static void renderGauge(float width, float yGaugePosition, float percentage, float alarmThreshold, float[] color) {

		TextureImpl.bindNone();
		GL11.glColor4f(0.5f, 0.5f, 0.5f, 10);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(width / 2 + 2, yGaugePosition - 5);
		GL11.glVertex2f(width / 2 + 2, yGaugePosition + 5);
		GL11.glVertex2f(-(width / 2 + 2), yGaugePosition + 5);
		GL11.glVertex2f(-(width / 2 + 2), yGaugePosition - 5);
		GL11.glEnd();

		GL11.glColor4f(0, 0, 0, 1);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(width / 2 + 1, yGaugePosition - 4);
		GL11.glVertex2f(width / 2 + 1, yGaugePosition + 4);
		GL11.glVertex2f(-(width / 2 + 1), yGaugePosition + 4);
		GL11.glVertex2f(-(width / 2 + 1), yGaugePosition - 4);
		GL11.glEnd();

		if (percentage < alarmThreshold) {
			GL11.glColor4f(1, 0.5f, 0.5f, 0.5f);
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(width / 2, yGaugePosition - 3);
			GL11.glVertex2f(width / 2, yGaugePosition + 3);
			GL11.glVertex2f(-width / 2, yGaugePosition + 3);
			GL11.glVertex2f(-width / 2, yGaugePosition - 3);
			GL11.glEnd();
		}

		GL11.glColor4f(color[0], color[1], color[2], color[3]);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glVertex2f(-width / 2 + percentage * width / 2 * 2, yGaugePosition - 3);
		GL11.glVertex2f(-width / 2 + percentage * width / 2 * 2, yGaugePosition + 3);
		GL11.glVertex2f(-width / 2, yGaugePosition + 3);
		GL11.glVertex2f(-width / 2, yGaugePosition - 3);
		GL11.glEnd();

	}

}
