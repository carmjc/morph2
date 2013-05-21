package net.carmgate.morph.model.entities.orders;

public class TakeDamageOrder implements Order {

	private final float damageAmount;

	public TakeDamageOrder(float damageAmount) {
		this.damageAmount = damageAmount;
	}

	public float getDamageAmount() {
		return damageAmount;
	}

}
