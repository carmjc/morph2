package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.behaviors.Movement;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;

public class Wander extends Movement {

	private float wanderFocusDistance;
	private float wanderRadius;
	private final Vect3D steeringForce = new Vect3D();

	private final Vect3D wanderTarget = new Vect3D();

	/**
	 * Do not use.
	 */
	@Deprecated
	public Wander() {
	}

	public Wander(Ship ship) {
		super(ship);
	}

	@Override
	public Vect3D getSteeringForce() {
		return steeringForce;
	}

	public float getWanderFocusDistance() {
		return wanderFocusDistance;
	}

	@Override
	public void initRenderer() {
		// nothing to do
	}

	@Override
	public boolean isActive() {
		return wanderFocusDistance != 0;
	}

	@Override
	public void render(int glMode) {
		// nothing to do
	}

	@Override
	public void run(float secondsSinceLastUpdate) {
		if (wanderRadius == 0) {
			ship.removeBehavior(this);
			return;
		}

		final Vect3D pos = new Vect3D(ship.getPos());

		// Update target within the given constraints
		Vect3D wanderFocus = new Vect3D(pos).add(new Vect3D(0, 1, 0).rotate(ship.getHeading()).normalize(wanderFocusDistance + ship.getMass()));

		// Determine a target at acceptable distance from the wander focus point
		wanderTarget.x += Math.random() * 0.25f - 0.125f;
		wanderTarget.y += Math.random() * 0.25f - 0.125f;
		if (new Vect3D(wanderFocus).add(wanderTarget).distance(wanderFocus) > wanderRadius) {
			wanderTarget.copy(Vect3D.NULL);
		}

		steeringForce.copy(new Vect3D(wanderFocus).substract(pos).add(wanderTarget)).truncate(Ship.MAX_FORCE / ship.getMass());
	}

	public void setWanderFocusDistance(float wanderFocusDistance) {
		this.wanderFocusDistance = wanderFocusDistance;
	}

	public void setWanderRadius(float wanderRadius) {
		this.wanderRadius = wanderRadius;
	}

}