package net.carmgate.morph.model.entities;

import java.io.FileInputStream;
import java.io.IOException;

import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityHints;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.ui.rendering.RenderingHints;
import net.carmgate.morph.ui.rendering.RenderingSteps;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A world area contains :
 * - a reference to the ships within the area
 * - a reference to neighboring areas. 
 */
@RenderingHints(renderingStep = RenderingSteps.WORLDAREA)
@EntityHints(entityType = EntityType.WORLDAREA, selectable = false)
// This should not be an entity
public class WorldArea extends Entity {

	public static enum Cardinal {
		N, NE, E, SE, S, SW, W, NW;
	}

	private static Integer nextId = 0;
	private final int id;

	private static final int RENDERING_SIZE = 512;
	private static final Logger LOGGER = LoggerFactory.getLogger(WorldArea.class);
	private static Texture[] textures = new Texture[8];

	private final WorldArea[] neighbours = new WorldArea[8];
	private final float x;
	private final float y;

	public WorldArea() {
		this(0, 0);
	}

	public WorldArea(float x, float y) {
		synchronized (nextId) {
			id = nextId++;
		}

		this.x = x;
		this.y = y;
	}

	@Override
	public int getId() {
		return id;
	}

	public WorldArea getNeighbour(Cardinal card) {
		if (neighbours[card.ordinal()] == null) {
			neighbours[card.ordinal()] = new WorldArea(0, 0);
		}
		return neighbours[card.ordinal()];
	}

	@Override
	public void initRenderer() {
		for (int i = 0; i < textures.length; i++) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("stars" + (i + 1) + ".png").getPath())) {
				textures[i] = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}
	}

	@Override
	public void render(int glMode) {
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glTranslatef(x, y, 0);
		GL11.glColor4f(1, 1, 1, 1);

		textures[id % 8].bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(-RENDERING_SIZE, RENDERING_SIZE);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(RENDERING_SIZE, RENDERING_SIZE);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(RENDERING_SIZE, -RENDERING_SIZE);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-RENDERING_SIZE, -RENDERING_SIZE);
		GL11.glEnd();

		GL11.glTranslatef(-x, -y, 0);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	public void setSelected(boolean selected) {
	}

	@Override
	public void update() {
	}

}
