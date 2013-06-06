package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.conf.Conf.ConfItem;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.common.ActivatedMorph;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.behaviors.common.Needs;
import net.carmgate.morph.model.entities.Morph;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.orders.TakeDamage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Needs({ @ActivatedMorph(morphType = MorphType.LASER) })
public class InflictLaserDamage implements Behavior {
	private static final float MAX_DAMAGE_PER_HIT = 0.2f;

	private final Logger LOGGER = LoggerFactory.getLogger(InflictLaserDamage.class);

	/** rate of fire (nb/ms). */
	private static final float rateOfFire = 0.005f;
	public static final float MAX_RANGE = 800f;

	private final Ship sourceOfDamage;

	private final Ship target;

	private long timeOfLastAction;

	public InflictLaserDamage(Ship sourceOfDamage, Ship target) {
		this.sourceOfDamage = sourceOfDamage;
		this.target = target;
	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		// TODO This test should not have to be done
		// Something else than a ship should be able to inflict damage
		if (entity instanceof Ship) {
			return new InflictLaserDamage((Ship) entity, target);
		}

		return null;
	}

	public boolean consumeEnergy() {
		return sourceOfDamage.consumeEnergy(Model.getModel().getSecondsSinceLastUpdate()
				* MorphType.LASER.getEnergyConsumption());
	}

	@Override
	public boolean isActive() {
		return target != null;
	}

	@Override
	public void run(float secondsSinceLastUpdate) {
		if (target.isDead()) {
			sourceOfDamage.removeBehavior(this);
		}

		// TODO The damage amount taken from the target take into account the target's speed, distance and size.
		// TODO The damage sent to the target should take into account current morphs' xp, level and type.
		// TODO This should also be updated to cope with the improbable possibility that the refresh rate is insufficient to handle
		// the orders one by one. (currentTs - timeOfLastAction / rateOfFire > 2)
		if (timeOfLastAction == 0 || (Model.getModel().getCurrentTS() - timeOfLastAction) * rateOfFire > 1) {
			if (target.getPos().distance(sourceOfDamage.getPos()) < MAX_RANGE && consumeEnergy()) {
				target.fireOrder(new TakeDamage(MAX_DAMAGE_PER_HIT));

				for (Morph morph : sourceOfDamage.getMorphsByType(MorphType.LASER)) {
					morph.increaseXp(Conf.getFloatProperty(ConfItem.MORPH_LASER_MAXXPPERHIT));
				}
			}

			// TODO There seems to be an issue with this.
			// It seems that if the ship has to run long before hitting the target, it hammers heavier on it ...
			// Sometimes, it takes 20s to kill a target, sometimes it takes 3s to bring done a clone of the former.
			timeOfLastAction += 1 / rateOfFire;
		}
	}
}