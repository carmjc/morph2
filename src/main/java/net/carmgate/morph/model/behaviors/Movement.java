package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Renderable;

public abstract class Movement implements Behavior, Renderable {
	protected final Ship shipToMove;

	/**
	 * Do not use.
	 */
	@Deprecated
	protected Movement() {
		shipToMove = null;
	}

	protected Movement(Ship shipToMove) {
		this.shipToMove = shipToMove;
	}

	public abstract Vect3D getSteeringForce();

}