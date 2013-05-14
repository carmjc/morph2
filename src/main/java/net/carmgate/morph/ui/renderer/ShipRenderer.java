package net.carmgate.morph.ui.renderer;

import java.io.FileInputStream;
import java.io.IOException;

import net.carmgate.morph.model.entities.Entity;
import net.carmgate.morph.model.entities.Ship;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Renders({ Ship.class })
public class ShipRenderer implements Renderer<Ship> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShipRenderer.class);

	/** The texture under the morph image. */
	private Texture baseTexture;

	@Override
	public void init() {
		// load texture from PNG file if needed
		if (baseTexture == null) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("spaceship.png").getPath())) {
				baseTexture = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}

	}

	@Override
	public void render(int glMode, Renderer.RenderingType renderingType, Ship ship) {

		GL11.glTranslatef(ship.pos.x, ship.pos.y, ship.pos.z);
		GL11.glRotatef(ship.rot, 0, 0, 1);

		if (glMode == GL11.GL_SELECT) {
			// Render for selection
			GL11.glPushName(Ship.class.getAnnotation(Entity.class).uniqueId());
			GL11.glPushName(ship.getId());

			GL11.glBegin(GL11.GL_QUADS);
			GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
			GL11.glVertex2f(baseTexture.getTextureWidth() / 2, -baseTexture.getTextureWidth() / 2);
			GL11.glVertex2f(baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
			GL11.glVertex2f(-baseTexture.getTextureWidth() / 2, baseTexture.getTextureHeight() / 2);
			GL11.glEnd();

			GL11.glPopName();
			GL11.glPopName();
		} else {
			// Render for show
			// GL11.glColor3f(0.7f, 0.7f, 0.7f);
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
		}

		GL11.glRotatef(-ship.rot, 0, 0, 1);
		GL11.glTranslatef(-ship.pos.x, -ship.pos.y, -ship.pos.z);
	}

}
