package net.carmgate.morph.model.behaviors.common;

import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.conf.Conf.ConfItem;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.Renderable;

public abstract class Movement implements Behavior, Renderable {
	protected final Entity movableEntity;

	/**
	 * Do not use.
	 */
	@Deprecated
	protected Movement() {
		movableEntity = null;
	}

	protected Movement(Entity movable) {
		movableEntity = movable;
	}

	@Override
	public abstract Behavior cloneForEntity(Entity entity);

	// TODO the energy consumption should depend on the number and level of the propulsor morphs
	public boolean consumeEnergy() {
		if (movableEntity instanceof Ship) {
			float energyDec = Model.getModel().getSecondsSinceLastUpdate()
					* MorphType.SIMPLE_PROPULSOR.getEnergyConsumption()
					* ((Ship) movableEntity).getRealAccelModulus() / movableEntity.getMaxSteeringForce();
			return ((Ship) movableEntity).consumeEnergy(energyDec);
		}
		return false;
	}

	public abstract Vect3D getSteeringForce();

	@Override
	public void initRenderer() {
		// Empty impl for adapting
	}

	@Override
	public void render(int glMode) {
		// Empty impl for adapting
	}

	public void rewardMorphs() {
		if (movableEntity instanceof Ship) {
			for (Morph morph : ((Ship) movableEntity).getMorphsByType(MorphType.SIMPLE_PROPULSOR)) {
				morph.increaseXp(((Ship) movableEntity).getRealAccelModulus() / movableEntity.getMaxSteeringForce()
						* Model.getModel().getSecondsSinceLastUpdate()
						* Conf.getFloatProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXXPPERSECOND));
			}
		}
	}

}