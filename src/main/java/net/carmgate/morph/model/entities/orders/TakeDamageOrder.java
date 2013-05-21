package net.carmgate.morph.model.entities.orders;

public class TakeDamageOrder implements Order {

	private final int damageAmount;

	public TakeDamageOrder(int damageAmount) {
		this.damageAmount = damageAmount;
	}

	public int getDamageAmount() {
		return damageAmount;
	}

}
