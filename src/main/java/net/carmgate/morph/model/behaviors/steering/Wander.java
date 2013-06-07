package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.common.ActivatedMorph;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.behaviors.common.Movement;
import net.carmgate.morph.model.behaviors.common.Needs;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.common.Entity;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.TextureImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Needs({ @ActivatedMorph(morphType = MorphType.SIMPLE_PROPULSOR) })
public class Wander extends Movement {

	private static final Logger LOGGER = LoggerFactory.getLogger(Wander.class);

	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);

	protected final float wanderFocusDistance;
	protected final float wanderRadius;
	protected float wanderAngle;
	private final Vect3D steeringForce = new Vect3D();

	/**
	 * Do not use.
	 */
	@Deprecated
	public Wander() {
		wanderFocusDistance = 0;
		wanderRadius = 0;
		wanderAngle = 0;
	}

	public Wander(Entity shipToMove, float wanderFocusDistance, float wanderRadius) {
		super(shipToMove);
		this.wanderFocusDistance = wanderFocusDistance;
		this.wanderRadius = wanderRadius;
		if (shipToMove != null) {
			wanderAngle = (float) (Math.random() * 360);
		}
	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		return new Wander(entity, wanderFocusDistance, wanderRadius);
	}

	@Override
	public Vect3D getSteeringForce() {
		return steeringForce;
	}

	@Override
	public void initRenderer() {
		// nothing to do
	}

	@Override
	public void render(int glMode) {
		final Vect3D pos = movableEntity.getPos();
		final Vect3D speed = movableEntity.getSpeed();

		if (Model.getModel().getUiContext().isDebugMode()) {
			GL11.glTranslatef(pos.x, pos.y, pos.z);
			speed.render(glMode);
			GL11.glColor3f(0, 0, 1);
			steeringForce.render(glMode);

			// render limit of effect zone
			GL11.glBegin(GL11.GL_LINES);
			float t = 0; // temporary data holder
			float x = wanderFocusDistance; // radius
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

			GL11.glRotatef(movableEntity.getHeading(), 0, 0, 1);
			GL11.glTranslatef(0, -wanderFocusDistance, 0);

			// render limit of effect zone
			GL11.glBegin(GL11.GL_LINES);
			t = 0; // temporary data holder
			x = wanderRadius; // radius
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

			GL11.glRotatef(wanderAngle - movableEntity.getHeading(), 0, 0, 1);
			GL11.glTranslatef(0, -wanderRadius, 0);

			GL11.glColor4f(1, 1, 1, 1);
			TextureImpl.bindNone();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(-4, -4);
			GL11.glVertex2f(4, -4);
			GL11.glVertex2f(4, 4);
			GL11.glVertex2f(-4, 4);
			GL11.glEnd();

			GL11.glTranslatef(0, wanderRadius, 0);
			GL11.glRotatef(-wanderAngle + movableEntity.getHeading(), 0, 0, 1);
			GL11.glTranslatef(0, wanderFocusDistance, 0);
			GL11.glRotatef(-movableEntity.getHeading(), 0, 0, 1);
			GL11.glTranslatef(-pos.x, -pos.y, -pos.z);
		}

	}

	@Override
	public void run() {
		final Vect3D pos = new Vect3D(movableEntity.getPos());

		wanderAngle += Math.random() * 4 - 2;
		Vect3D target = new Vect3D(new Vect3D(Vect3D.NORTH).normalize(wanderFocusDistance).rotate(movableEntity.getHeading()))
				.add(new Vect3D(Vect3D.NORTH).normalize(wanderRadius).rotate(wanderAngle));

		// TODO is it right to multiply by mass ?
		// What are we multiplying by mass ?
		steeringForce.copy(target).truncate(movableEntity.getMaxSteeringForce())
				.mult(movableEntity.getMass());
	}
}