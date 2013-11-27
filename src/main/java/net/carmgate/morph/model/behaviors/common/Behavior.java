package net.carmgate.morph.model.behaviors.common;

import net.carmgate.morph.model.entities.common.Entity;

public interface Behavior extends Cloneable {

	Behavior cloneForEntity(Entity entity);

	/**
	 * This method computes the amount of xp awarded to the ship's morphs by this behavior
	 */
	void computeXpContribution();

	void run();
}
