package net.carmgate.morph.model.behaviors;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.Star;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.model.orders.Die;

import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StarsContribution extends ForceGeneratingBehavior implements Renderable {

	private static final Logger LOGGER = LoggerFactory.getLogger(StarsContribution.class);

	private final Entity movable;

	private final Vect3D force = new Vect3D();

	public StarsContribution() {
		this(null);
	}

	public StarsContribution(Entity ship) {
		movable = ship;
	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		return new StarsContribution(entity);
	}

	@Override
	public Vect3D getNonSteeringForce() {
		return force;
	}

	@Override
	public void initRenderer() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActive() {
		// This behavior is always active
		return true;
	}

	@Override
	public void render(int glMode) {
		GL11.glTranslatef(movable.getPos().x, movable.getPos().y, movable.getPos().z);
		if (Model.getModel().getUiContext().isDebugMode()) {
			GL11.glColor4f(1, 0, 0, 1);
			force.render(glMode);
		}
		GL11.glTranslatef(-movable.getPos().x, -movable.getPos().y, -movable.getPos().z);
	}

	@Override
	public void run(float secondsSinceLastUpdate) {
		for (Entity entity : Model.getModel().getEntitiesByType(EntityType.STAR).values()) {
			Star star = (Star) entity;
			force.copy(star.getPos()).substract(movable.getPos());
			float distance = force.modulus();

			// if the ship enters the star, it's destroyed
			if (distance < star.getKillingRadius() && movable instanceof Ship) {
				// TODO This might be better handled by a destruction order given to the ship
				((Ship) movable).fireOrder(new Die());
			}

			// Adds the gravity pulling force
			force.normalize(1).mult((float) (star.getGm() * movable.getMass() / (distance * distance)));

			// Add energy inflow
			if (movable instanceof Ship) {
				float energyInflux = (float) (star.getEnergyFlow() * Math.sqrt(movable.getMass()) / movable.getPos().distance(star.getPos()));
				((Ship) movable).addEnergy(energyInflux * Model.getModel().getSecondsSinceLastUpdate());
			}

			// TODO Add overflow energy induced damage
		}
	}
}
