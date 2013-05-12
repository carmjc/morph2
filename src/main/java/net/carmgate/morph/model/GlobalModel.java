package net.carmgate.morph.model;

import net.carmgate.morph.model.view.ViewPort;

public class GlobalModel {

	private static final GlobalModel _instance = new GlobalModel();

	/** Singleton instance getter. */
	public static GlobalModel getModel() {
		return _instance;
	}

	private final ViewPort viewport = new ViewPort();

	private GlobalModel() {
	}

	public ViewPort getViewport() {
		return viewport;
	}
}
