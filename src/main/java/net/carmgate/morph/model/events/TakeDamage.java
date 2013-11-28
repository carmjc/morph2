package net.carmgate.morph.model.events;

import net.carmgate.morph.model.entities.common.Entity;

public class TakeDamage implements Event {

	private final Entity sourceOfDamage;
	private final float damageAmount;

	public TakeDamage(Entity sourceOfDamage, float damageAmount) {
		this.sourceOfDamage = sourceOfDamage;
		this.damageAmount = damageAmount;
	}

	public float getDamageAmount() {
		return damageAmount;
	}

	public Entity getSourceOfDamage() {
		return sourceOfDamage;
	}

}
