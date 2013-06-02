package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.Behavior;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Planet;
import net.carmgate.morph.model.entities.common.Renderable;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;

// Replace this with an Arrive behavior compatible with planets 
public class ArriveForPlanet implements Behavior, Renderable {

	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);
	private static final int MAX_STEERING_FORCE = 10000;
	public static final float MAX_SPEED = 10000;

	// Be careful, this is the real instance of the ship's position
	private final Vect3D target;
	private final Vect3D desiredVelocity = new Vect3D();

	private float slowingDistance;
	private final Vect3D speedOpposition = new Vect3D();
	private final Vect3D targetOffset = new Vect3D();
	private final Vect3D normalizedTargetOffset = new Vect3D();
	private final Vect3D steeringForce = new Vect3D();

	private final Planet planetToMove;

	/**
	 * Do not use.
	 */
	@Deprecated
	public ArriveForPlanet() {
		this(null, null);
	}

	public ArriveForPlanet(Planet planetToMove, Vect3D target) {
		this.target = target;
		this.planetToMove = planetToMove;
	}

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
		final Vect3D pos = planetToMove.getPos();
		final Vect3D speed = planetToMove.getSpeed();

		GL11.glTranslatef(pos.x, pos.y, pos.z);
		if (Model.getModel().getUiContext().isDebugMode()) {
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

		if (target != null && planetToMove.isSelected()) {
			// Show target
			GL11.glTranslatef(target.x, target.y, 0);

			float zoomFactor = Model.getModel().getViewport().getZoomFactor();
			GL11.glScalef(1f / zoomFactor, 1f / zoomFactor, 1);
			TextureImpl.bindNone();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(-3, -3);
			GL11.glVertex2f(3, -3);
			GL11.glVertex2f(3, 3);
			GL11.glVertex2f(-3, 3);
			GL11.glEnd();

			// render limit of effect zone
			GL11.glBegin(GL11.GL_LINES);
			float t = 0; // temporary data holder
			float x = 15; // radius
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

			if (Model.getModel().getUiContext().isDebugMode()) {
				// render limit of effect zone
				GL11.glBegin(GL11.GL_LINES);
				t = 0; // temporary data holder
				x = slowingDistance; // radius
				y = 0;
				for (int i = 0; i < nbSegments; i++) {
					GL11.glColor4d(1, 1, 1, 0.15);
					GL11.glVertex2d(x, y);

					t = x;
					x = cos * x - sin * y;
					y = sin * t + cos * y;

					GL11.glVertex2d(x, y);
				}
				GL11.glEnd();
			}

			GL11.glScalef(zoomFactor, zoomFactor, 1);
			GL11.glTranslatef(-target.x, -target.y, 0);
		}
	}

	@Override
	public void run(float secondsSinceLastUpdate) {

		// Get some ship variables (must be final)
		final float mass = planetToMove.getMass();
		final Vect3D pos = new Vect3D(planetToMove.getPos());
		final Vect3D speed = new Vect3D(planetToMove.getSpeed());

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
		slowingDistance = 0.00001f + (float) (Math.pow(speed.modulus(), 2) / (2 * MAX_STEERING_FORCE / mass * cosSpeedToTO));

		// Ramped speed is the optimal target speed modulus
		float rampedSpeed = (float) Math.sqrt(2 * MAX_STEERING_FORCE / mass * distance);
		// clipped_speed clips the speed to max speed
		float clippedSpeed = Math.min(rampedSpeed, MAX_SPEED);
		// desired_velocity would be the optimal speed vector if we had unlimited thrust
		desiredVelocity.copy(targetOffset).add(speedOpposition).mult(clippedSpeed / distance);

		steeringForce.copy(desiredVelocity).substract(speed);
		float factor = 1.35f;
		float sdmin = slowingDistance / factor;
		float sdmax = slowingDistance;
		float overdrive = 1.0f + speed.modulus() / MAX_SPEED;
		if (distance > sdmax) {
			steeringForce.truncate(MAX_STEERING_FORCE / mass);
		} else if (distance > sdmin) {
			float stModulus = steeringForce.modulus();
			steeringForce.normalize((distance - sdmin) / (sdmax - sdmin) * stModulus + (sdmax - distance)
					/ (sdmax - sdmin) * MAX_STEERING_FORCE / mass * overdrive);
		} else {
			steeringForce.normalize(MAX_STEERING_FORCE / mass * overdrive);
		}

	}

}