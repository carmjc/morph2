package net.carmgate.morph.model.behaviors.passive;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.model.orders.Die;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TakingDamage implements Behavior, Renderable {

	private static final Logger LOGGER = LoggerFactory.getLogger(TakingDamage.class);

	private Entity target;
	private float maxDamage;

	@Deprecated
	public TakingDamage() {
	}

	public TakingDamage(Entity target, float maxDamage) {
		this.target = target;
		this.maxDamage = maxDamage;

	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		return new TakingDamage(target, maxDamage);
	}

	@Override
	public void computeXpContribution() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initRenderer() {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(int glMode) {
		// Rendering of the order
		float explosionAngle = (float) (Math.random() * 180 + 90);
		for (int i = 0; i < 5; i++) {
			Model.getModel().getParticleEngine().addParticle(
					new Vect3D(target.getPos()),
					new Vect3D(target.getSpeed()).mult(0.25f).rotate((float) (explosionAngle + Math.random() * 5)).add(
							target.getSpeed()),
							2, 0.125f, 0.5f, 0.2f);
		}
	}

	@Override
	public void run() {
		float realDamage = maxDamage;

		LOGGER.debug("Taking damage: " + realDamage + "/" + maxDamage);
		// This is not multiplied by lastUpdateTS because the timing is handled by the sender of the event.
		// TODO It should take the shields into account
		target.setDamage(target.getDamage() + maxDamage);
		if (target.getDamage() > target.getMaxDamage()) {
			target.fireOrder(new Die());
		}
		maxDamage = 0;

		target.removeBehavior(this);
	}

}
