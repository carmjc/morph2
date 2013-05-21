package net.carmgate.morph.model;

import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.carmgate.morph.actions.common.InteractionStack;
import net.carmgate.morph.model.entities.Entity;
import net.carmgate.morph.model.entities.EntityHints;
import net.carmgate.morph.model.entities.EntityType;
import net.carmgate.morph.model.entities.Selectable;
import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.model.view.Window;
import net.carmgate.morph.ui.ParticleEngine;
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
	private long gameStartMsec = new Date().getTime();

	private final Window window = new Window();

	private final ViewPort viewport = new ViewPort();

	private final Set<Selectable> simpleSelection = new HashSet<>();
	private final Deque<Selectable> actionSelection = new LinkedList<>();

	private final InteractionStack interactionStack = new InteractionStack();

	/** All the entities of the world can be searched by @entity uniqueId and entity instance uniqueId. */
	// TODO we should rework this structure, it's not clean.
	private final Map<EntityType, EntityMap> entitiesByEntityType = new HashMap<>();
	private final Map<RenderingSteps, EntityMap> entitiesByRenderingStep = new HashMap<>();

	private final ParticleEngine particleEngine = new ParticleEngine();
	private boolean pause;

	private final Set<Entity> entitiesToRemove = new HashSet<>();

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
	public void clearActionSelection() {
		actionSelection.clear();
	}

	// TODO We must fix the temptation to use getSelection.clear() instead
	public void clearSimpleSelection() {
		for (Selectable selectable : simpleSelection) {
			selectable.setSelected(false);
		}
		simpleSelection.clear();
	}

	public Deque<Selectable> getActionSelection() {
		return actionSelection;
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

	public ParticleEngine getParticleEngine() {
		return particleEngine;
	}

	public Set<Selectable> getSimpleSelection() {
		return simpleSelection;
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

	public void removeEntity(Entity entity) {
		entitiesToRemove.add(entity);
	}

	public void toggleDebugMode() {
		debugMode = !debugMode;
	}

	public void togglePause() {
		pause = !pause;
	}

	public void update() {
		// update the number of millis since game start
		long tmpMsec = new Date().getTime() - gameStartMsec;
		if (pause) {
			gameStartMsec += tmpMsec - msec;
		} else {
			msec = tmpMsec;
		}

		// Update all entities
		// IMPROVE Find a way to filter the entities needing an update
		for (EntityMap entityMap : entitiesByEntityType.values()) {
			for (Entity entity : entityMap.values()) {
				entity.update();
			}
		}

		// Remove entities flagged as "being removed"
		for (Entity entity : entitiesToRemove) {
			getEntitiesByRenderingType(entity.getClass().getAnnotation(RenderingHints.class).renderingStep()).remove(entity.getSelectionId());
			getEntitiesByType(entity.getClass().getAnnotation(EntityHints.class).entityType()).remove(entity.getSelectionId());
		}

		// particle engin
		particleEngine.update();
	}
}
