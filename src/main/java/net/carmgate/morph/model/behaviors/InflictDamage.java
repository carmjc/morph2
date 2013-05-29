package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.orders.TakeDamage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InflictDamage implements Behavior {
	private final Logger LOGGER = LoggerFactory.getLogger(InflictDamage.class);

	/** rate of fire (nb/ms). */
	private static final float rateOfFire = 0.001f;
	public static final float MAX_RANGE = 800f;

	private final Ship sourceOfDamage;

	private final Ship target;

	private long timeOfLastAction;

	public InflictDamage(Ship sourceOfDamage, Ship target) {
		this.sourceOfDamage = sourceOfDamage;
		this.target = target;
	}

	@Override
	public boolean isActive() {
		return target != null;
	}

	@Override
	public void run(float secondsSinceLastUpdate) {
		// TODO The damage amount taken from the target take into account the target's speed, distance and size.
		// TODO The damage sent to the target should take into account current morphs' xp, level and type.
		// TODO This should also be updated to cope with the improbable possibility that the refresh rate is insufficient to handle
		// the orders one by one. (currentTs - timeOfLastAction / rateOfFire > 2)
		if (timeOfLastAction == 0 || (Model.getModel().getCurrentTS() - timeOfLastAction) * rateOfFire > 1) {
			if (target.getPos().distance(sourceOfDamage.getPos()) < MAX_RANGE) {
				target.fireOrder(new TakeDamage(0.1f));
			} else {
				LOGGER.debug("" + target.getPos().distance(sourceOfDamage.getPos()));
			}
			timeOfLastAction += 1 / rateOfFire;
		}
	}
}