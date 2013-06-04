package net.carmgate.morph.model.entities.common;

import net.carmgate.morph.model.behaviors.Behavior;
import net.carmgate.morph.model.common.Vect3D;

// TODO we must clean this a bit
public interface Movable {
	void addBehavior(Behavior behavior);

	float getHeading();

	float getMass();

	float getMaxSpeed();

	float getMaxSteeringForce();

	Vect3D getPos();

	Vect3D getSpeed();

	void removeBehavior(Behavior behavior);

}
