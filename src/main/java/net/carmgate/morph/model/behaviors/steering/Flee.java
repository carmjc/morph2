package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.common.ActivatedMorph;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.behaviors.common.Movement;
import net.carmgate.morph.model.behaviors.common.Needs;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Entity;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;

@Needs({ @ActivatedMorph(morphType = MorphType.SIMPLE_PROPULSOR) })
public class Flee extends Movement {

	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);

	// Be careful, this is the real instance of the ship's position
	private Entity target;
	private final Vect3D desiredVelocity = new Vect3D();

	private float slowingDistance;
	private float minDist;
	// private final Vect3D speedOpposition = new Vect3D();
	// private final Vect3D targetOffset = new Vect3D();
	// private final Vect3D normalizedTargetOffset = new Vect3D();
	private final Vect3D steeringForce = new Vect3D();

	// private Vect3D targetSpeed;
	/**
	 * Do not use.
	 */
	@Deprecated
	public Flee() {
		super(null);
	}

	public Flee(Entity shipToMove, Entity target, float minDist) {
		super(shipToMove);
		this.target = target;
		this.minDist = minDist;
		// targetSpeed = target.getSpeed();
	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		return new Flee(entity, target, minDist);
	}

	@Override
	public Vect3D getSteeringForce() {
		return steeringForce;
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
			// GL11.glColor3f(0, 1, 0);
			// speedOpposition.render(glMode);
		}
		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);

		if (target != null && movableEntity instanceof Ship && movableEntity.isSelected() && Model.getModel().getUiContext().isDebugMode()) {
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
	public void run() {
		if (target.isDead()) {
			// Remove existing arrive and combat behaviors
			movableEntity.removeBehavior(this);
			// TODO We might want to switch to previous behavior when leaving this one instead of breaking
			movableEntity.addBehavior(new Break(movableEntity));

			return;
		}

		// Get some ship variables (must be final)
		final float mass = movableEntity.getMass();
		final Vect3D pos = new Vect3D(movableEntity.getPos());
		final Vect3D speed = new Vect3D(movableEntity.getSpeed());

		desiredVelocity.copy(new Vect3D(pos).substract(target.getPos())).mult(movableEntity.getMaxSpeed());

		steeringForce.copy(desiredVelocity).substract(speed).mult(mass);

	}
}