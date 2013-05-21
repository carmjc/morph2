package net.carmgate.morph.model.entities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.model.common.Vect3D;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Morph implements Renderable, Selectable {

	public enum MorphType {
		OVERMIND,
		SHIELD,
		LASER,
		PROPULSOR;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Morph.class);
	private static Integer nextId = 0;
	private int id;
	private static Texture baseTexture;
	private static Map<MorphType, Texture> morphTypeTextures = new HashMap<>();
	private final Vect3D pos = new Vect3D();
	private final MorphType morphType;

	public Morph() {
		this(null, 0, 0, 0);
	}

	public Morph(MorphType morphType, float x, float y, float z) {
		this.morphType = morphType;
		synchronized (nextId) {
			id = nextId++;
		}
	}

	public Vect3D getPos() {
		return pos;
	}

	@Override
	public int getSelectionId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void initRenderer() {
		// load texture from PNG file if needed
		if (baseTexture == null) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("morphEditor1-64.png").getPath())) {
				baseTexture = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}

		for (MorphType morphType : MorphType.values()) {
			// morphTypeTextures.put(morphType, baseTexture)
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("morph_" + morphType.name().toLowerCase() + ".png")
					.getPath())) {
				morphTypeTextures.put(morphType, TextureLoader.getTexture("PNG", fileInputStream));
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}
	}

	@Override
	public void render(int glMode) {
		GL11.glTranslated(pos.x * 64, pos.y * 64, 0);
		GL11.glScalef(1.2f, 1.2f, 1.2f);

		// TODO the hexagons must be drawn in a form of hexagon to ensure picking will be done properly.
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		baseTexture.bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
		GL11.glEnd();

		if (morphType != null) {
			GL11.glScalef(0.5f, 0.5f, 1);
			morphTypeTextures.get(morphType).bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
			GL11.glEnd();
			GL11.glScalef(2, 2, 1);
		}

		GL11.glScalef(1 / 1.2f, 1 / 1.2f, 1 / 1.2f);
		GL11.glTranslated(-pos.x * 64, -pos.y * 64, 0);
	}

	@Override
	public void setSelected(boolean selected) {
		// TODO Auto-generated method stub

	}

}
