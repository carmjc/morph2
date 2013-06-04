package net.carmgate.morph.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.model.entities.common.Updatable;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticleEngine implements Renderable, Updatable {

	// IMPROVE Might be interesting to implement several fading types.
	public static class Particle {
		private final Vect3D pos;
		private final Vect3D speed;
		private final float rot = (float) (Math.random() * 360);

		private final float initialLuminosity;
		private float luminosity = 0;

		private final float maxLife;

		private float life;
		private final Random random = new Random();

		/**
		 * Create a new particle
		 * @param pos
		 * @param speed
		 * @param life the time it takes for the particle to die (in millis)
		 * @param initialAlpha 
		 */
		public Particle(Vect3D pos, Vect3D speed, float initialLife, float initialLifeDeviation, float minInitialAlpha, float maxInitialAlpha) {
			this.pos = pos;
			this.speed = speed;
			maxLife = life = Math.max(0, (float) random.nextGaussian() * initialLifeDeviation + initialLife);
			initialLuminosity = luminosity = (float) (Math.random() * (maxInitialAlpha - minInitialAlpha) + minInitialAlpha);
		}

		public float getInitialLuminosity() {
			return initialLuminosity;
		}

		public float getLife() {
			return life;
		}

		public float getLuminosity() {
			return luminosity;
		}

		public float getMaxLife() {
			return maxLife;
		}

		public Vect3D getPos() {
			return pos;
		}

		public float getRot() {
			return rot;
		}

		public Vect3D getSpeed() {
			return speed;
		}

		public void setLife(float life) {
			this.life = life;
		}

		public void setLuminosity(float luminosity) {
			this.luminosity = luminosity;
		}

	}

	/** The texture under the morph image. */
	private static Texture baseTexture;

	private static final Logger LOGGER = LoggerFactory.getLogger(ParticleEngine.class);

	private final List<Particle> particles = new LinkedList<>();

	/** Timestamp of last time the ship's position was calculated. */
	// IMPROVE We should move this in a class that can handle this behavior for any Updatable
	private long lastUpdateTS;

	protected float secondsSinceLastUpdate;

	protected final Random random = new Random();

	public void addParticle(Vect3D pos, Vect3D speed, float initialLife, float initialLifeDeviation, float minInitialAlpha, float maxInitialAlpha) {
		if (speed.modulus() != 0) {
			speed.rotate((float) random.nextGaussian() * 2);
		}
		particles.add(new Particle(pos, speed, initialLife, initialLifeDeviation, minInitialAlpha, maxInitialAlpha));
	}

	@Override
	public void initRenderer() {
		// load texture from PNG file if needed
		if (baseTexture == null) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("img/particle1-32.png").getPath())) {
				baseTexture = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}

	}

	@Override
	public void render(int glMode) {
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		for (Particle particle : particles) {
			// LOGGER.debug("Rendering particle: " + particle);
			GL11.glTranslatef(particle.getPos().x, particle.getPos().y, 0);
			GL11.glRotatef(particle.getRot(), 0, 0, 1);

			float factor = 5;
			float particleSize = 0.01f + (particle.getMaxLife() - particle.getLife()) * factor;
			GL11.glScalef(particleSize, particleSize, 1);

			GL11.glColor4f(1, 1, 1, particle.getLuminosity());
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

			GL11.glScalef(1f / particleSize, 1f / particleSize, 1);
			GL11.glRotatef(-particle.getRot(), 0, 0, 1);
			GL11.glTranslatef(-particle.getPos().x, -particle.getPos().y, 0);
		}
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	@Override
	public void update() {
		secondsSinceLastUpdate = ((float) Model.getModel().getCurrentTS() - lastUpdateTS) / 1000;
		lastUpdateTS = Model.getModel().getCurrentTS();
		// TODO this should be in the main loop
		if (secondsSinceLastUpdate == 0f) {
			return;
		}

		// Update all particles
		List<Particle> particlesToRemove = new ArrayList<>();
		for (Particle particle : particles) {
			Vect3D pos = particle.getPos();
			Vect3D speed = particle.getSpeed();
			pos.x += speed.x * secondsSinceLastUpdate;
			pos.y += speed.y * secondsSinceLastUpdate;
			pos.z += speed.z * secondsSinceLastUpdate;

			// linear decrease in luminosity
			particle.setLuminosity(particle.getInitialLuminosity() * particle.getLife() / particle.getMaxLife());
			particle.setLife(particle.getLife() - secondsSinceLastUpdate / particle.getMaxLife());

			if (particle.getLife() <= 0 || particle.getLuminosity() <= 0) {
				particlesToRemove.add(particle);
			}
		}

		// Remove dead particles
		for (Particle particle : particlesToRemove) {
			particles.remove(particle);
		}
	}
}
