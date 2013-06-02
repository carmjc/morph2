package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.Behavior;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Planet;
import net.carmgate.morph.model.entities.Star;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.ui.common.RenderUtils;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Orbit implements Behavior, Renderable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Orbit.class);

	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);

	private final float orbitRadius;
	private final Planet orbiter;
	private final Star orbitee;

	private ArriveForPlanet arrive;
	private final Vect3D steeringForce = new Vect3D();

	@Deprecated
	public Orbit() {
		this(null, null, 0);
	}

	// TODO Replace orbiter with a type that would encompass any movable entity
	public Orbit(Planet orbiter, Star orbitee, float orbitRadius) {
		this.orbiter = orbiter;
		this.orbitee = orbitee;
		this.orbitRadius = orbitRadius;
	}

	public Vect3D getSteeringForce() {
		return steeringForce;
	}

	@Override
	public void initRenderer() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void render(int glMode) {

		// LOGGER.debug("orbit around " + orbitee.getPos() + " at " + orbitRadius);
		GL11.glTranslatef(orbitee.getPos().x, orbitee.getPos().y, orbitee.getPos().z);

		TextureImpl.bindNone();
		RenderUtils.renderCircle(orbitRadius, 2, new Float[] { 0f, 0f, 0f, 0f }, new Float[] { 1f, 1f, 1f, 1f }, new Float[] { 0f, 0f, 0f, 0f });

		GL11.glTranslatef(-orbitee.getPos().x, -orbitee.getPos().y, -orbitee.getPos().z);
	}

	@Override
	public void run(float secondsSinceLastUpdate) {
		// If the orbiter is not on the desired distance, set an arrive behavior to go to it
		if (arrive == null) {
			Vect3D orbitalTarget = new Vect3D(orbiter.getPos()).substract(orbitee.getPos()).normalize(orbitRadius).add(orbitee.getPos());
			arrive = new ArriveForPlanet(orbiter, orbitalTarget);
		}
		arrive.run(Model.getModel().getSecondsSinceLastUpdate());
		steeringForce.copy(arrive.getSteeringForce());

		float optimalSpeed = (float) Math.sqrt(Star.SIMPLE_G * (orbitee.getMass() + orbiter.getMass()) / orbitRadius);

		if (orbiter.getSpeed().modulus() < optimalSpeed) {
			Vect3D tangentialForce = new Vect3D(orbiter.getPos()).substract(orbitee.getPos()).rotate(90);
			tangentialForce.mult(tangentialForce.prodScal(orbiter.getSpeed())).normalize(optimalSpeed - orbiter.getSpeed().modulus());
			steeringForce.add(tangentialForce);
		}

		// When the orbiter is at the right distance of the orbitee but not at orbiting speed (Math.sqrt(G*(M+m)/r)),
		// Keep the arriving behavior but add a tangential acceleration to circularize it

		// When it is circularized, cheat and make the orbit permanent until the non steering force changes.
		// This will allow to limit the computation but allow to de-circularize the orbiter
	}
}
