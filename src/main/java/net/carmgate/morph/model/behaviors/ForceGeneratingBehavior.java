package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.model.common.Vect3D;

public abstract class ForceGeneratingBehavior implements Behavior {

	public abstract Vect3D getNonSteeringForce();

}
