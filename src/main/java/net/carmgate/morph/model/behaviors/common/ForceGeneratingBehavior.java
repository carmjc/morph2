package net.carmgate.morph.model.behaviors.common;

import net.carmgate.morph.model.common.Vect3D;

public abstract class ForceGeneratingBehavior implements Behavior {

	// TODO What is the difference between a steering and a non steering force ?
	public abstract Vect3D getNonSteeringForce();

}
