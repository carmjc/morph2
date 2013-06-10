package net.carmgate.morph.model.entities.common.listener;

import net.carmgate.morph.model.entities.common.Entity;


public interface DeathListener {
	void handleDeathEvent(Entity deadShip);
}
