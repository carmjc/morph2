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
import net.carmgate.morph.model.behaviors.steering.Orbit;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityHints;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.model.orders.Die;
import net.carmgate.morph.model.orders.Order;
import net.carmgate.morph.model.orders.TakeDamage;
import net.carmgate.morph.model.player.Player;
import net.carmgate.morph.model.player.Player.PlayerType;
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

	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);

	private static final Logger LOGGER = LoggerFactory.getLogger(Ship.class);
	/** The texture under the morph image. */
	private static Texture baseTexture;

	private static Texture zoomedOutTexture;

	// TODO This should be moved elsewhere
	private static final float MAX_DAMAGE = 10;

	private final Map<Integer, Morph> morphsById = new HashMap<>();

	private final Map<MorphType, List<Morph>> morphsByType = new HashMap<>();

	private final List<Order> orderList = new ArrayList<>();

	// TODO put in conf the dimension of the table
	private final Vect3D[] trail = new Vect3D[20];
	/** Stores last trail update. It occurred less than trailUpdateInterval ago. */
	private long trailLastUpdate;
	// TODO put in conf the trail update interval
	private final int trailUpdateInterval = 50;

	private final List<Order> newOrderList = new ArrayList<>();

	// TODO We should move this to entity
	private float realAccelModulus;

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

		// initialize energy
		// TODO This should be a function of the ship's fitting
		energy = 100;

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
							pendingBehaviorsAddition.add(behavior);
							// TODO Clean this and similar items
							if (behavior instanceof Orbit) {
								((Orbit) behavior).setStarsContribution(starsContribution);
							}

							return;
						}
					}
				}
			}

			return;
		}

		super.addBehavior(behavior);
	}

	public void addEnergy(float energyInc) {
		// TODO implement some kind of max energy
		energy += energyInc;
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

		// clone behaviors
		for (Behavior behavior : behaviorSet) {
			newShip.addBehavior(behavior.cloneForEntity(newShip));
		}

		return newShip;
	}

	public boolean consumeEnergy(float energyDec) {
		// return true if there is enough energy
		if (energy >= energyDec) {
			// TODO implement some kind of max energy
			energy -= energyDec;
			return true;
		}

		// return false if there isn't enough energy
		return false;
	}

	/** 
	 * Adds orders.
	 * The orders are effectively added at the end of the update cycle
	 * once the current update cycle orders have been processed.
	 * @param order
	 */
	public void fireOrder(Order order) {
		newOrderList.add(order);
	}

	public float getEnergy() {
		return energy;
	}

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

	public Morph getMorphById(int id) {
		return morphsById.get(id);
	}

	/**
	 * <b>Warning : Do not modify the resulting List</b>
	 * @param morphType
	 * @return a list containing all the morphs of a given type.
	 */
	public List<Morph> getMorphsByType(MorphType morphType) {
		return morphsByType.get(morphType);
	}

	public float getRealAccelModulus() {
		return realAccelModulus;
	}

	/**
	 * This method handles orders.
	 * IMPROVE This probably should be improved. It is quite ugly to have such a if-else cascade.
	 * However, I don't want to use a handler factory that would kill the current simplicity of orders handling
	 * @param order
	 */
	private void handleOrder(Order order) {
		if (order instanceof TakeDamage) {
			// This is not multiplied by lastUpdateTS because the timing is handled by the sender of the event.
			damage += ((TakeDamage) order).getDamageAmount();
			if (damage > MAX_DAMAGE) {
				fireOrder(new Die());
			}

			float explosionAngle = (float) (Math.random() * 180 + 90);
			for (int i = 0; i < 5; i++) {
				Model.getModel()
						.getParticleEngine()
						.addParticle(new Vect3D(pos), new Vect3D(speed).mult(0.25f).rotate((float) (explosionAngle + Math.random() * 5)).add(speed),
								2, 0.125f,
								0.5f, 0.2f);
			}

			LOGGER.debug("Damage at " + damage + " for " + this);
		} else if (order instanceof Die) {
			LOGGER.debug("Die !!!");

			dead = true;
			for (int i = 0; i < 200; i++) {
				Model.getModel()
						.getParticleEngine()
						.addParticle(new Vect3D(pos), new Vect3D(200, 0, 0).rotate((float) (Math.random() * 360)).mult((float) Math.random()).add(speed),
								2, 0.5f,
								0.5f, 0.05f);
			}

			Model.getModel().removeEntity(this);

		}
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

	private void processAI() {
		// TODO Outsource this AI to allow several kinds of AIs
		// TODO implement AI processing
		// Very simple AI : wander and attack

	}

	@Override
	public void render(int glMode) {

		float massScale = mass / 10;
		float zoomFactor = Model.getModel().getViewport().getZoomFactor();
		boolean disappearZoom = 64f * massScale * zoomFactor < 1;
		if (disappearZoom && !selected) {
			return;
		}

		boolean minZoom = 64f * massScale * zoomFactor > 16;

		// render trail
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

				GL11.glColor4f(1, 1, 1, ((float) trail.length - i) / trail.length);
				TextureImpl.bindNone();
				GL11.glBegin(GL11.GL_QUADS);
				GL11.glVertex2f(start.x - startToEnd.x, start.y - startToEnd.y);
				GL11.glVertex2f(end.x - startToEnd.x, end.y - startToEnd.y);
				GL11.glVertex2f(end.x + startToEnd.x, end.y + startToEnd.y);
				GL11.glVertex2f(start.x + startToEnd.x, start.y + startToEnd.y);
				GL11.glEnd();

			}
		}

		GL11.glTranslatef(pos.x, pos.y, pos.z);
		GL11.glRotatef(heading, 0, 0, 1);

		// Render selection circle around the ship
		renderSelection(massScale, minZoom);

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
		if (minZoom) {
			GL11.glScalef(massScale, massScale, 0);
			baseTexture.bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(-64, -64);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(64, -64);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(64, 64);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(-64, 64);
			GL11.glEnd();
			GL11.glScalef(1f / massScale, 1f / massScale, 0);
		} else {
			GL11.glScalef(1f / (4 * zoomFactor), 1f / (4 * zoomFactor), 0);
			zoomedOutTexture.bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(-64, -64);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(64, -64);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(64, 64);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(-64, 64);
			GL11.glEnd();
			GL11.glScalef(4 * zoomFactor, 4 * zoomFactor, 0);
		}

		GL11.glRotatef(-heading, 0, 0, 1);

		// Render ship forces
		if (Model.getModel().getUiContext().isDebugMode()) {
			GL11.glColor3f(1, 1, 0);
			effectiveForce.render(glMode, 1);
		}

		// Render energy gauge
		GL11.glScalef(1f / zoomFactor, 1f / zoomFactor, 1);
		if (minZoom) {
			RenderUtils.renderGauge(50, 16 + 64 * zoomFactor * massScale + 5, Math.min(MAX_DAMAGE - damage, MAX_DAMAGE) / MAX_DAMAGE, 0.2f,
					new float[] { 0.5f, 1, 0.5f,
							1 });
			RenderUtils.renderGauge(50, 16 + 64 * zoomFactor * massScale - 5, Math.min(energy, 100) / 100, 0.05f, new float[] { 0.5f, 0.5f, 1, 1 });
		} else {
			RenderUtils.renderGauge(50, 32 + 5, Math.min(MAX_DAMAGE - damage, MAX_DAMAGE) / MAX_DAMAGE, 0.2f, new float[] { 0.5f, 1, 0.5f, 1 });
			RenderUtils.renderGauge(50, 32 - 5, Math.min(energy, 100) / 100, 0.05f, new float[] { 0.5f, 0.5f, 1, 1 });
		}
		GL11.glScalef(zoomFactor, zoomFactor, 1);

		// Render morphs
		if (Model.getModel().getUiContext().isMorphsShown()) {
			GL11.glScalef(1f / (2 * zoomFactor), 1f / (2 * zoomFactor), 1);
			Main.shipEditorRender(this, glMode);
			GL11.glScalef(2 * zoomFactor, 2 * zoomFactor, 1);
		}

		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);

		// Render behaviors
		for (Behavior behavior : behaviorSet) {
			if (behavior instanceof Renderable) {
				((Renderable) behavior).render(glMode);
			}
		}

	}

	// TODO Replace this method by using RenderUtils.renderCircle(...)
	private void renderSelection(float massScale, boolean maxZoom) {
		if (selected) {
			// render limit of effect zone
			TextureImpl.bindNone();
			float tInt = 0; // temporary data holder
			float tExt = 0; // temporary data holder
			float xInt;
			float xExt;
			float zoomFactor = Model.getModel().getViewport().getZoomFactor();

			if (maxZoom) {
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

	private void rotateProperly() {

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
	public String toString() {
		return "ship:" + pos.toString();
	}

	@Override
	public void update() {
		// TODO Is this really the proper way to do it ?
		effectiveForce.nullify();
		steeringForce.nullify();

		// handle AI assignements if appropriate
		if (player.getPlayerType() == PlayerType.AI) {
			processAI();
		}

		updateForcesWithBehavior();

		// cap steeringForce to maximum steering force
		steeringForce.truncate(getMaxSteeringForce());
		effectiveForce.add(steeringForce);

		// rotate and add trail according to the steering force vector
		rotateProperly();

		// real accel is necessary to calculate propulsors energy consumption
		// it is the difference between the speed in the new cycle and
		// the speed in the previous cycle
		Vect3D realAccel = new Vect3D(speed);

		// velocity = truncate (velocity + acceleration, max_speed)
		speed.add(new Vect3D(effectiveForce).mult(1f / mass).mult(Model.getModel().getSecondsSinceLastUpdate())).truncate(maxSpeed);
		realAccel.substract(speed);
		realAccelModulus = realAccel.modulus();
		// position = position + velocity
		pos.add(new Vect3D(speed).mult(Model.getModel().getSecondsSinceLastUpdate()));

		// Handle orders
		for (Order order : orderList) {
			handleOrder(order);
		}
		orderList.clear();
		orderList.addAll(newOrderList);
		newOrderList.clear();

		// update trail
		if (trailLastUpdate == 0 || Model.getModel().getLastUpdateTS() - trailLastUpdate > trailUpdateInterval) {
			for (int i = trail.length - 2; i >= 0; i--) {
				trail[i + 1] = trail[i];
			}
			trail[0] = new Vect3D(pos);
			trailLastUpdate += trailUpdateInterval;
		}

		// TODO move this somewhere else
		handlePendingBehaviors();
	}

	private void updateMorphDependantValues() {
		// Compute morphs level dependant values
		// TODO Update these values each time a morph is upgraded
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
}
