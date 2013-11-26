package net.carmgate.morph.model.entities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.Main;
import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.conf.Conf.ConfItem;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.common.ActivatedMorph;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.behaviors.common.Needs;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityHints;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.model.player.Player;
import net.carmgate.morph.model.player.Player.FOF;
import net.carmgate.morph.ui.common.RenderUtils;
import net.carmgate.morph.ui.common.RenderingHints;
import net.carmgate.morph.ui.common.RenderingSteps;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EntityHints(entityType = EntityType.SHIP)
@RenderingHints(renderingStep = RenderingSteps.SHIP)
public class Ship extends Entity {

	// Used for drawing circles efficiently
	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);

	public static final Logger LOGGER = LoggerFactory.getLogger(Ship.class);

	/** The texture under the morph image. */
	private static Texture baseTexture;
	private static Texture zoomedOutTexture;

	// morphs
	private final Map<Integer, Morph> morphsById = new HashMap<>();
	private final Map<MorphType, List<Morph>> morphsByType = new HashMap<>();

	/** Stores last trail update. It occurred less than trailUpdateInterval ago. */
	private long trailLastUpdate;
	private final int trailUpdateInterval = Conf.getIntProperty(ConfItem.SHIP_TRAIL_UPDATEINTERVAL);
	private final Vect3D[] trail = new Vect3D[Conf.getIntProperty(ConfItem.SHIP_TRAIL_NUMBEROFSEGMENTS)];

	/***
	 * Creates a new ship with position (0, 0, 0), mass = 10 assigned to player "self".
	 */
	public Ship() {
		this(0, 0, 0, 0, 10, Model.getModel().getSelf());
	}

	public Ship(float x, float y, float z, float heading, float mass, Player player) {
		super(player);

		Model.getModel().getPlayers().add(player);

		// initialize positional information
		pos.copy(x, y, z);
		this.heading = heading;
		this.mass = mass;

		// TODO This should be a function of the ship's fitting
		energy = 100;
		maxDamage = 10;
		maxEnergy = 100;

	}

	@Override
	public void addBehavior(Behavior behavior) {

		// Checks that the behavior can be added to the ship
		if (behavior != null
				&& behavior.getClass().isAnnotationPresent(Needs.class)) {
			ActivatedMorph[] needs = behavior.getClass().getAnnotation(Needs.class).value();

			if (needs != null) {
				for (ActivatedMorph need : needs) {
					for (Morph morph : morphsById.values()) {
						if (morph.getMorphType() == need.morphType()) {
							super.addBehavior(behavior);

							return;
						}
					}
				}
			}

			return;
		}

		super.addBehavior(behavior);
	}

	public void addMorph(Morph morph) {
		morphsById.put(morph.getId(), morph);

		List<Morph> list = morphsByType.get(morph.getMorphType());
		if (list == null) {
			list = new ArrayList<>();
			morphsByType.put(morph.getMorphType(), list);
		}
		list.add(morph);

		updateMorphDependantValues();
	}

	@Override
	protected void autoRotate() {

		float secondsSinceLastUpdate = Model.getModel().getSecondsSinceLastUpdate();

		// if steeringForce is too small, we must not change the orientation or we will be
		// by orientation fluctuations due to improper angle approximation
		// LOGGER.debug("" + steeringForce.modulus());
		if (steeringForce.modulus() < 0.1) {
			return;
		}

		// rotate properly along the speed vector (historically along the steering force vector)
		float newHeading;
		float headingFactor = steeringForce.modulus() / maxSteeringForce * mass * 4;
		if (headingFactor > 3) {
			newHeading = new Vect3D(Vect3D.NORTH).angleWith(steeringForce);
		} else if (headingFactor > 0) {
			newHeading = new Vect3D(Vect3D.NORTH).angleWith(new Vect3D(steeringForce).mult(headingFactor).add(new Vect3D(speed).mult(1 - headingFactor / 3)));
		} else {
			newHeading = new Vect3D(Vect3D.NORTH).angleWith(speed);
		}

		// heading = newHeading;
		float angleDiff = (newHeading - heading + 360) % 360;
		float maxAngleSpeed = Conf.getIntProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXANGLESPEEDPERMASSUNIT) / mass;
		if (angleDiff < maxAngleSpeed * Math.max(1, angleDiff / 180) * secondsSinceLastUpdate) {
			heading = newHeading;
		} else if (angleDiff < 180) {
			heading = heading + maxAngleSpeed * Math.max(1, angleDiff / 180) * secondsSinceLastUpdate;
		} else if (angleDiff >= 360 - maxAngleSpeed * Math.max(1, angleDiff / 180) * secondsSinceLastUpdate) {
			heading = newHeading;
		} else {
			heading = heading - maxAngleSpeed * Math.max(1, angleDiff / 180) * secondsSinceLastUpdate;
		}
	}

	@Override
	public Ship clone() {
		Ship newShip = new Ship(getPos().x, getPos().y, getPos().z, getHeading(),
				getMass(), getPlayer());

		// clone morphs
		for (MorphType morphType : MorphType.values()) {
			List<Morph> morphs = getMorphsByType(morphType);
			if (morphs != null) {
				for (Morph morph : morphs) {
					newShip.addMorph(new Morph(morphType, morph.getLevel(), morph.getXp()));
				}
			}
		}

		cloneBehaviors(newShip);

		return newShip;
	}

	public boolean consumeEnergy(float energyDec) {
		// return true if there is enough energy
		if (energy >= energyDec) {
			// TODO #25 implement some kind of max energy
			energy -= energyDec;
			return true;
		}

		// return false if there isn't enough energy
		return false;
	}

	public float getEnergy() {
		return energy;
	}

	/**
	 * @param morphType a morph type
	 * @return the highest level of a morph of given type inthe ship.
	 */
	private int getMaxLevelForMorphType(final MorphType morphType) {
		int maxLevel = 0;

		if (morphsByType.get(morphType) == null) {
			return 0;
		}

		for (Morph morph : morphsByType.get(morphType)) {
			if (morph.getLevel() > maxLevel) {
				maxLevel = morph.getLevel();
			}
		}
		return maxLevel;
	}

	/**
	 * @param id the id of the morph to retrieve.
	 * @return the morph matching the given id in the ship.
	 */
	public Morph getMorphById(int id) {
		return morphsById.get(id);
	}

	/**
	 * <b>Warning : Do not modify the resulting List. There might be concurrency issues.</b>
	 * @param morphType
	 * @return a list containing all the morphs of a given type.
	 */
	public List<Morph> getMorphsByType(MorphType morphType) {
		return morphsByType.get(morphType);
	}

	public float getRealAccelModulus() {
		return realAccelModulus;
	}

	/** List of ships IAs. */
	// private final List<IA> iaList = new ArrayList<IA>();

	@Override
	public void initRenderer() {
		// load texture from PNG file if needed
		if (baseTexture == null) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("img/spaceship.png").getPath())) {
				baseTexture = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}

		if (zoomedOutTexture == null) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("img/spaceshipZoomedOut.png").getPath())) {
				zoomedOutTexture = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}
	}

	@Override
	public void render(int glMode) {

		float massScale = mass / 10;
		float width = 128;
		float zoomFactor = Model.getModel().getViewport().getZoomFactor();
		boolean disappearZoom = massScale / mass * zoomFactor < 0.002f;
		if (disappearZoom && !selected && getPlayer().getFof() != FOF.SELF) {
			return;
		}

		boolean minZoom = massScale / mass * zoomFactor < 0.02f;

		// render trail
		renderTrail(glMode);

		// Render behaviors
		if (!isSelectRendering(glMode)) {
			for (Behavior behavior : getBehaviors()) {
				if (behavior instanceof Renderable) {
					((Renderable) behavior).render(glMode);
				}
			}
		}

		GL11.glTranslatef(pos.x, pos.y, pos.z);
		GL11.glRotatef(heading, 0, 0, 1);

		// Render selection circle around the ship
		renderSelection(glMode, massScale, minZoom);

		// Render the ship in itself
		if (Model.getModel().getUiContext().isDebugMode()) {
			// IMPROVE replace this with some more proper mass rendering
			float energyPercent = energy / 100;
			if (energyPercent <= 0) {
				GL11.glColor3f(0.1f, 0.1f, 0.1f);
			} else {
				GL11.glColor3f(1f - energyPercent, energyPercent, 0);
			}
		} else {
			GL11.glColor3f(1f, 1f, 1f);
		}
		if (minZoom && (selected || getPlayer().getFof() == FOF.SELF)) {
			GL11.glScalef(1f / (4 * zoomFactor), 1f / (4 * zoomFactor), 0);
			if (isSelectRendering(glMode)) {
				TextureImpl.bindNone();
				RenderUtils.renderDisc(width / 2);
			} else {
				zoomedOutTexture.bind();
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
			GL11.glScalef(4 * zoomFactor, 4 * zoomFactor, 0);
		} else {
			if (isSelectRendering(glMode)) {
				massScale = (float) Math.max(massScale, 0.02 * mass / zoomFactor);
			}

			GL11.glScalef(massScale, massScale, 0);
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
			GL11.glScalef(1f / massScale, 1f / massScale, 0);
		}

		GL11.glRotatef(-heading, 0, 0, 1);

		// Render ship forces
		if (Model.getModel().getUiContext().isDebugMode() && !isSelectRendering(glMode)) {
			GL11.glColor3f(1, 1, 0);
			effectiveForce.render(glMode, 1);
		}

		// Render energy gauge
		if (!isSelectRendering(glMode) && !minZoom || getPlayer().getFof() == FOF.SELF || selected) {
			GL11.glScalef(1f / zoomFactor, 1f / zoomFactor, 1);
			if (!minZoom) {
				RenderUtils.renderGauge(50, 16 + 64 * zoomFactor * massScale + 5, Math.min(maxDamage - damage, maxDamage) / maxDamage, 0.2f,
						new float[] { 0.5f, 1, 0.5f,
					1 });
				RenderUtils.renderGauge(50, 16 + 64 * zoomFactor * massScale - 5, Math.min(energy, maxEnergy) / maxEnergy, 0.05f, new float[] { 0.5f, 0.5f, 1,
					1 });
			} else {
				RenderUtils.renderGauge(50, 32 + 5, Math.min(maxDamage - damage, maxDamage) / maxDamage, 0.2f, new float[] { 0.5f, 1, 0.5f, 1 });
				RenderUtils.renderGauge(50, 32 - 5, Math.min(energy, maxEnergy) / maxEnergy, 0.05f, new float[] { 0.5f, 0.5f, 1, 1 });
			}
			GL11.glScalef(zoomFactor, zoomFactor, 1);
		}

		// Render morphs
		if (Model.getModel().getUiContext().isDebugMorphsShown()) {
			GL11.glScalef(1f / (2 * zoomFactor), 1f / (2 * zoomFactor), 1);
			Main.shipEditorRender(this, glMode);
			GL11.glScalef(2 * zoomFactor, 2 * zoomFactor, 1);
		}

		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);

		if (getPlayer().getFof() == FOF.SELF) {
			RenderUtils.renderLineToConsole("Max DPS: ", 2);
		}

	}

	// TODO Replace this method by using RenderUtils.renderCircle(...)
	private void renderSelection(int glMode, float massScale, boolean minZoom) {
		if (selected && !isSelectRendering(glMode)) {
			// render limit of effect zone
			TextureImpl.bindNone();
			float tInt = 0; // temporary data holder
			float tExt = 0; // temporary data holder
			float xInt;
			float xExt;
			float zoomFactor = Model.getModel().getViewport().getZoomFactor();

			if (!minZoom) {
				xInt = 64 * massScale - 16; // radius
				xExt = 64 * massScale - 16 + 6 / zoomFactor; // radius
			} else {
				xInt = 16 / zoomFactor; // radius
				xExt = 16 / zoomFactor + 6 / zoomFactor; // radius
			}

			float xIntBackup = xInt; // radius
			float xExtBackup = xExt; // radius
			float yInt = 0;
			float yExt = 0;
			float yIntBackup = 0;
			float yExtBackup = 0;
			float alphaMax = 1f;
			for (int i = 0; i < nbSegments; i++) {

				tInt = xInt;
				tExt = xExt;
				xInt = cos * xInt - sin * yInt;
				xExt = cos * xExt - sin * yExt;
				yInt = sin * tInt + cos * yInt;
				yExt = sin * tExt + cos * yExt;

				GL11.glBegin(GL11.GL_QUADS);
				GL11.glColor4f(0, 0.7f, 0, 0);
				GL11.glVertex2f(xInt, yInt);
				GL11.glColor4f(0, 0.7f, 0, 0);
				GL11.glVertex2f(xIntBackup, yIntBackup);
				GL11.glColor4f(0, 0.7f, 0, alphaMax);
				GL11.glVertex2f((xExtBackup + xIntBackup) / 2, (yExtBackup + yIntBackup) / 2);
				GL11.glColor4f(0, 0.7f, 0, alphaMax);
				GL11.glVertex2f((xExt + xInt) / 2, (yExt + yInt) / 2);
				GL11.glColor4f(0, 0.7f, 0, alphaMax);
				GL11.glVertex2f((xExtBackup + xIntBackup) / 2, (yExtBackup + yIntBackup) / 2);
				GL11.glColor4f(0, 0.7f, 0, alphaMax);
				GL11.glVertex2f((xExt + xInt) / 2, (yExt + yInt) / 2);
				GL11.glColor4f(0, 0.7f, 0, 0);
				GL11.glVertex2f(xExt, yExt);
				GL11.glColor4f(0, 0.7f, 0, 0);
				GL11.glVertex2f(xExtBackup, yExtBackup);
				GL11.glEnd();

				xIntBackup = xInt;
				xExtBackup = xExt;
				yIntBackup = yInt;
				yExtBackup = yExt;
			}
		}
	}

	private void renderTrail(int glMode) {
		if (!isSelectRendering(glMode)) {
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			if (trail[0] != null) {
				Vect3D start = new Vect3D(pos);
				Vect3D end = new Vect3D();
				Vect3D startToEnd = new Vect3D();
				for (int i = 0; i < trail.length; i++) {
					if (trail[i] == null) {
						break;
					}

					end.copy(start);
					start.copy(trail[i]);
					startToEnd.copy(start).substract(end).rotate(90).normalize(5);

					GL11.glColor4f(1, 1, 1, ((float) trail.length - i) / (2 * trail.length));
					TextureImpl.bindNone();
					GL11.glBegin(GL11.GL_QUADS);
					GL11.glVertex2f(start.x - startToEnd.x, start.y - startToEnd.y);
					GL11.glVertex2f(end.x - startToEnd.x, end.y - startToEnd.y);
					GL11.glVertex2f(end.x + startToEnd.x, end.y + startToEnd.y);
					GL11.glVertex2f(start.x + startToEnd.x, start.y + startToEnd.y);
					GL11.glEnd();
				}
			}
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
	}

	@Override
	public String toString() {
		return "ship:" + pos.toString();
	}

	private void updateMorphDependantValues() {
		// Compute morphs level dependant values
		// TODO #20 Update these values each time a morph is upgraded
		// int maxSimplePropulsorLevel = getMaxLevelForMorphType(MorphType.SIMPLE_PROPULSOR);
		maxSteeringForce = 0;
		maxSpeed = 0;
		float stackingPenalty = 1;
		List<Morph> simplePropulsorMorphs = getMorphsByType(MorphType.SIMPLE_PROPULSOR);
		if (simplePropulsorMorphs != null) {
			for (Morph morph : simplePropulsorMorphs) {
				maxSteeringForce += (float) (Conf.getIntProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXFORCE)
						* Math.pow(Conf.getFloatProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXFORCE_FACTORPERLEVEL), morph.getLevel()));
				maxSpeed += (float) (Conf.getIntProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXSPEED)
						* Math.pow(Conf.getFloatProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXSPEED_FACTORPERLEVEL), morph.getLevel()));
				stackingPenalty *= 0.75f;
			}
			maxSteeringForce *= stackingPenalty;
			maxSpeed *= stackingPenalty;
		}
		maxSteeringForce /= mass;
	}

	@Override
	protected void updateTrail() {
		if (trailLastUpdate == 0 || Model.getModel().getLastUpdateTS() - trailLastUpdate > trailUpdateInterval) {
			for (int i = trail.length - 2; i >= 0; i--) {
				trail[i + 1] = trail[i];
			}
			trail[0] = new Vect3D(pos);
			trailLastUpdate += trailUpdateInterval;
		}
	}
}
