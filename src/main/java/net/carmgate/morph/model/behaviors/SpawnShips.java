package net.carmgate.morph.model.behaviors;

import java.util.ArrayList;
import java.util.List;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.listener.DeathListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Do not use this behavior for ships spwaning ships.
 * This is intended for stations or other ships factories.
 */
public class SpawnShips implements Behavior, DeathListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpawnShips.class);

	private final List<Ship> spawnedShips = new ArrayList<>();
	private final int maxNumberOfShips;
	private final long spawnPeriod;
	private long lastSpawnTS;
	private final Ship modelShip;
	private final Vect3D spawnLocation;

	public SpawnShips(Vect3D spawnLocation, int maxNumberOfShips, long spawnPeriod, Ship modelShip) {
		this.spawnLocation = spawnLocation;
		this.maxNumberOfShips = maxNumberOfShips;
		this.spawnPeriod = spawnPeriod;
		this.modelShip = modelShip;

		// initialize lastSpawnTS
		lastSpawnTS = Model.getModel().getCurrentTS();
	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		return new SpawnShips(spawnLocation, maxNumberOfShips, spawnPeriod, modelShip);
	}

	@Override
	public void handleDeathEvent(Entity deadShip) {
		spawnedShips.remove(deadShip);
	}

	@Override
	public void run() {
		if (Model.getModel().getCurrentTS() - lastSpawnTS > spawnPeriod) {

			LOGGER.debug("children: " + spawnedShips.size());
			if (spawnedShips.size() < maxNumberOfShips) {
				// TODO The ship might not be the best place to clone a ship FOR this behavior ...
				Ship newShip = modelShip.clone();
				newShip.getPos().copy(spawnLocation);
				newShip.setHeading((float) (Math.random() * 360));
				newShip.addListener(this);
				Model.getModel().addEntity(newShip);
				spawnedShips.add(newShip);
			}

			lastSpawnTS += spawnPeriod;
		}
	}

}
