package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Renderable;

public abstract class Movement implements Behavior, Renderable {
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

	public abstract Vect3D getSteeringForce();

}