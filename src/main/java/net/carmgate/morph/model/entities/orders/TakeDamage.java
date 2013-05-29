package net.carmgate.morph.model.entities.orders;

public class TakeDamage implements Order {

	private final float damageAmount;

	public TakeDamage(float damageAmount) {
		this.damageAmount = damageAmount;
	}

	public float getDamageAmount() {
		return damageAmount;
	}

}
