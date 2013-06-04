package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.conf.Conf.ConfItem;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Movable;
import net.carmgate.morph.model.entities.common.Renderable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Movement implements Behavior, Renderable {
	private static final Logger LOGGER = LoggerFactory.getLogger(Movement.class);

	protected final Movable shipToMove;

	/**
	 * Do not use.
	 */
	@Deprecated
	protected Movement() {
		shipToMove = null;
	}

	protected Movement(Movable shipToMove) {
		this.shipToMove = shipToMove;
	}

	// TODO the energy consumption should depend on the number and level of the propulsor morphs
	public boolean consumeEnergy() {
		if (shipToMove instanceof Ship) {
			float energyDec = Model.getModel().getSecondsSinceLastUpdate()
					* MorphType.SIMPLE_PROPULSOR.getEnergyConsumption()
					* ((Ship) shipToMove).getRealAccelModulus() / ((Ship) shipToMove).getMaxSteeringForce();
			return ((Ship) shipToMove).consumeEnergy(energyDec);
		}
		return false;
	}

	public abstract Vect3D getSteeringForce();

	public void rewardMorphs() {
		if (shipToMove instanceof Ship) {
			for (Morph morph : ((Ship) shipToMove).getMorphsByType(MorphType.SIMPLE_PROPULSOR)) {
				morph.increaseXp(((Ship) shipToMove).getRealAccelModulus() / ((Ship) shipToMove).getMaxSteeringForce()
						* Model.getModel().getSecondsSinceLastUpdate()
						* Conf.getFloatProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXXPPERSECOND));
			}
		}
	}

}