package net.carmgate.morph.model;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.carmgate.morph.actions.InteractionStack;
import net.carmgate.morph.model.entities.Entity;
import net.carmgate.morph.model.entities.EntityHints;
import net.carmgate.morph.model.entities.EntityType;
import net.carmgate.morph.model.entities.Selectable;
import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.model.view.Window;
import net.carmgate.morph.ui.rendering.RenderingHints;
import net.carmgate.morph.ui.rendering.RenderingSteps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Model {

	private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);
	private static final Model _instance = new Model();

	/** Singleton instance getter. */
	public static Model getModel() {
		return _instance;
	}

	private boolean debugMode = false;

	/** number of millis since game start. */
	private long msec = 0;

	/** timestamp of game start. */
	private final long gameStartMsec = new Date().getTime();

	private final Window window = new Window();

	private final ViewPort viewport = new ViewPort();

	private final Set<Selectable> selection = new HashSet<>();

	private final InteractionStack interactionStack = new InteractionStack();
	/** All the entities of the world can be searched by @entity uniqueId and entity instance uniqueId. */
	// TODO we should rework this structure, it's not clean.
	private final Map<EntityType, EntityMap> entitiesByEntityType = new HashMap<>();

	private final Map<RenderingSteps, EntityMap> entitiesByRenderingStep = new HashMap<>();

	private Model() {
		// add some noop in the interaction queue to get rid of exceptions
	}

	public void addEntity(Entity entity) {
		// Add it the selection model
		EntityType entityType = entity.getClass().getAnnotation(EntityHints.class).entityType();
		RenderingSteps renderingStep = entity.getClass().getAnnotation(RenderingHints.class).renderingStep();
		EntityMap entityMap = getEntitiesByType(entityType);
		if (entityMap == null) {
			entityMap = new EntityMap();
			entitiesByEntityType.put(entityType, entityMap);
			entitiesByRenderingStep.put(renderingStep, entityMap);
		}
		entityMap.put(entity.getSelectionId(), entity);
	}

	// TODO We must fix the temptation to use getSelection.clear() instead
	public void clearSelection() {
		for (Selectable selectable : selection) {
			selectable.setSelected(false);
		}
		selection.clear();
	}

	/**
	 * @return number of millis since game start.
	 */
	public long getCurrentTS() {
		return msec;
	}

	public EntityMap getEntitiesByRenderingType(RenderingSteps renderingStep) {
		return entitiesByRenderingStep.get(renderingStep);
	}

	public EntityMap getEntitiesByType(EntityType entityType) {
		return entitiesByEntityType.get(entityType);
	}

	public EntityMap getEntitiesByType(int ordinal) {
		return entitiesByEntityType.get(EntityType.values()[ordinal]);
	}

	public InteractionStack getInteractionStack() {
		return interactionStack;
	}

	public Set<Selectable> getSelection() {
		return selection;
	}

	public ViewPort getViewport() {
		return viewport;
	}

	public Window getWindow() {
		return window;
	}

	public boolean isDebugMode() {
		return debugMode;
	}

	public void toggleDebugMode() {
		debugMode = !debugMode;
	}

	public void update() {
		// update the number of millis since game start
		msec = new Date().getTime() - gameStartMsec;

		// Update all entities
		// TODO Find a way to filter the entities needing an update
		for (EntityMap entityMap : entitiesByEntityType.values()) {
			for (Entity entity : entityMap.values()) {
				entity.update();
			}
		}
	}

}
