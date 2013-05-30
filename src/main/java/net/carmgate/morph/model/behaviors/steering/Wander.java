package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.behaviors.Movement;
import net.carmgate.morph.model.behaviors.ActivatedMorph;
import net.carmgate.morph.model.behaviors.Needs;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.Ship;

@Needs({ @ActivatedMorph(morphType = MorphType.SIMPLE_PROPULSOR) })
public class Wander extends Movement {

	private final float wanderFocusDistance;
	private final float wanderRadius;
	private final Vect3D steeringForce = new Vect3D();

	private final Vect3D wanderTarget = new Vect3D();

	/**
	 * Do not use.
	 */
	@Deprecated
	public Wander() {
		wanderFocusDistance = 0;
		wanderRadius = 0;
	}

	public Wander(Ship shipToMove, float wanderFocusDistance, float wanderRadius) {
		super(shipToMove);
		this.wanderFocusDistance = wanderFocusDistance;
		this.wanderRadius = wanderRadius;
	}

	@Override
	public Vect3D getSteeringForce() {
		return steeringForce;
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
			shipToMove.removeBehavior(this);
			return;
		}

		final Vect3D pos = new Vect3D(shipToMove.getPos());

		// Update target within the given constraints
		Vect3D wanderFocus = new Vect3D(pos).add(new Vect3D(0, 1, 0).rotate(shipToMove.getHeading()).normalize(wanderFocusDistance + shipToMove.getMass()));

		// Determine a target at acceptable distance from the wander focus point
		wanderTarget.x += Math.random() * 0.25f - 0.125f;
		wanderTarget.y += Math.random() * 0.25f - 0.125f;
		if (new Vect3D(wanderFocus).add(wanderTarget).distance(wanderFocus) > wanderRadius) {
			wanderTarget.copy(Vect3D.NULL);
		}

		steeringForce.copy(new Vect3D(wanderFocus).substract(pos).add(wanderTarget)).truncate(shipToMove.getMaxSteeringForce() / shipToMove.getMass());
	}

}