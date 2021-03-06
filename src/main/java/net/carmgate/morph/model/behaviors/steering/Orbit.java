package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.Constants;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.StarsContribution;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.behaviors.common.Movement;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.ui.common.RenderUtils;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Orbit extends Movement {

	private static final Logger LOGGER = LoggerFactory.getLogger(Orbit.class);

	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);

	private final float orbitRadius;
	private final Entity orbitee;

	private Arrive arrive;
	private final Vect3D steeringForce = new Vect3D();

	private final Vect3D tangentialForce = new Vect3D();

	// TODO rework this ... awful thing
	private StarsContribution starsContribution;

	private boolean stable;

	private final boolean instantOrbit;

	@Deprecated
	public Orbit() {
		this(null, null, 0, false);
	}

	public Orbit(Entity orbiter, Entity orbitee, float orbitRadius, boolean instantOrbit) {
		super(orbiter);
		this.orbitee = orbitee;
		this.orbitRadius = orbitRadius;
		this.instantOrbit = instantOrbit;
		if (movableEntity != null && orbitee != null) {
			Vect3D orbiteeToOrbiter = new Vect3D(movableEntity.getPos()).substract(orbitee.getPos());
			Vect3D orbitalTarget = new Vect3D(orbiteeToOrbiter).normalize(orbitRadius).add(orbitee.getPos());

			if (!instantOrbit) {
				arrive = new Arrive(movableEntity, orbitalTarget);
			}
		} else {
			if (movableEntity != null) {
				throw new IllegalStateException();
			}
		}
	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		return new Orbit(entity, orbitee, orbitRadius, instantOrbit);
	}

	@Override
	public Vect3D getSteeringForce() {
		return steeringForce;
	}

	@Override
	public void render(int glMode) {

		GL11.glTranslatef(orbitee.getPos().x, orbitee.getPos().y, orbitee.getPos().z);

		TextureImpl.bindNone();
		RenderUtils.renderCircle(orbitRadius, 5 / Model.getModel().getViewport().getZoomFactor(),
				new Float[] { 0f, 0f, 0f, 0f }, new Float[] { 1f, 1f, 1f, 0.3f }, new Float[] { 0f, 0f, 0f, 0f });

		GL11.glTranslatef(-orbitee.getPos().x, -orbitee.getPos().y, -orbitee.getPos().z);

		if (Model.getModel().getUiContext().isDebugMode()) {
			if (arrive != null) {
				arrive.render(glMode);
			}

			GL11.glTranslatef(movableEntity.getPos().x, movableEntity.getPos().y, movableEntity.getPos().z);
			GL11.glColor4f(1, 0, 1, 0.5f);
			movableEntity.getSpeed().render(glMode);
			tangentialForce.render(glMode);
			GL11.glColor4f(0, 0, 1, 1);
			getSteeringForce().render(glMode);
			GL11.glColor4f(1f, 1f, 0f, 1);
			new Vect3D(starsContribution.getNonSteeringForce()).add(steeringForce).render(glMode);
			GL11.glTranslatef(-movableEntity.getPos().x, -movableEntity.getPos().y, -movableEntity.getPos().z);
		}
	}

	@Override
	public void run() {

		steeringForce.nullify();
		Vect3D orbiteeToOrbiter = new Vect3D(movableEntity.getPos()).substract(orbitee.getPos());
		Vect3D radialVector = new Vect3D(orbiteeToOrbiter).normalize(1);
		Vect3D tangentialVector = new Vect3D(radialVector).rotate(90);
		float optimalSpeed = (float) Math.sqrt(Constants.SIMPLE_G * (orbitee.getMass() + movableEntity.getMass()) / orbitRadius);

		if (stable || instantOrbit) {
			// Cheating to stay in orbit
			// IMPROVE we should check that the non steering force have not changed
			// However, this behavior should not concern ship's or playable entities anywhere in the future
			// TODO we might do that far less often
			movableEntity.getPos().substract(orbitee.getPos()).normalize(orbitRadius).add(orbitee.getPos());
			movableEntity.getSpeed().copy(tangentialVector).normalize(optimalSpeed);
			if (movableEntity instanceof Ship) {
				LOGGER.debug("stable2");
			}
			return;
		}

		if (tangentialForce.prodScal(movableEntity.getSpeed()) < 0) {
			// rotate in speed vector direction if not purely radial
			tangentialForce.mult(-1);
		}

		// If the orbiter is not on the desired distance, set an arrive behavior to go to it
		// TODO there is a problem with arrive nullifying target
		if (arrive != null && arrive.getTarget() != null) {
			Vect3D orbitalTarget = new Vect3D(orbiteeToOrbiter).normalize(orbitRadius).add(orbitee.getPos());
			arrive.getTarget().copy(orbitalTarget);
			arrive.run();
			steeringForce.add(new Vect3D(radialVector).normalize(arrive.getSteeringForce().prodScal(radialVector)));
			steeringForce.substract(starsContribution.getNonSteeringForce());
		}

		tangentialForce.copy(tangentialVector);
		float test = new Vect3D(tangentialVector).prodScal(new Vect3D(movableEntity.getSpeed()).normalize(1));
		if (test == 0) {
			test = 1;
		}
		tangentialForce.normalize((optimalSpeed - movableEntity.getSpeed().modulus())
				* test);
		tangentialForce.mult(movableEntity.getMass());
		steeringForce.add(tangentialForce); // .substract(movableEntity.getSpeed());
		// LOGGER.debug("" + movableEntity.getClass().getSimpleName() + ": " + steeringForce + ", optimal: " + optimalSpeed + ", current: "
		// + movableEntity.getSpeed().modulus());

		if (Math.abs(Math.abs(new Vect3D(movableEntity.getSpeed()).prodScal(tangentialVector)) - optimalSpeed) < optimalSpeed / 200
				&& Math.abs(orbiteeToOrbiter.modulus() - orbitRadius) < 0.01) {
			movableEntity.getSpeed().copy(tangentialVector).mult(optimalSpeed);
			LOGGER.debug("now stable");
			stable = true;
			steeringForce.nullify();
			tangentialForce.nullify();
			arrive = null;
		} else {
			if (movableEntity instanceof Ship) {
				LOGGER.debug(Math.abs(Math.abs(new Vect3D(movableEntity.getSpeed()).prodScal(tangentialVector)) - optimalSpeed) + "/" + optimalSpeed / 200);
			}
		}

	}

	public void setStarsContribution(StarsContribution starsContribution) {
		this.starsContribution = starsContribution;
	}
}
