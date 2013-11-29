package net.carmgate.morph.model.behaviors.steering;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.common.ActivatedMorph;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.behaviors.common.Needs;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.ui.common.RenderUtils;

import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Needs({ @ActivatedMorph(morphType = MorphType.SIMPLE_PROPULSOR) })
public class WanderWithinRange extends Wander {

	private static final Logger LOGGER = LoggerFactory.getLogger(WanderWithinRange.class);

	private final Entity target;
	private final float range;
	private float delta = 0.2f;

	/**
	 * Do not use.
	 */
	@Deprecated
	public WanderWithinRange() {
		this(null, 0, 0, null, 0);
	}

	public WanderWithinRange(Entity entityToMove, float wanderFocusDistance, float wanderRadius, Entity target, float range) {
		super(entityToMove, wanderFocusDistance, wanderRadius);
		this.target = target;
		this.range = range;
	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		return new WanderWithinRange(entity, wanderFocusDistance, wanderRadius, target, range);
	}

	@Override
	public void render(int glMode) {
		super.render(glMode);

		if (Model.getModel().getUiContext().isDebugMode()) {
			GL11.glTranslatef(target.getPos().x, target.getPos().y, target.getPos().z);
			RenderUtils.renderCircle(range, 3 / Model.getModel().getViewport().getZoomFactor(),
					new Float[] { 0f, 0f, 0f, 0f }, new Float[] { 0f, 1f, 0f, 0.5f }, new Float[] { 0f, 0f, 0f, 0f });
			GL11.glTranslatef(-target.getPos().x, -target.getPos().y, -target.getPos().z);
		}
	}

	@Override
	public void run() {
		LOGGER.debug("Wandering within range");

		if (wanderRadius == 0) {
			movableEntity.removeBehavior(this);
			return;
		}

		wanderAngle += Math.random() * 2 - 1f;

		// if we are out of range, change the angle to take the ship back within range
		// the farther we are out of range, the more we pull it back within range
		// if we are twice as far as proper range, angle is full ahead to range center
		Vect3D offsetToTarget = new Vect3D(target.getPos()).substract(movableEntity.getPos());
		float distanceToTarget = offsetToTarget.modulus();
		float minDist = range * (1 - delta);
		float maxDist = range * (1 + delta);
		float forcedAngle = Vect3D.NORTH.angleWith(offsetToTarget);
		float speedAdjustment = Math.abs(new Vect3D(offsetToTarget).normalize(1).prodScal(movableEntity.getSpeed()));
		// TODO Improve speed adjustment use
		if (distanceToTarget + speedAdjustment > maxDist) {
			wanderAngle = forcedAngle;
		} else if (distanceToTarget + speedAdjustment > minDist) {
			float relativeAngle = (wanderAngle - forcedAngle + 180 + 720) % 360;
			float minAngle = 180 - 180 * (maxDist - distanceToTarget) / (maxDist - minDist);
			float maxAngle = 180 + 180 * (maxDist - distanceToTarget) / (maxDist - minDist);
			if (relativeAngle < minAngle) {
				wanderAngle = (minAngle + forcedAngle - 180 + 720) % 360;
			} else if (relativeAngle > maxAngle) {
				wanderAngle = (maxAngle + forcedAngle - 180 + 720) % 360;
			}
		}

		Vect3D targetDirection = new Vect3D(new Vect3D(Vect3D.NORTH).normalize(wanderFocusDistance).rotate(movableEntity.getHeading()))
		.add(new Vect3D(Vect3D.NORTH).normalize(wanderRadius).rotate(wanderAngle));

		// TODO is it right to multiply by mass ?
		// What are we multiplying by mass ?
		getSteeringForce().copy(targetDirection).truncate(movableEntity.getMaxSteeringForce())
		.mult(movableEntity.getMass());

	}
}