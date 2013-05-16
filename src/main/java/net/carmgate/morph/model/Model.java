package net.carmgate.morph.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.carmgate.morph.actions.InteractionStack;
import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.model.view.Window;
import net.carmgate.morph.ui.Context;
import net.carmgate.morph.ui.Selectable;

public class Model {

	private static final Model _instance = new Model();

	/** Singleton instance getter. */
	public static Model getModel() {
		return _instance;
	}

	private final Window window = new Window();
	private final ViewPort viewport = new ViewPort();

	private final Context uiContext = new Context();

	private final Set<Selectable> selection = new HashSet<>();
	private final InteractionStack interactionStack = new InteractionStack();
	/** All the entities of the world can be searched by @entity uniqueId and entity instance uniqueId. */
	private final Map<Integer, Map<Integer, Object>> entities = new HashMap<>();

	private Model() {
		// add some noop in the interaction queue to get rid of exceptions
	}

	// TODO We must fix the temptation to use getSelection.clear() instead
	public void clearSelection() {
		for (Selectable selectable : selection) {
			selectable.setSelected(false);
		}
		selection.clear();
	}

	public Map<Integer, Map<Integer, Object>> getEntities() {
		return entities;
	}

	public <T> Map<Integer, T> getEntityMap(int entityUniqueId) {
		return (Map<Integer, T>) entities.get(entityUniqueId);
	}

	public InteractionStack getInteractionStack() {
		return interactionStack;
	}

	public Set<Selectable> getSelection() {
		return selection;
	}

	public Context getUIContext() {
		return uiContext;
	}

	public ViewPort getViewport() {
		return viewport;
	}

	public Window getWindow() {
		return window;
	}

}
