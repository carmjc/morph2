package net.carmgate.morph.ui;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;

import org.lwjgl.input.Mouse;

/**
 * Allows mouse manipulation in world coordinates.
 */
public class GameMouse {

	public static Vect3D getPosInWord() {
		return new Vect3D(getXInWorld(), getYInWorld(), 0);
	}

	/**
	 * @return mouse X position in window coordinates.
	 */
	public static int getX() {
		return Mouse.getX();
	}

	/**
	 * @return mouse X position in world coordinates.
	 */
	public static int getXInWorld() {
		ViewPort viewport = Model.getModel().getViewport();
		return (int) ((Mouse.getX() - Model.getModel().getWindow().getWidth() / 2 + viewport.getFocalPoint().x) / viewport.getZoomFactor());
	}

	/**
	 * @return mouse Y position in window coordinates.
	 */
	public static int getY() {
		return Mouse.getY();
	}

	/**
	 * @return mouse Y position in world coordinates.
	 */
	public static int getYInWorld() {
		ViewPort viewport = Model.getModel().getViewport();
		return (int) ((-Mouse.getY() + Model.getModel().getWindow().getHeight() / 2 + viewport.getFocalPoint().y) / viewport.getZoomFactor());
	}
}
