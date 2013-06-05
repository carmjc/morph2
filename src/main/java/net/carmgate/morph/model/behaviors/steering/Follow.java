package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.ActivatedMorph;
import net.carmgate.morph.model.behaviors.Movement;
import net.carmgate.morph.model.behaviors.Needs;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Entity;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;

@Needs({ @ActivatedMorph(morphType = MorphType.SIMPLE_PROPULSOR) })
public class Follow extends Movement {

	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);

	// Be careful, this is the real instance of the ship's position
	private Entity target;
	private final Vect3D desiredVelocity = new Vect3D();

	private float slowingDistance;
	private final Vect3D speedOpposition = new Vect3D();
	private final Vect3D targetOffset = new Vect3D();
	private final Vect3D normalizedTargetOffset = new Vect3D();
	private final Vect3D steeringForce = new Vect3D();
	private Vect3D targetSpeed;
	private float maxDistance;

	/**
	 * Do not use.
	 */
	@Deprecated
	public Follow() {
	}

	public Follow(Entity shipToMove, Entity target, float maxDistance) {
		super(shipToMove);
		this.maxDistance = maxDistance;
		this.target = target;
		targetSpeed = target.getSpeed();
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

		if (target != null && movableEntity instanceof Ship && ((Entity) movableEntity).isSelected() && Model.getModel().getUiContext().isDebugMode()) {
			// Show target
			GL11.glTranslatef(target.getPos().x, target.getPos().y, 0);

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
			GL11.glTranslatef(-target.getPos().x, -target.getPos().y, 0);
		}
	}

	@Override
	public void run(float secondsSinceLastUpdate) {
		if (target.isDead()) {
			// Remove existing arrive and combat behaviors
			movableEntity.removeBehavior(this);

			// Add new arrive behavior
			// TODO Replace this with a simple break behavior
			// TODO Add a break behavior triggered by KEY_ESCAPE ?
			movableEntity.addBehavior(new Arrive(movableEntity, target.getPos()));

			return;
		}

		// Get some ship variables (must be final)
		final float mass = movableEntity.getMass();
		final Vect3D pos = new Vect3D(movableEntity.getPos());
		final Vect3D speed = new Vect3D(movableEntity.getSpeed());

		Vect3D recomputedTarget = new Vect3D(target.getPos()).add(new Vect3D(targetSpeed).truncate(targetSpeed.modulus() - maxDistance));
		targetOffset.copy(recomputedTarget).substract(pos);

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

		// desired_velocity would be the optimal speed vector if we had unlimited thrust
		desiredVelocity.copy(targetOffset).add(speedOpposition).normalize(distance);

		steeringForce.copy(desiredVelocity).substract(speed).mult(mass);

		// stop condition
		if (new Vect3D(recomputedTarget).substract(pos).modulus() < 5 && speed.modulus() < 60) {

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