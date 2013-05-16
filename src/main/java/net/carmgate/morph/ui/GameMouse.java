package net.carmgate.morph.ui;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.view.ViewPort;

import org.lwjgl.input.Mouse;

/**
 * Allows mouse manipulation in world coordinates.
 */
public class GameMouse {

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
		return (int) ((Mouse.getX() - Model.getModel().getWindow().getWidth() / 2) / viewport.getZoomFactor() - viewport.getFocalPoint().x);
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
		return (int) ((Mouse.getY() - Model.getModel().getWindow().getHeight() / 2) / viewport.getZoomFactor() - viewport.getFocalPoint().y);
	}

}
