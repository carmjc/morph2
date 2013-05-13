package net.carmgate.morph.model;

import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.ui.UIContext;

public class GlobalModel {

	private static final GlobalModel _instance = new GlobalModel();

	/** Singleton instance getter. */
	public static GlobalModel getModel() {
		return _instance;
	}

	private final ViewPort viewport = new ViewPort();
	private final UIContext uiContext = new UIContext();

	private GlobalModel() {
	}

	public UIContext getUIContext() {
		return uiContext;
	}

	public ViewPort getViewport() {
		return viewport;
	}
}
