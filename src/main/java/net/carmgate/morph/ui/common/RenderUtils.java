package net.carmgate.morph.ui.common;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;

public class RenderUtils {

	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);

	public static void renderCircle(float radius, float lineWidth, Float[] colorInt, Float[] colorMiddle, Float[] colorExt) {
		// render limit of effect zone
		TextureImpl.bindNone();
		float tInt = 0; // temporary data holder
		float tExt = 0; // temporary data holder
		float xInt;
		float xExt;

		xInt = radius; // radius
		xExt = radius + lineWidth; // radius

		float xIntBackup = xInt; // radius
		float xExtBackup = xExt; // radius
		float yInt = 0;
		float yExt = 0;
		float yIntBackup = 0;
		float yExtBackup = 0;
		for (int i = 0; i < nbSegments; i++) {

			tInt = xInt;
			tExt = xExt;
			xInt = cos * xInt - sin * yInt;
			xExt = cos * xExt - sin * yExt;
			yInt = sin * tInt + cos * yInt;
			yExt = sin * tExt + cos * yExt;

			GL11.glBegin(GL11.GL_QUADS);
			GL11.glColor4f(colorInt[0], colorInt[1], colorInt[2], colorInt[3]);
			GL11.glVertex2f(xInt, yInt);
			GL11.glColor4f(colorInt[0], colorInt[1], colorInt[2], colorInt[3]);
			GL11.glVertex2f(xIntBackup, yIntBackup);
			GL11.glColor4f(colorMiddle[0], colorMiddle[1], colorMiddle[2], colorMiddle[3]);
			GL11.glVertex2f((xExtBackup + xIntBackup) / 2, (yExtBackup + yIntBackup) / 2);
			GL11.glColor4f(colorMiddle[0], colorMiddle[1], colorMiddle[2], colorMiddle[3]);
			GL11.glVertex2f((xExt + xInt) / 2, (yExt + yInt) / 2);
			GL11.glColor4f(colorMiddle[0], colorMiddle[1], colorMiddle[2], colorMiddle[3]);
			GL11.glVertex2f((xExtBackup + xIntBackup) / 2, (yExtBackup + yIntBackup) / 2);
			GL11.glColor4f(colorMiddle[0], colorMiddle[1], colorMiddle[2], colorMiddle[3]);
			GL11.glVertex2f((xExt + xInt) / 2, (yExt + yInt) / 2);
			GL11.glColor4f(colorExt[0], colorExt[1], colorExt[2], colorExt[3]);
			GL11.glVertex2f(xExt, yExt);
			GL11.glColor4f(colorExt[0], colorExt[1], colorExt[2], colorExt[3]);
			GL11.glVertex2f(xExtBackup, yExtBackup);
			GL11.glEnd();

			xIntBackup = xInt;
			xExtBackup = xExt;
			yIntBackup = yInt;
			yExtBackup = yExt;
		}
	}

	public static void renderDisc(float radius) {
		// render limit of effect zone
		TextureImpl.bindNone();
		float tInt = 0; // temporary data holder
		float tExt = 0; // temporary data holder
		float xInt;
		float xExt;
		float zoomFactor = Model.getModel().getViewport().getZoomFactor();

		xInt = 0; // radius
		xExt = radius; // radius

		float xIntBackup = xInt; // radius
		float xExtBackup = xExt; // radius
		float yInt = 0;
		float yExt = 0;
		float yIntBackup = 0;
		float yExtBackup = 0;
		for (int i = 0; i < nbSegments; i++) {

			tExt = xExt;
			xExt = cos * xExt - sin * yExt;
			yExt = sin * tExt + cos * yExt;

			GL11.glBegin(GL11.GL_QUADS);
			// GL11.glColor4f(colorInt[0], colorInt[1], colorInt[2], colorInt[3]);
			GL11.glVertex2f(xInt, yInt);
			// GL11.glColor4f(colorInt[0], colorInt[1], colorInt[2], colorInt[3]);
			GL11.glVertex2f(xIntBackup, yIntBackup);
			// GL11.glColor4f(colorMiddle[0], colorMiddle[1], colorMiddle[2], colorMiddle[3]);
			GL11.glVertex2f((xExtBackup + xIntBackup) / 2, (yExtBackup + yIntBackup) / 2);
			// GL11.glColor4f(colorMiddle[0], colorMiddle[1], colorMiddle[2], colorMiddle[3]);
			GL11.glVertex2f((xExt + xInt) / 2, (yExt + yInt) / 2);
			// GL11.glColor4f(colorMiddle[0], colorMiddle[1], colorMiddle[2], colorMiddle[3]);
			GL11.glVertex2f((xExtBackup + xIntBackup) / 2, (yExtBackup + yIntBackup) / 2);
			// GL11.glColor4f(colorMiddle[0], colorMiddle[1], colorMiddle[2], colorMiddle[3]);
			GL11.glVertex2f((xExt + xInt) / 2, (yExt + yInt) / 2);
			// GL11.glColor4f(colorExt[0], colorExt[1], colorExt[2], colorExt[3]);
			GL11.glVertex2f(xExt, yExt);
			// GL11.glColor4f(colorExt[0], colorExt[1], colorExt[2], colorExt[3]);
			GL11.glVertex2f(xExtBackup, yExtBackup);
			GL11.glEnd();

			xIntBackup = xInt;
			xExtBackup = xExt;
			yIntBackup = yInt;
			yExtBackup = yExt;
		}
	}

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

	public static void renderLine(Vect3D from, Vect3D to, float width, Float[] colorInt, Float[] colorExt) {
		TextureImpl.bindNone();
		Vect3D ortho = new Vect3D(to).substract(from).normalize(width / 2).rotate(90);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glColor4f(colorExt[0], colorExt[1], colorExt[2], colorExt[3]);
		GL11.glVertex2f(from.x - ortho.x, from.y - ortho.y);
		GL11.glVertex2f(to.x - ortho.x, to.y - ortho.y);
		GL11.glColor4f(colorInt[0], colorInt[1], colorInt[2], colorInt[3]);
		GL11.glVertex2f(to.x, to.y);
		GL11.glVertex2f(from.x, from.y);
		GL11.glVertex2f(to.x, to.y);
		GL11.glVertex2f(from.x, from.y);
		GL11.glColor4f(colorExt[0], colorExt[1], colorExt[2], colorExt[3]);
		GL11.glVertex2f(from.x + ortho.x, from.y + ortho.y);
		GL11.glVertex2f(to.x + ortho.x, to.y + ortho.y);
		GL11.glEnd();
	}

}
