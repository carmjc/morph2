package net.carmgate.morph.model;

import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.carmgate.morph.actions.common.InteractionStack;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.WorldArea;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityHints;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.model.entities.common.Selectable;
import net.carmgate.morph.model.player.Player;
import net.carmgate.morph.model.player.Player.FOF;
import net.carmgate.morph.model.player.Player.PlayerType;
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
	private long currentTS = 0;

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
	private final Player self;
	private final Set<Player> players = new HashSet<>();
	private WorldArea rootWA;
	private float secondsSinceLastUpdate;
	private long lastUpdateTS;

	private Model() {
		self = new Player(PlayerType.HUMAN, "Carm", FOF.SELF);
		rootWA = new WorldArea();
		// TODO The following lines should not be needed
		for (int i = 0; i < 16; i++) {
			rootWA = rootWA.getParent();
		}
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
		entityMap.put(entity.getId(), entity);
	}

	// IMPROVE We must fix the temptation to use getSelection.clear() instead
	public void clearActionSelection() {
		actionSelection.clear();
	}

	// IMPROVE We must fix the temptation to use getSelection.clear() instead
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
		return currentTS;
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

	public long getLastUpdateTS() {
		return lastUpdateTS;
	}

	public ParticleEngine getParticleEngine() {
		return particleEngine;
	}

	public Set<Player> getPlayers() {
		return players;
	}

	public WorldArea getRootWA() {
		return rootWA;
	}

	public float getSecondsSinceLastUpdate() {
		return secondsSinceLastUpdate;
	}

	public Player getSelf() {
		return self;
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
		// long before = new GregorianCalendar().getTimeInMillis();

		// update the number of millis since game start
		long tmpMsec = new Date().getTime() - gameStartMsec;
		lastUpdateTS = currentTS;
		if (pause) {
			gameStartMsec += tmpMsec - currentTS;
		} else {
			currentTS = tmpMsec;
		}
		secondsSinceLastUpdate = ((float) currentTS - lastUpdateTS) / 1000;

		// Update WAs
		// Create necessary WAs
		if (viewport.getZoomFactor() > 0.25) {
			float zoomFactor = Model.getModel().getViewport().getZoomFactor();
			float windowWidthInWorld = window.getWidth() / zoomFactor;
			float windowHeightInWorld = window.getHeight() / zoomFactor;
			Vect3D focalPointInWorld = new Vect3D().substract(new Vect3D(viewport.getFocalPoint())).mult(1f / zoomFactor);
			for (float x = focalPointInWorld.x - windowWidthInWorld / 2; x < focalPointInWorld.x + windowWidthInWorld + 512 / zoomFactor; x += 512 / zoomFactor) {
				for (float y = focalPointInWorld.y - windowHeightInWorld / 2; y < focalPointInWorld.y + windowHeightInWorld + 512 / zoomFactor; y += 512 / zoomFactor) {

					Vect3D studiedPoint = new Vect3D(x, y, 0);

					// get the WA under the focal point
					Set<WorldArea> fpWAs = rootWA.getOverlappingWAs(studiedPoint, 0);
					WorldArea fpWA = null;

					if (fpWAs.isEmpty()) {
						// the focal point is not even in the root wa ..
						// not probable for now
						LOGGER.debug("empty");
					} else {
						int minLevel = Integer.MAX_VALUE;
						for (WorldArea wa : fpWAs) {
							if (minLevel > wa.getLevel()) {
								minLevel = wa.getLevel();
								fpWA = wa;
							}
						}

						// fpWAs is not empty, therefore, fpWA cannot be null
						if (minLevel > 0) {
							// LOGGER.debug(studiedPoint.toString());
							fpWA = fpWA.createDescendantWA(studiedPoint, 0);
						}
					}
				}

			}

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
			getEntitiesByRenderingType(entity.getClass().getAnnotation(RenderingHints.class).renderingStep()).remove(entity.getId());
			getEntitiesByType(entity.getClass().getAnnotation(EntityHints.class).entityType()).remove(entity.getId());
		}

		// particle engin
		particleEngine.update();

		// long after = new GregorianCalendar().getTimeInMillis();
		// LOGGER.debug("Time spent in world update: " + (after - before));
	}
}
