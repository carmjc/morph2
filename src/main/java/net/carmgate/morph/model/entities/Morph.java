package net.carmgate.morph.model.entities;

import java.awt.Font;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.conf.Conf.ConfItem;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.model.entities.common.Selectable;
import net.carmgate.morph.model.events.MorphLevelUp;
import net.carmgate.morph.ui.common.RenderUtils;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Morph implements Renderable, Selectable {

	public enum MorphType {
		OVERMIND(0),
		SHIELD(20),
		LASER(100),
		SIMPLE_PROPULSOR(30),
		ARMOR(2);
		//		CARGO(0);

		private final float energyConsumption;

		MorphType(float energyConsumption) {
			this.energyConsumption = energyConsumption;
		}

		public float getEnergyConsumption() {
			return energyConsumption;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(Morph.class);
	private static Integer nextId = 0;
	private int id;
	private static Texture baseTexture;
	private static TrueTypeFont font;
	private static Map<MorphType, Texture> morphTypeTextures = new HashMap<>();

	private static float morphMaxXpLevel1 = Conf.getFloatProperty(ConfItem.MORPH_MAXXPLEVEL1);

	// Morph characteristics
	private final MorphType morphType;
	private int level = 0;
	private float xp = 0;
	private boolean selected;
	private Ship ship;

	public Morph() {
		this(null, null);
	}

	public Morph(MorphType morphType, int level, float xp, Ship ship) {
		this.morphType = morphType;
		this.level = level;
		this.xp = xp;
		this.ship = ship;
		synchronized (nextId) {
			id = nextId++;
		}
	}

	public Morph(MorphType morphType, Ship ship) {
		this(morphType, 0, 0, ship);
	}

	@Override
	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public float getMaxXpForCurrentLevel() {
		return morphMaxXpLevel1 * level;
	}

	public MorphType getMorphType() {
		return morphType;
	}

	public float getXp() {
		return xp;
	}

	public void increaseLevel() {
		level++;
	}

	public void increaseXp(float xpIncrement) {
		xp += xpIncrement;
		if (xp > getMaxXpForCurrentLevel()) {
			xp -= getMaxXpForCurrentLevel();
			level++;
			ship.fireEvent(new MorphLevelUp(this));
		}
	}

	@Override
	public void initRenderer() {
		// load texture from PNG file if needed
		if (baseTexture == null) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("img/morphEditor1-64.png").getPath())) {
				baseTexture = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}

		for (MorphType tmpMorphType : MorphType.values()) {
			// morphTypeTextures.put(morphType, baseTexture)
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("img/morph_" + tmpMorphType.name().toLowerCase() + ".png")
					.getPath())) {
				morphTypeTextures.put(tmpMorphType, TextureLoader.getTexture("PNG", fileInputStream));
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture for " + tmpMorphType, e);
			}
		}

		Font awtFont = new Font("Tahoma", Font.BOLD, 14);
		font = new TrueTypeFont(awtFont, true);
	}

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void render(int glMode) {
		float scale = 1.2f;
		float typeScale = 0.4f;
		float width = 64;

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glScalef(scale, scale, 1);

		// TODO the hexagons must be drawn in a form of hexagon to ensure picking will be done properly.
		GL11.glEnable(GL11.GL_TEXTURE_2D);
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

		if (morphType != null) {
			if (selected) {
				long ts = new GregorianCalendar().getTimeInMillis();
				final int blinkPeriod = 500;
				float millis = ts % blinkPeriod;
				if (millis > blinkPeriod / 2) {
					millis = blinkPeriod - millis;
				}
				GL11.glColor4f(1, 1, 1, millis / (blinkPeriod / 2));
			}
			GL11.glScalef(typeScale, typeScale, 1);
			morphTypeTextures.get(morphType).bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(-width / 2, width / 2);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(width / 2, width / 2);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(width / 2, -width / 2);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(-width / 2, -width / 2);
			GL11.glEnd();
			GL11.glScalef(1f / typeScale, 1f / typeScale, 1);
		}

		GL11.glScalef(1f / scale, 1f / scale, 1);

		String str = Integer.toString(level);
		font.drawString(-font.getWidth(str) / 2, -width / 2, str, Color.white);
		RenderUtils.renderGauge(30, 20, Math.min(1, xp / getMaxXpForCurrentLevel()), 0, new float[] { 1, 1, 0.5f, 1 });
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
