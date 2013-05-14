package net.carmgate.morph.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.ui.Selectable;
import net.carmgate.morph.ui.Context;

public class Model {

	private static final Model _instance = new Model();

	/** Singleton instance getter. */
	public static Model getModel() {
		return _instance;
	}

	private final ViewPort viewport = new ViewPort();
	private final Context uiContext = new Context();
	private final List<Selectable> selection = new ArrayList<>();

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

	public List<Selectable> getSelection() {
		return selection;
	}

	public Context getUIContext() {
		return uiContext;
	}

	public ViewPort getViewport() {
		return viewport;
	}
}
