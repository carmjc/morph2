package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.ActivatedMorph;
import net.carmgate.morph.model.behaviors.Behavior;
import net.carmgate.morph.model.behaviors.Movement;
import net.carmgate.morph.model.behaviors.Needs;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.Planet;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Entity;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;

@Needs({ @ActivatedMorph(morphType = MorphType.SIMPLE_PROPULSOR) })
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

	public Arrive(Entity movable, Entity target) {
		super(movable);
		this.target = target.getPos();
	}

	public Arrive(Entity movable, Vect3D target) {
		super(movable);
		this.target = target;
	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		return new Arrive(entity, target);
	}

	@Override
	public Vect3D getSteeringForce() {
		return steeringForce;
	}

	public Vect3D getTarget() {
		return target;
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
		final Vect3D pos = movableEntity.getPos();
		final Vect3D speed = movableEntity.getSpeed();

		GL11.glTranslatef(pos.x, pos.y, pos.z);
		if (Model.getModel().getUiContext().isDebugMode()) {
			speed.render(glMode);
			GL11.glColor3f(1, 0, 0);
			desiredVelocity.render(glMode);
			GL11.glTranslated(desiredVelocity.x, desiredVelocity.y, 0);
			GL11.glColor3f(0, 0, 1);
			steeringForce.render(glMode);
			GL11.glTranslated(-desiredVelocity.x, -desiredVelocity.y, 0);
			GL11.glColor3f(0, 1, 0);
			speedOpposition.render(glMode);
		}
		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);

		if (target != null && (movableEntity instanceof Ship && movableEntity.isSelected() || movableEntity instanceof Planet)) {
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
					GL11.glColor4d(1, 0, 0, 0.15);
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
		final float mass = movableEntity.getMass();
		final Vect3D pos = new Vect3D(movableEntity.getPos());
		final Vect3D speed = new Vect3D(movableEntity.getSpeed());

		targetOffset.copy(target).substract(pos);

		if (targetOffset.modulus() == 0) {
			return;
		}

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
		slowingDistance = 0.00001f + (float) (Math.pow(speed.modulus(), 2) / (2 * movableEntity.getMaxSteeringForce() / mass * cosSpeedToTO));

		// Ramped speed is the optimal target speed modulus
		float rampedSpeed = (float) Math.sqrt(2 * movableEntity.getMaxSteeringForce() / mass * distance);
		// clipped_speed clips the speed to max speed
		float clippedSpeed = Math.min(rampedSpeed, movableEntity.getMaxSpeed());
		// desired_velocity would be the optimal speed vector if we had unlimited thrust
		desiredVelocity.copy(targetOffset).add(speedOpposition).mult(clippedSpeed / (distance + 0.0001f)); // + 0.000001 added for debug TODO

		steeringForce.copy(desiredVelocity).substract(speed).mult(mass);
		float factor = 1.35f;
		float sdmin = slowingDistance / factor;
		float sdmax = slowingDistance;
		float overdrive = 1.0f + speed.modulus() / rampedSpeed;
		if (distance > sdmax) {
			steeringForce.truncate(movableEntity.getMaxSteeringForce()); // / mass
		} else if (distance > sdmin) {
			float stModulus = steeringForce.modulus();
			steeringForce.normalize((distance - sdmin) / (sdmax - sdmin) * stModulus + (sdmax - distance)
					/ (sdmax - sdmin) * movableEntity.getMaxSteeringForce() * overdrive); // / mass
		} else {
			steeringForce.normalize(movableEntity.getMaxSteeringForce() * overdrive); // / mass
		}

		// stop condition
		// TODO remove the instanceof test
		if (new Vect3D(target).substract(pos).modulus() < 5 && speed.modulus() < 1 && movableEntity instanceof Ship) {

			movableEntity.removeBehavior(this);

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
			movableEntity.getSpeed().nullify();
		}
	}
}