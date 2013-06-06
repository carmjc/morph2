package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.model.entities.common.Entity;

public interface Behavior extends Cloneable {

	Behavior cloneForEntity(Entity entity);

	// TODO It seems it's not useful anymore. It should be a good idea to get rid of it
	@Deprecated
	abstract boolean isActive();

	// TODO Remove the parameter since it's served by the model now
	abstract void run(float secondsSinceLastUpdate);
}
