package net.carmgate.morph.model.entities;

import java.io.FileInputStream;
import java.io.IOException;

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

@EntityHints(entityType = EntityType.STAR, selectable = false)
@RenderingHints(renderingStep = RenderingSteps.STAR)
public class Star extends Entity {

	private static final Logger LOGGER = LoggerFactory.getLogger(Star.class);
	private static Texture baseTexture;

	private static Integer nextId = 0;
	public static double SIMPLE_G = 6.67259 * Math.pow(10, 3); // normal one is .. * Math.pow(10, -11)
	private final double gm;

	private final float radius;
	private final float energyFlow;

	/**
	 * Do not use this constructor.
	 */
	public Star() {
		this(0, 0, 0, 0, 0, 0);
	}

	public Star(float x, float y, float z, float mass, float radius, float energyFlow) {

		pos.x = x;
		pos.y = y;
		pos.z = z;
		this.mass = mass;
		this.radius = radius;
		gm = SIMPLE_G * mass;
		this.energyFlow = energyFlow;

		starsContribution = null;
	}

	public float getEnergyFlow() {
		return energyFlow;
	}

	public double getGm() {
		return gm;
	}

	public float getKillingRadius() {
		return radius * 3;
	}

	public float getRadius() {
		return radius;
	}

	@Override
	public void initRenderer() {
		// load texture from PNG file if needed
		if (baseTexture == null) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("img/stars/blue.png").getPath())) {
				baseTexture = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}

	}

	@Override
	public void render(int glMode) {
		GL11.glTranslatef(pos.x, pos.y, pos.z);
		float radiusScale = radius / 10;
		float halfWidth = 64f;
		// boolean maxZoom = halfWidth * radiusScale * Model.getModel().getViewport().getZoomFactor() > 15;

		// if (maxZoom) {
		GL11.glColor4f(1, 1, 1, 1);
		GL11.glScalef(radiusScale, radiusScale, 0);
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
		GL11.glScalef(1 / radiusScale, 1 / radiusScale, 0);

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
	}

	@Override
	public void update() {
		// TODO move this somewhere else
		handlePendingBehaviors();
		// Nothing to do
	}

}
