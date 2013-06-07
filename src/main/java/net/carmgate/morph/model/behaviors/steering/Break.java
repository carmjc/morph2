package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.behaviors.common.Movement;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Entity;

public class Break extends Movement {

	private final Vect3D steeringForce = new Vect3D();
	private final Vect3D speedLastUpdate = new Vect3D();

	@Deprecated
	public Break() {
		// Nothing to do
	}

	public Break(Entity movable) {
		super(movable);
	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		return new Break(entity);
	}

	@Override
	public Vect3D getSteeringForce() {
		return steeringForce;
	}

	@Override
	public void run() {

		// if speed last update is in the same direction as current speed, then we still need to break
		if (speedLastUpdate.modulus() == 0 || speedLastUpdate.prodScal(movableEntity.getSpeed()) > 0) {
			steeringForce.nullify().substract(movableEntity.getSpeed()).normalize(movableEntity.getMaxSteeringForce());
			speedLastUpdate.copy(movableEntity.getSpeed());
		} else {
			// Stop it completely
			movableEntity.getSpeed().nullify();
			movableEntity.removeBehavior(this);
		}
	}
}
