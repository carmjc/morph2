package net.carmgate.morph.model;

import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.ui.UIContext;

public class Model {

	private static final Model _instance = new Model();

	/** Singleton instance getter. */
	public static Model getModel() {
		return _instance;
	}

	private final ViewPort viewport = new ViewPort();
	private final UIContext uiContext = new UIContext();

	private Model() {
	}

	public UIContext getUIContext() {
		return uiContext;
	}

	public ViewPort getViewport() {
		return viewport;
	}
}
