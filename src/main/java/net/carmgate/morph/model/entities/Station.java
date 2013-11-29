package net.carmgate.morph.model.entities;

import java.io.FileInputStream;
import java.io.IOException;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityHints;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.model.player.Player;
import net.carmgate.morph.ui.common.RenderUtils;
import net.carmgate.morph.ui.common.RenderingHints;
import net.carmgate.morph.ui.common.RenderingSteps;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EntityHints(entityType = EntityType.STATION, selectable = true)
@RenderingHints(renderingStep = RenderingSteps.STATION)
public class Station extends Entity {

	private static final Logger LOGGER = LoggerFactory.getLogger(Station.class);
	private static Texture baseTexture;

	private final float radius;

	@Deprecated
	public Station() {
		this(null, 0, 0, 0, null);
	}

	public Station(Entity orbitee, float mass, float radius, float orbit, Player player) {
		super(player);
		maxSpeed = 100000;
		maxSteeringForce = 100000;

		this.mass = mass;
		this.radius = radius;
		if (orbitee != null) {
			pos.copy(new Vect3D(orbitee.getPos()).add(new Vect3D(Vect3D.NORTH).rotate((float) (Math.random() * 360)).mult(orbit)));
		}

	}

	@Override
	public void initRenderer() {
		// load texture from PNG file if needed
		if (baseTexture == null) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("img/spacestations/station1.png").getPath())) {
				baseTexture = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}
	}

	@Override
	public void render(int glMode) {
		float scale = radius / 10;
		float width = 128f;

		float zoomFactor = Model.getModel().getViewport().getZoomFactor();
		// TODO disappear zoom should depend on the eventual orbit radius
		boolean disappearZoom = scale / radius * zoomFactor < 0.0005f;
		if (disappearZoom && !selected) {
			return;
		}

		boolean minZoom = scale / radius * zoomFactor < 0.002f;
		if (minZoom) {
			scale = 0.002f * radius / zoomFactor;
		}

		GL11.glTranslatef(pos.x, pos.y, pos.z);

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glScalef(scale, scale, 1);
		if (isSelectRendering(glMode)) {
			TextureImpl.bindNone();
			RenderUtils.renderDisc(width / 2);
		} else {
			baseTexture.bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(-width / 2, -width / 2);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(width / 2, -width / 2);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(width / 2, width / 2);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(-width / 2, width / 2);
			GL11.glEnd();
		}
		GL11.glScalef(1f / scale, 1f / scale, 1);

		if (Model.getModel().getUiContext().isDebugMode() && !isSelectRendering(glMode)) {
			GL11.glColor4f(0, 1, 0, 1);
			speed.render(glMode);
		}

		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);

		// Render behaviors
		if (!isSelectRendering(glMode)) {
			for (Behavior behavior : getBehaviors()) {
				if (behavior instanceof Renderable) {
					((Renderable) behavior).render(glMode);
				}
			}
		}

	}
}
