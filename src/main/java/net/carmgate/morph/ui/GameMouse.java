package net.carmgate.morph.ui;

import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.model.GlobalModel;
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
		ViewPort viewport = GlobalModel.getModel().getViewport();
		// TODO move the width to the model
		return (int) ((Mouse.getX() - Conf.getIntProperty("window.initialWidth") / 2) / viewport.getZoomFactor() + viewport.getFocalPoint().x) ;
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
		ViewPort viewport = GlobalModel.getModel().getViewport();
		// TODO move the height to the model
		return (int) ((Mouse.getY() - Conf.getIntProperty("window.initialHeight") / 2) / viewport.getZoomFactor() + viewport.getFocalPoint().y) ;
	}

}
