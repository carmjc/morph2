package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.Movement;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;

public class Arrive extends Movement {

	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);

	// Be careful, this is the real instance of the ship's position
	private Vect3D target;
	private final Vect3D desiredVelocity = new Vect3D();

	private float slowingDistance;
	private final Vect3D speedOpposition = new Vect3D();
	private final Vect3D targetOffset = new Vect3D();
	private final Vect3D normalizedTargetOffset = new Vect3D();
	private final Vect3D steeringForce = new Vect3D();

	/**
	 * Do not use.
	 */
	@Deprecated
	public Arrive() {
	}

	public Arrive(Ship shipToMove, Ship target) {
		super(shipToMove);
		this.target = target.getPos();
	}

	public Arrive(Ship shipToMove, Vect3D target) {
		super(shipToMove);
		this.target = target;
	}

	@Override
	public Vect3D getSteeringForce() {
		return steeringForce;
	}

	@Override
	public void initRenderer() {
		// Nothing to do
	}

	@Override
	public boolean isActive() {
		return target != null;
	}

	@Override
	public void render(int glMode) {
		final Vect3D pos = shipToMove.getPos();
		final Vect3D speed = shipToMove.getSpeed();

		GL11.glTranslatef(pos.x, pos.y, pos.z);
		if (Model.getModel().isDebugMode()) {
			speed.render(1);
			GL11.glColor3f(1, 0, 0);
			desiredVelocity.render(1);
			GL11.glTranslated(desiredVelocity.x, desiredVelocity.y, 0);
			GL11.glColor3f(0, 0, 1);
			steeringForce.render(1);
			GL11.glTranslated(-desiredVelocity.x, -desiredVelocity.y, 0);
			GL11.glColor3f(0, 1, 0);
			speedOpposition.render(1);
		}
		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);

		if (target != null && shipToMove.isSelected() && Model.getModel().isDebugMode()) {
			// Show target
			GL11.glTranslatef(target.x, target.y, 0);

			TextureImpl.bindNone();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(-16, -16);
			GL11.glVertex2f(16, -16);
			GL11.glVertex2f(16, 16);
			GL11.glVertex2f(-16, 16);
			GL11.glEnd();

			// render limit of effect zone
			GL11.glBegin(GL11.GL_LINES);
			float t = 0; // temporary data holder
			float x = slowingDistance; // radius
			float y = 0;
			for (int i = 0; i < nbSegments; i++) {
				GL11.glColor4d(1, 1, 1, 0.15);
				GL11.glVertex2d(x, y);

				t = x;
				x = cos * x - sin * y;
				y = sin * t + cos * y;

				GL11.glVertex2d(x, y);
			}
			GL11.glEnd();
			GL11.glTranslatef(-target.x, -target.y, 0);
		}
	}

	@Override
	public void run(float secondsSinceLastUpdate) {

		// Get some ship variables (must be final)
		final float mass = shipToMove.getMass();
		final Vect3D pos = new Vect3D(shipToMove.getPos());
		final Vect3D speed = new Vect3D(shipToMove.getSpeed());

		targetOffset.copy(target).substract(pos).mult(0.9f);

		normalizedTargetOffset.copy(targetOffset).normalize(1);
		speedOpposition.copy(normalizedTargetOffset).rotate(90).mult(speed.prodVectOnZ(normalizedTargetOffset));

		float cosSpeedToTO = 1;
		if (speed.modulus() != 0) {
			cosSpeedToTO = Math.abs(new Vect3D(speed).normalize(1).prodScal(normalizedTargetOffset));
		}

		// distance = length (target_offset)
		float distance = targetOffset.modulus();

		// Optimal slowing distance when cruising at MAX_SPEED before entering the slowing radius
		// Optimal slowing distance is computed for debugging purposes only
		slowingDistance = 0.00001f + (float) (Math.pow(speed.modulus(), 2) / (2 * Ship.MAX_FORCE / mass * cosSpeedToTO));

		// Ramped speed is the optimal target speed modulus
		float rampedSpeed = (float) Math.sqrt(2 * Ship.MAX_FORCE / mass * distance);
		// clipped_speed clips the speed to max speed
		float clippedSpeed = Math.min(rampedSpeed, Ship.MAX_SPEED);
		// desired_velocity would be the optimal speed vector if we had unlimited thrust
		desiredVelocity.copy(targetOffset).add(speedOpposition).mult(clippedSpeed / distance);

		steeringForce.copy(desiredVelocity).substract(speed);
		float factor = 1.35f;
		float sdmin = slowingDistance / factor;
		float sdmax = slowingDistance;
		float overdrive = 1.0f + speed.modulus() / Ship.MAX_SPEED;
		if (distance > sdmax) {
			steeringForce.truncate(Ship.MAX_FORCE / mass);
		} else if (distance > sdmin) {
			float stModulus = steeringForce.modulus();
			steeringForce.normalize((distance - sdmin) / (sdmax - sdmin) * stModulus + (sdmax - distance)
					/ (sdmax - sdmin) * Ship.MAX_FORCE / mass * overdrive);
		} else {
			steeringForce.normalize(Ship.MAX_FORCE / mass * overdrive);
		}

		// stop condition
		if (new Vect3D(target).substract(pos).modulus() < 5 && speed.modulus() < 60) {

			shipToMove.removeBehavior(this);

			// Stop and reset the behavior
			target = null;
			desiredVelocity.nullify();
			steeringForce.nullify();
			slowingDistance = 0;
			speedOpposition.nullify();
			targetOffset.nullify();
			normalizedTargetOffset.nullify();

			// stop ship
			// TODO we should do this some other way, there is a risk it collides with some other order
			// This might also be implemented with a physics cheating mecanism (see github issue #16)
			// -> Maybe we should send a stop order to the ship.
			shipToMove.getSpeed().nullify();
		}
	}

}