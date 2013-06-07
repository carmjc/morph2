package net.carmgate.morph.model.behaviors.common;

import net.carmgate.morph.model.entities.common.Entity;

public interface Behavior extends Cloneable {

	Behavior cloneForEntity(Entity entity);

	abstract void run();
}
