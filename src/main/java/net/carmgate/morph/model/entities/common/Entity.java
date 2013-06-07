package net.carmgate.morph.model.entities.common;

import java.util.HashSet;
import java.util.Set;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.StarsContribution;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.behaviors.common.ForceGeneratingBehavior;
import net.carmgate.morph.model.behaviors.common.Movement;
import net.carmgate.morph.model.behaviors.steering.Orbit;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Star;
import net.carmgate.morph.model.player.Player;

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
	protected final Player player;

	protected final Set<Behavior> behaviorSet = new HashSet<>();
	protected final Set<Behavior> pendingBehaviorsRemoval = new HashSet<>();
	protected final Set<Behavior> pendingBehaviorsAddition = new HashSet<>();
	protected StarsContribution starsContribution;

	protected boolean dead;
	protected float realAccelModulus;

	protected Entity(Player player) {
		synchronized (nextId) {
			id = nextId++;
		}

		if (!(this instanceof Star)) {
			starsContribution = new StarsContribution(this);
			addBehavior(starsContribution);
		}

		this.player = player;
	}

	/**
	 * Adds a behavior to the ship if the needed morphs are present is the ship
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

	public void addEnergy(float energyInc) {
		energy = Math.min(maxEnergy, energy + energyInc);
	}

	private void applySteeringForce(Vect3D force) {
		steeringForce.add(force);
	}

	protected void computeForcesFromBehavior() {
		effectiveForce.nullify();
		steeringForce.nullify();

		// if no movement needed, no update needed
		for (Behavior behavior : behaviorSet) {
			behavior.run();

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

	protected void computeSpeedAndPos() {
		// real accel is necessary to calculate propulsors energy consumption
		// it is the difference between the speed in the new cycle and
		// the speed in the previous cycle
		Vect3D realAccel = new Vect3D(speed);
		speed.add(new Vect3D(effectiveForce).mult(1f / mass).mult(Model.getModel().getSecondsSinceLastUpdate())).truncate(maxSpeed);
		realAccel.substract(speed);
		realAccelModulus = realAccel.modulus();
		pos.add(new Vect3D(speed).mult(Model.getModel().getSecondsSinceLastUpdate()));
	}

	public float getHeading() {
		return heading;
	}

	@Override
	public int getId() {
		return id;
	}

	public float getMass() {
		return mass;
	}

	public float getMaxSpeed() {
		return maxSpeed;
	}

	public float getMaxSteeringForce() {
		return maxSteeringForce;
	}

	public Player getPlayer() {
		return player;
	}

	public Vect3D getPos() {
		return pos;
	}

	public Vect3D getSpeed() {
		return speed;
	}

	public boolean isDead() {
		return dead;
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	// No contract specific to the entity
	// IMPROVE we should probably define the entities in a different way

	protected boolean isSelectRendering(int glMode) {
		return glMode == GL11.GL_SELECT ||
				Model.getModel().getUiContext().isDebugMode() && Model.getModel().getUiContext().isSelectViewMode();
	}

	public void processPendingBehaviors() {
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
	public void removeBehavior(Behavior behavior) {
		pendingBehaviorsRemoval.add(behavior);
	}

	/**
	 * Removes all the behaviors that are of the same type.
	 * This method queues the behavior removal.
	 * @param behaviorClass
	 */
	public void removeBehaviorsByClass(Class<?> behaviorClass) {
		if (behaviorClass == null) {
			LOGGER.error("This method parameter should not be null");
		}

		CollectionUtils.select(behaviorSet, new SameClassPredicate(behaviorClass), pendingBehaviorsRemoval);
	}

	public void setHeading(float heading) {
		this.heading = heading;
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
}
