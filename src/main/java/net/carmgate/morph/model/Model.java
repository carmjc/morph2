package net.carmgate.morph.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private final List selection = new ArrayList<>();

	/** All the entities of the world can be searched by @entity uniqueId and entity instance uniqueId. */
	private final Map<Integer, Map<Integer, Object>> entities = new HashMap<>();

	private Model() {
	}

	public Map<Integer, Map<Integer, Object>> getEntities() {
		return entities;
	}

	public <T> Map<Integer, T> getEntityMap(int entityUniqueId) {
		return (Map<Integer, T>) entities.get(entityUniqueId);
	}

	public List getSelection() {
		return selection;
	}

	public UIContext getUIContext() {
		return uiContext;
	}

	public ViewPort getViewport() {
		return viewport;
	}
}
