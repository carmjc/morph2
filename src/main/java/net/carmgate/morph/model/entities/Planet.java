package net.carmgate.morph.model.entities;

import java.io.FileInputStream;
import java.io.IOException;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.steering.ArriveForPlanet;
import net.carmgate.morph.model.behaviors.steering.Orbit;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityHints;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.ui.common.RenderingHints;
import net.carmgate.morph.ui.common.RenderingSteps;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EntityHints(entityType = EntityType.PLANET, selectable = false)
@RenderingHints(renderingStep = RenderingSteps.PLANET)
public class Planet extends Entity {

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
	private Orbit behavior;

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
	}

	public void addBehavior(Orbit behavior) {
		this.behavior = behavior;
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getMass() {
		return mass;
	}

	public Vect3D getPos() {
		return pos;
	}

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

		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);

		behavior.render(glMode);
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
		behavior.run(Model.getModel().getSecondsSinceLastUpdate());

		// velocity = truncate (velocity + acceleration, max_speed)
		speed.add(new Vect3D(behavior.getSteeringForce()).mult(Model.getModel().getSecondsSinceLastUpdate())).truncate(ArriveForPlanet.MAX_SPEED);
		// position = position + velocity
		pos.add(new Vect3D(speed).mult(Model.getModel().getSecondsSinceLastUpdate()));

	}

}
