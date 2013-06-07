package net.carmgate.morph.model.behaviors.common;

import net.carmgate.morph.model.entities.common.Entity;

public interface Behavior extends Cloneable {

	Behavior cloneForEntity(Entity entity);

	// TODO It seems it's not useful anymore. It should be a good idea to get rid of it
	@Deprecated
	abstract boolean isActive();

	abstract void run();
}
