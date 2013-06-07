package net.carmgate.morph.model.entities.common;

import net.carmgate.morph.model.entities.Ship;

public interface DeathListener {
	void handleDeathEvent(Ship deadShip);
}
