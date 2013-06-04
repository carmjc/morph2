package net.carmgate.morph.model.entities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.Behavior;
import net.carmgate.morph.model.behaviors.ForceGeneratingBehavior;
import net.carmgate.morph.model.behaviors.Movement;
import net.carmgate.morph.model.behaviors.StarsContribution;
import net.carmgate.morph.model.behaviors.steering.Orbit;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityHints;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.model.entities.common.Movable;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.ui.common.RenderingHints;
import net.carmgate.morph.ui.common.RenderingSteps;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EntityHints(entityType = EntityType.PLANET, selectable = false)
@RenderingHints(renderingStep = RenderingSteps.PLANET)
public class Planet extends Entity implements Movable {

	private static final Logger LOGGER = LoggerFactory.getLogger(Planet.class);
	private static Texture baseTexture;
	private static Integer nextId = 0;

	private final Vect3D pos = new Vect3D();
	private final Vect3D speed = new Vect3D();
	private final Star star;

	private int id;
	private final float mass;
	private final float mu;

	private final float radius;

	private final float orbit;
	private final Set<Behavior> behaviorSet = new HashSet<>();
	private final Vect3D steeringForce = new Vect3D();
	private final Vect3D effectiveForce = new Vect3D();
	private final StarsContribution starsContribution;

	@Deprecated
	public Planet() {
		this(null, 0, 0, 0);
	}

	public Planet(Star star, float mass, float radius, float orbit) {

		this.star = star;
		this.mass = mass;
		this.radius = radius;
		this.orbit = orbit;
		if (star != null) {
			mu = (float) Math.sqrt(Star.SIMPLE_G * (star.getMass() + mass) / orbit);
			// TODO compute random first position
			pos.copy(new Vect3D(star.getPos()).add(new Vect3D(Vect3D.NORTH).rotate((float) (Math.random() * 360)).mult(orbit)));
		} else {
			mu = 0;
		}

		synchronized (nextId) {
			id = nextId++;
		}

		starsContribution = new StarsContribution(this);
		addBehavior(starsContribution);
	}

	@Override
	public void addBehavior(Behavior behavior) {
		behaviorSet.add(behavior);
		if (behavior instanceof Orbit) {
			((Orbit) behavior).setStarsContribution(starsContribution);
		}
	}

	// IMPROVE remove effectiveForce from steeringForce management ?
	// movements should add a propulsion force to the ship
	private void applySteeringForce(Vect3D force) {
		steeringForce.add(force);
		effectiveForce.add(force);
	}

	@Override
	public float getHeading() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public float getMass() {
		return mass;
	}

	@Override
	public float getMaxSpeed() {
		// TODO Auto-generated method stub
		return 100000;
	}

	@Override
	public float getMaxSteeringForce() {
		// TODO Auto-generated method stub
		return 100000;
	}

	@Override
	public Vect3D getPos() {
		return pos;
	}

	@Override
	public Vect3D getSpeed() {
		return speed;
	}

	@Override
	public void initRenderer() {
		// load texture from PNG file if needed
		if (baseTexture == null) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("img/planet/planet4.png").getPath())) {
				baseTexture = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}
	}

	@Override
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeBehavior(Behavior behavior) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(int glMode) {
		GL11.glTranslatef(pos.x, pos.y, pos.z);
		float radiusScale = radius / 10;
		float halfWidth = 64f;
		// boolean maxZoom = halfWidth * radiusScale * Model.getModel().getViewport().getZoomFactor() > 15;

		// if (maxZoom) {
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glScalef(radiusScale, radiusScale, 1);
		baseTexture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(-halfWidth, halfWidth);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(halfWidth, halfWidth);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(halfWidth, -halfWidth);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-halfWidth, -halfWidth);
		GL11.glEnd();
		GL11.glScalef(1f / radiusScale, 1f / radiusScale, 1);
		// } else {
		// float adjustedSize = 15 / Model.getModel().getViewport().getZoomFactor();
		// // zoomedOutTexture.bind();
		// // TODO make a texture for the zoomed out star
		// baseTexture.bind();
		// GL11.glBegin(GL11.GL_QUADS);
		// GL11.glTexCoord2f(0, 0);
		// GL11.glVertex2f(-adjustedSize, adjustedSize);
		// GL11.glTexCoord2f(1, 0);
		// GL11.glVertex2f(adjustedSize, adjustedSize);
		// GL11.glTexCoord2f(1, 1);
		// GL11.glVertex2f(adjustedSize, -adjustedSize);
		// GL11.glTexCoord2f(0, 1);
		// GL11.glVertex2f(-adjustedSize, -adjustedSize);
		// GL11.glEnd();
		// }

		if (Model.getModel().getUiContext().isDebugMode()) {
			GL11.glColor4f(0, 1, 0, 1);
			speed.render(glMode);
		}

		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);

		// Render behaviors
		for (Behavior behavior : behaviorSet) {
			if (behavior instanceof Renderable) {
				((Renderable) behavior).render(glMode);
			}
		}

	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setSelected(boolean selected) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update() {

		effectiveForce.nullify();
		steeringForce.nullify();

		// if no movement needed, no update needed
		for (Behavior behavior : behaviorSet) {
			if (behavior.isActive()) {
				behavior.run(Model.getModel().getSecondsSinceLastUpdate());

				// if the behavior is a movement, use the generated steering force
				if (behavior instanceof Movement) {
					applySteeringForce(((Movement) behavior).getSteeringForce());
				}

				// if the behavior is generating a force, we must apply it
				if (behavior instanceof ForceGeneratingBehavior) {
					effectiveForce.add(((ForceGeneratingBehavior) behavior).getNonSteeringForce());
				}

			}
		}

		// velocity = truncate (velocity + acceleration, max_speed)
		// TODO fix this magic number
		Vect3D accel = new Vect3D(effectiveForce).mult(1f / mass);
		// LOGGER.debug("planet: " + accel);
		speed.add(accel.mult(Model.getModel().getSecondsSinceLastUpdate())).truncate(getMaxSpeed());
		// position = position + velocity
		pos.add(new Vect3D(speed).mult(Model.getModel().getSecondsSinceLastUpdate()));

		// LOGGER.debug("planet effective force: " + effectiveForce);
	}

}
