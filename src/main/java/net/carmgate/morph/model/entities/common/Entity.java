package net.carmgate.morph.model.entities.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.ai.BalancedAI;
import net.carmgate.morph.model.behaviors.StarsContribution;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.behaviors.common.ForceGeneratingBehavior;
import net.carmgate.morph.model.behaviors.common.Movement;
import net.carmgate.morph.model.behaviors.passive.Dying;
import net.carmgate.morph.model.behaviors.passive.TakingDamage;
import net.carmgate.morph.model.behaviors.steering.Orbit;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.Star;
import net.carmgate.morph.model.entities.common.listener.DeathListener;
import net.carmgate.morph.model.events.Die;
import net.carmgate.morph.model.events.Event;
import net.carmgate.morph.model.events.TakeDamage;
import net.carmgate.morph.model.player.Player;
import net.carmgate.morph.model.player.Player.PlayerType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Entity implements Renderable, Selectable, Updatable {

	private static final class SameClassPredicate implements Predicate {
		private final Class<?> behaviorClass;

		public SameClassPredicate(Class<?> behaviorClass) {
			this.behaviorClass = behaviorClass;
		}

		@Override
		public boolean evaluate(Object object) {
			return behaviorClass.isInstance(object);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Entity.class);
	private static Integer nextId = 0;
	protected final int id;

	/** The ship position in the world. */
	protected final Vect3D pos = new Vect3D();
	protected final Vect3D speed = new Vect3D();
	/** The ship orientation in the world. */
	protected float heading;

	protected float maxSteeringForce;
	protected float maxSpeed;
	protected final Vect3D steeringForce = new Vect3D();
	protected final Vect3D effectiveForce = new Vect3D();

	// TODO remove the initial 10 value
	protected float mass = 10;
	protected float damage = 0;
	protected float maxDamage = 1;
	protected float energy;
	protected float maxEnergy = 0;

	protected boolean selected;
	private final Player player;

	private final Set<Behavior> behaviorSet = new HashSet<>();
	private final Set<Behavior> pendingBehaviorsRemoval = new HashSet<>();
	private final Set<Behavior> pendingBehaviorsAddition = new HashSet<>();
	private StarsContribution starsContribution;

	private boolean dead;
	protected float realAccelModulus;
	private final List<Event> newEventList = new ArrayList<>();
	private final List<Event> eventList = new ArrayList<>();
	private final List<DeathListener> deathListeners = new ArrayList<>();

	protected Entity(Player player) {
		synchronized (nextId) {
			id = nextId++;
		}

		// TODO We should not have to exclude Stars within Entity
		// Entity should not have to know Stars
		if (!(this instanceof Star)) {
			starsContribution = new StarsContribution(this);
			addBehavior(starsContribution);
		}

		this.player = player;
	}

	/**
	 * Adds a behavior to the ship if the needed morphs are present is the ship.
	 * Warning : This method is overridden in {@link Ship}
	 * @param behavior
	 * @return true if it was possible to add the behavior
	 */
	public void addBehavior(Behavior behavior) {
		pendingBehaviorsAddition.add(behavior);
		// TODO Find a better way of handling this
		if (behavior instanceof Orbit) {
			((Orbit) behavior).setStarsContribution(starsContribution);
		}
	}

	/**
	 * @param deathListener The death listener to add
	 */
	public void addDeathListener(DeathListener deathListener) {
		deathListeners.add(deathListener);
	}

	public final void addEnergy(float energyInc) {
		energy = Math.min(maxEnergy, energy + energyInc);
	}

	private void applySteeringForce(Vect3D force) {
		steeringForce.add(force);
	}

	/**
	 * Rotates the entity to match its orientation with its heading.
	 */
	protected void autoRotate() {
		// Empty default implementation
	}

	/**
	 * Clone the behaviors of the current entity into the entity passed as parameter.
	 * TODO The logic should be reversed, it's not clean to have to call this method from the subclass.
	 * @param clone the entity to clone the behaviors into.
	 */
	protected void cloneBehaviors(Entity clone) {
		// clone behaviors
		for (Behavior behavior : behaviorSet) {
			clone.addBehavior(behavior.cloneForEntity(clone));
		}

		// clone behaviors being added
		for (Behavior behavior : pendingBehaviorsAddition) {
			clone.addBehavior(behavior.cloneForEntity(clone));
		}

		// clone behaviors
		for (Behavior behavior : pendingBehaviorsRemoval) {
			clone.removeBehavior(behavior.cloneForEntity(clone));
		}

	}

	private void computeForcesFromBehavior() {
		effectiveForce.nullify();
		steeringForce.nullify();

		// if no movement needed, no update needed
		for (Behavior behavior : behaviorSet) {

			// if the behavior is a movement, use the generated steering force
			if (behavior instanceof Movement) {
				applySteeringForce(((Movement) behavior).getSteeringForce());
			}

			// if the behavior is generating a force, we must apply it
			if (behavior instanceof ForceGeneratingBehavior) {
				effectiveForce.add(((ForceGeneratingBehavior) behavior).getNonSteeringForce());
			}
		}

		// cap steeringForce to maximum steering force
		steeringForce.truncate(getMaxSteeringForce());
		effectiveForce.add(steeringForce);

	}

	private void computeSpeedAndPos() {
		// real accel is necessary to calculate propulsors energy consumption
		// it is the difference between the speed in the new cycle and
		// the speed in the previous cycle
		Vect3D realAccel = new Vect3D(speed);
		speed.add(new Vect3D(effectiveForce).mult(1f / mass).mult(Model.getModel().getSecondsSinceLastUpdate())).truncate(maxSpeed);
		realAccel.substract(speed);
		realAccelModulus = realAccel.modulus();
		pos.add(new Vect3D(speed).mult(Model.getModel().getSecondsSinceLastUpdate()));
	}

	/**
	 * Adds orders.
	 * The orders are effectively added at the end of the update cycle
	 * once the current update cycle orders have been processed.
	 * @param order
	 */
	public final void fireEvent(Event order) {
		newEventList.add(order);
	}

	protected BalancedAI getAI() {
		// empty default implem
		return null;
	}

	protected Set<Behavior> getBehaviors() {
		return behaviorSet;
	}

	public final float getDamage() {
		return damage;
	}

	public final List<DeathListener> getDeathListeners() {
		return deathListeners;
	}

	public final float getHeading() {
		return heading;
	}

	@Override
	public final int getId() {
		return id;
	}

	public final float getMass() {
		return mass;
	}

	public float getMaxDamage() {
		return maxDamage;
	}

	public final float getMaxSpeed() {
		return maxSpeed;
	}

	public final float getMaxSteeringForce() {
		return maxSteeringForce;
	}

	// No contract specific to the entity
	// IMPROVE we should probably define the entities in a different way

	public final Player getPlayer() {
		return player;
	}

	public final Vect3D getPos() {
		return pos;
	}

	public final Vect3D getSpeed() {
		return speed;
	}

	/**
	 * This method handles orders.
	 * IMPROVE This probably should be improved. It is quite ugly to have such a if-else cascade.
	 * However, I don't want to use a handler factory that would kill the current simplicity of orders handling inner code
	 * @param event
	 */
	protected void handleEvent(Event event) {
		if (event instanceof TakeDamage) {
			addBehavior(new TakingDamage(this, ((TakeDamage) event).getDamageAmount()));

		} else if (event instanceof Die) {
			addBehavior(new Dying(this));

		}
	}

	/**
	 * Handle events for the Entity.
	 */
	private void handleEvents() {
		for (Event event : eventList) {
			if (getAI() != null) {
				getAI().handleEvent(event);
			}
			handleEvent(event);
		}
		eventList.clear();
		eventList.addAll(newEventList);
		newEventList.clear();
	}

	public boolean hasBehaviorByClass(Class<?> behaviorClass) {
		if (behaviorClass == null) {
			LOGGER.error("This method parameter should not be null");
		}

		return CollectionUtils.countMatches(behaviorSet, new SameClassPredicate(behaviorClass)) > 0;
	}

	public final boolean isDead() {
		return dead;
	}

	@Override
	public final boolean isSelected() {
		return selected;
	}

	protected final boolean isSelectRendering(int glMode) {
		return glMode == GL11.GL_SELECT ||
				Model.getModel().getUiContext().isDebugMode() && Model.getModel().getUiContext().isDebugSelectViewMode();
	}

	public final void processPendingBehaviors() {
		// Cleaning
		for (Behavior behavior : pendingBehaviorsRemoval) {
			behaviorSet.remove(behavior);
		}
		pendingBehaviorsRemoval.clear();

		// Executing pending behavior addition
		for (Behavior behavior : pendingBehaviorsAddition) {
			behaviorSet.add(behavior);
		}
		pendingBehaviorsAddition.clear();
	}

	/**
	 * Removes a behavior from the ship's behavior collection.
	 * This method postpones the behavior deletion until the end of the processing loop.
	 * This way, the handling of behaviors is insensitive to the order in which they are processed and removed.
	 * @param behavior to remove
	 */
	public final void removeBehavior(Behavior behavior) {
		pendingBehaviorsRemoval.add(behavior);
	}

	/**
	 * Removes all the behaviors that are of the same type.
	 * This method queues the behavior removal.
	 * @param behaviorClass
	 */
	public final void removeBehaviorsByClass(Class<?> behaviorClass) {
		if (behaviorClass == null) {
			LOGGER.error("This method parameter should not be null");
		}

		CollectionUtils.select(behaviorSet, new SameClassPredicate(behaviorClass), pendingBehaviorsRemoval);
	}

	/**
	 * @param deathListener The death listener to remove
	 */
	public final void removeDeathListener(DeathListener deathListener) {
		deathListeners.remove(deathListener);
	}

	public final void setDamage(float damage) {
		this.damage = damage;
	}

	public final void setDead(boolean dead) {
		this.dead = dead;
	}

	public final void setHeading(float heading) {
		this.heading = heading;
	}

	@Override
	public final void setSelected(boolean selected) {
		this.selected = selected;
	}

	// FIXME
	@Override
	public void update() {
		// handle AI assignements if appropriate
		// TODO This is not implemented so far, and this probably is not the best way to handle it
		if (player.getPlayerType() == PlayerType.AI) {
			if (getAI() != null) {
				LOGGER.debug("Process AI");
				getAI().run();
			}
		}

		// Update behaviors
		for (Behavior behavior : behaviorSet) {
			behavior.run();
			behavior.computeXpContribution();
		}

		computeForcesFromBehavior();
		autoRotate();
		computeSpeedAndPos();

		// Handle orders
		handleEvents();

		// update trail
		updateTrail();
	}

	protected void updateTrail() {
		// TODO This mecanism should be handled in something more generic
		// so that we do not have to add this method to entity but so that the ship has it
		// empty default implementation
	}
}
