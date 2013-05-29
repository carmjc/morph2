package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Renderable;

// TODO make several movement classes to implement the different behaviors instead of mixing them.
public abstract class Movement implements Behavior, Renderable {
	// protected final Vect3D steeringForce = new Vect3D();
	protected final Ship ship;

	/**
	 * Do not use.
	 */
	@Deprecated
	protected Movement() {
		ship = null;
	}

	protected Movement(Ship ship) {
		this.ship = ship;
	}

	public abstract Vect3D getNonSteeringForce();

	public abstract Vect3D getSteeringForce();

}