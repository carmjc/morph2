package net.carmgate.morph.model;

import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.carmgate.morph.actions.common.InteractionStack;
import net.carmgate.morph.model.ai.BalancedAI;
import net.carmgate.morph.model.behaviors.SpawnShips;
import net.carmgate.morph.model.behaviors.steering.Orbit;
import net.carmgate.morph.model.behaviors.steering.WanderWithinRange;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.Planet;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.Star;
import net.carmgate.morph.model.entities.Station;
import net.carmgate.morph.model.entities.WorldArea;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityHints;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.model.player.Player;
import net.carmgate.morph.model.player.Player.FOF;
import net.carmgate.morph.model.player.Player.PlayerType;
import net.carmgate.morph.model.ui.UiContext;
import net.carmgate.morph.model.ui.ViewPort;
import net.carmgate.morph.model.ui.Window;
import net.carmgate.morph.ui.ParticleEngine;
import net.carmgate.morph.ui.common.RenderingHints;
import net.carmgate.morph.ui.common.RenderingSteps;

import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Model {

	private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);
	private static final Model _instance = new Model();

	static {
		Model.getModel().init();
	}

	/** Singleton instance getter. */
	public static Model getModel() {
		return _instance;
	}

	// Time management
	/** number of millis since game start. */
	private long currentTS = 0;
	/** timestamp of game start. */
	private long gameStartMsec = new Date().getTime();
	private float secondsSinceLastUpdate;
	private long lastUpdateTS;

	// Ui context
	private final Window window = new Window();
	private final ViewPort viewport = new ViewPort();
	private final UiContext uiContext = new UiContext();

	// Handling user inputs
	private final Set<Entity> simpleSelection = new HashSet<>();
	private final Deque<Entity> actionSelection = new LinkedList<>();
	private final InteractionStack interactionStack = new InteractionStack();
	private final Set<Morph> morphSelection = new HashSet<>();

	/** All the entities of the world can be searched by @entity uniqueId and entity instance uniqueId. */
	// TODO we should rework this structure, it's not clean.
	private final Set<Entity> entities = new HashSet<>();
	private final Map<EntityType, EntityMap> entitiesByEntityType = new HashMap<>();
	private final Map<RenderingSteps, EntityMap> entitiesByRenderingStep = new HashMap<>();
	private final Set<Entity> entitiesToRemove = new HashSet<>();

	// particle engine
	private final ParticleEngine particleEngine = new ParticleEngine();

	// players and self ship
	private final Player self;
	private Ship selfShip;
	private final Set<Player> players = new HashSet<>();

	// world areas
	// TODO #24 Replace this with some kind of particles to show that there is some movement without
	// the hassle of using such a complex system.
	// However, we should keep the world area code somewhere just in case we need it for optimization purpose later.
	private final WorldArea rootWA;

	private Model() {
		self = new Player(PlayerType.HUMAN, "Carm", FOF.SELF);

		// WorldArea deacivated for now
		// Review this strange initialization when reactivating
		rootWA = new WorldArea();
		// for (int i = 0; i < 16; i++) {
		// rootWA = rootWA.getParent();
		// }
	}

	/**
	 * Add an entity to the model.
	 * @param entity
	 */
	public void addEntity(Entity entity) {
		EntityType entityType = entity.getClass().getAnnotation(EntityHints.class).entityType();
		RenderingSteps renderingStep = entity.getClass().getAnnotation(RenderingHints.class).renderingStep();
		EntityMap entityMap = getEntitiesByType(entityType);
		if (entityMap == null) {
			entityMap = new EntityMap();
			entitiesByEntityType.put(entityType, entityMap);
			entitiesByRenderingStep.put(renderingStep, entityMap);
		}
		entityMap.put(entity.getId(), entity);
		entities.add(entity);
	}

	// IMPROVE We must fix the temptation to use getSelection.clear() instead
	public void clearActionSelection() {
		actionSelection.clear();
	}

	// IMPROVE We must fix the temptation to use getSelection.clear() instead
	public void clearMorphSelection() {
		for (Morph morph : morphSelection) {
			morph.setSelected(false);
		}
		morphSelection.clear();
	}

	// IMPROVE We must fix the temptation to use getSelection.clear() instead
	public void clearSimpleSelection() {
		for (Entity selectable : simpleSelection) {
			selectable.setSelected(false);
		}
		simpleSelection.clear();
	}

	// TODO Rework this method .. maybe with a Predicate
	public Set<Entity> findEntitiesWithinDistanceOfLocationAndNotPlayerOwned(final Vect3D location, float radius,
			Predicate predicate) {
		// TODO Check this ... it is probably very bad in terms of performance
		Set<Entity> resultSet = new TreeSet<>(new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				float d1 = o1.getPos().distance(location);
				float d2 = o2.getPos().distance(location);
				if (d1 < d2) {
					return -1;
				}
				if (d1 > d2) {
					return 1;
				}
				return 0;
			}
		});

		for (Entity entity : entities) {
			if (entity.getPos().distance(location) < radius && predicate.evaluate(entity)) {
				resultSet.add(entity);
			}
		}

		return resultSet;
	}

	public Deque<Entity> getActionSelection() {
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

	public Set<Morph> getMorphSelection() {
		return morphSelection;
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

	public Ship getSelfShip() {
		return selfShip;
	}

	public Set<Entity> getSimpleSelection() {
		return simpleSelection;
	}

	public UiContext getUiContext() {
		return uiContext;
	}

	public ViewPort getViewport() {
		return viewport;
	}

	public Window getWindow() {
		return window;
	}

	private void init() {
		Star star = new Star(3000, 3000, 0, 20000, 500, 4000000);
		Model.getModel().addEntity(star);
		Planet planet = new Planet(star, 1000, 100, 500000);
		planet.addBehavior(new Orbit(planet, star, 500000, true));
		Model.getModel().addEntity(planet);

		Player player = new Player(PlayerType.AI, "Nemesis", FOF.FOE);
		Station station = new Station(planet, 100, 50, 7000, player);

		Ship enemyShip = new Ship(128, 0, 0, 0, 10, player);
		enemyShip.addMorph(new Morph(MorphType.OVERMIND, enemyShip));
		enemyShip.addMorph(new Morph(MorphType.SIMPLE_PROPULSOR, enemyShip));
		// enemyShip.addMorph(new Morph(MorphType.SIMPLE_PROPULSOR, enemyShip));
		enemyShip.addMorph(new Morph(MorphType.LASER, enemyShip));
		enemyShip.addBehavior(new WanderWithinRange(enemyShip, 200, 100, station, 2000));
		enemyShip.setAi(new BalancedAI(enemyShip));

		station.addBehavior(new Orbit(station, planet, 7000, true));
		station.addBehavior(new SpawnShips(station.getPos(), 10, 5000, enemyShip));
		Model.getModel().addEntity(station);

		selfShip = new Ship(station.getPos().x, station.getPos().y, station.getPos().z, 10, 8, self);
		selfShip.addMorph(new Morph(MorphType.OVERMIND, selfShip));
		selfShip.addMorph(new Morph(MorphType.SHIELD, selfShip));
		selfShip.addMorph(new Morph(MorphType.SIMPLE_PROPULSOR, selfShip));
		selfShip.addMorph(new Morph(MorphType.LASER, selfShip));
		selfShip.addMorph(new Morph(MorphType.LASER, selfShip));
		selfShip.addMorph(new Morph(MorphType.ARMOR, selfShip));
		Model.getModel().addEntity(selfShip);

		Model.getModel().getSimpleSelection().add(selfShip);
		selfShip.setSelected(true);
		Model.getModel().getViewport().getFocalPoint().copy(new Vect3D(selfShip.getPos()).mult(Model.getModel().getViewport().getZoomFactor()));

	}

	public void removeEntity(Entity entity) {
		entitiesToRemove.add(entity);
	}

	public void update() {
		// long before = new GregorianCalendar().getTimeInMillis();

		// update the number of millis since game start
		long tmpMsec = new Date().getTime() - gameStartMsec;
		lastUpdateTS = currentTS;
		if (uiContext.isPaused()) {
			gameStartMsec += tmpMsec - currentTS;
		} else {
			currentTS = tmpMsec;
		}
		secondsSinceLastUpdate = ((float) currentTS - lastUpdateTS) / 1000;

		// if time has not progresse, we do nothing in the update part.
		if (secondsSinceLastUpdate == 0) {
			return;
		}

		// Update WAs
		// Create necessary WAs
		// FIXME if content dead code ?
		if (viewport.getZoomFactor() > 0.25) {
			float zoomFactor = Model.getModel().getViewport().getZoomFactor();
			float windowWidthInWorld = window.getWidth() / zoomFactor;
			float windowHeightInWorld = window.getHeight() / zoomFactor;
			Vect3D focalPointInWorld = new Vect3D().add(new Vect3D(viewport.getFocalPoint())).mult(1f / zoomFactor);
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
				entity.processPendingBehaviors();
			}
		}

		// Remove entities flagged as "needing to be removed"
		for (Entity entity : entitiesToRemove) {
			getEntitiesByRenderingType(entity.getClass().getAnnotation(RenderingHints.class).renderingStep()).remove(entity.getId());
			getEntitiesByType(entity.getClass().getAnnotation(EntityHints.class).entityType()).remove(entity.getId());
		}

		// particle engine update
		particleEngine.update();

	}
}
