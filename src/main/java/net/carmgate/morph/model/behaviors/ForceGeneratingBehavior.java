package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.model.common.Vect3D;

public interface ForceGeneratingBehavior extends Behavior {

	public abstract Vect3D getNonSteeringForce();

}
