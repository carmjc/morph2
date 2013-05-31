package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.Star;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.model.orders.Die;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StarsContribution extends ForceGeneratingBehavior {

	private static final Logger LOGGER = LoggerFactory.getLogger(StarsContribution.class);

	private final Ship ship;

	private final Vect3D force = new Vect3D();

	public StarsContribution(Ship ship) {
		this.ship = ship;
	}

	@Override
	public Vect3D getNonSteeringForce() {
		return force;
	}

	@Override
	public boolean isActive() {
		// This behavior is always active
		return true;
	}

	@Override
	public void run(float secondsSinceLastUpdate) {
		for (Entity entity : Model.getModel().getEntitiesByType(EntityType.STAR).values()) {
			Star star = (Star) entity;
			force.copy(star.getPos()).substract(ship.getPos());
			float distance = force.modulus();

			// if the ship enters the star, it's destroyed
			if (distance < star.getKillingRadius()) {
				// TODO This might be better handled by a destruction order given to the ship
				ship.fireOrder(new Die());
				;
			}

			// Adds the gravity pulling force
			force.normalize(1).mult((float) (star.getGm() * ship.getMass() / (distance * distance)));

			// Add energy inflow
			float energyInflux = (float) (star.getEnergyFlow() * Math.sqrt(ship.getMass()) / ship.getPos().distance(star.getPos()));
			ship.addEnergy(energyInflux * Model.getModel().getSecondsSinceLastUpdate());

			// TODO Add overflow energy induced damage
		}
	}
}
