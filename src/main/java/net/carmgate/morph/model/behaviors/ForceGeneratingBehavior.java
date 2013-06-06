package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Entity;

public abstract class ForceGeneratingBehavior implements Behavior {

	@Override
	public abstract Behavior cloneForEntity(Entity entity);

	public abstract Vect3D getNonSteeringForce();

}
