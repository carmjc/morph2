package net.carmgate.morph.model.entities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.conf.Conf.ConfItem;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.ActivatedMorph;
import net.carmgate.morph.model.behaviors.Behavior;
import net.carmgate.morph.model.behaviors.ForceGeneratingBehavior;
import net.carmgate.morph.model.behaviors.Movement;
import net.carmgate.morph.model.behaviors.Needs;
import net.carmgate.morph.model.behaviors.StarsContribution;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.EntityHints;
import net.carmgate.morph.model.entities.common.EntityType;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.model.entities.orders.Die;
import net.carmgate.morph.model.entities.orders.Order;
import net.carmgate.morph.model.entities.orders.TakeDamage;
import net.carmgate.morph.model.player.Player;
import net.carmgate.morph.model.player.Player.PlayerType;
import net.carmgate.morph.ui.rendering.RenderingHints;
import net.carmgate.morph.ui.rendering.RenderingSteps;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.functors.NotPredicate;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureImpl;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EntityHints(entityType = EntityType.SHIP)
@RenderingHints(renderingStep = RenderingSteps.SHIP)
public class Ship extends Entity {

	private static final class SameClassPredicate implements Predicate {
		private final Class<?> behaviorClass;

		public SameClassPredicate(Class<?> behaviorClass) {
			this.behaviorClass = behaviorClass;
		}

		@Override
		public boolean evaluate(Object object) {
			return behaviorClass.isInstance(object);
		}
	}

	private static final int nbSegments = 200;
	private static final double deltaAngle = (float) (2 * Math.PI / nbSegments);
	private static final float cos = (float) Math.cos(deltaAngle);
	private static final float sin = (float) Math.sin(deltaAngle);

	private static final Logger LOGGER = LoggerFactory.getLogger(Ship.class);
	// Management of the ship's ids.
	private static Integer nextId = 1;

	private final int id;

	/** The texture under the morph image. */
	private static Texture baseTexture;
	private static Texture zoomedOutTexture;

	private static final float MAX_DAMAGE = 10;
	private final List<Morph> morphs = new ArrayList<>();
	private final Map<MorphType, List<Morph>> morphsByType = new HashMap<>();

	/** The ship position in the world. */
	private final Vect3D pos = new Vect3D();

	private final Vect3D speed = new Vect3D();

	/** The ship orientation in the world. */
	private float heading;

	private final List<Order> orderList = new ArrayList<>();

	private float mass = 10;

	private boolean selected;

	private final Vect3D accel = new Vect3D();

	private final Vect3D effectiveForce = new Vect3D();

	private final Player player;
	private final Vect3D steeringForce = new Vect3D();

	private final Set<Behavior> behaviorSet = new HashSet<>();
	private final Set<Behavior> pendingRemovalBehaviors = new HashSet<>();

	private float damage = 0;
	private float maxSteeringForce;
	private float maxSpeed;

	// TODO put in conf the dimension of the table
	private final Vect3D[] trail = new Vect3D[20];
	/** Stores last trail update. It occurred less than trailUpdateInterval ago. */
	private long trailLastUpdate;
	// TODO put in conf the trail update interval
	private final int trailUpdateInterval = 50;

	private float energy;
	private final List<Order> newOrderList = new ArrayList<>();

	/***
	 * Creates a new ship with position (0, 0, 0), mass = 10 assigned to player "self".
	 */
	public Ship() {
		this(0, 0, 0, 0, 10, Model.getModel().getSelf());
	}

	public Ship(float x, float y, float z, float heading, float mass, Player player) {
		this.player = player;
		Model.getModel().getPlayers().add(player);

		synchronized (nextId) {
			id = nextId++;
		}

		// initialize positional information
		pos.copy(x, y, z);
		this.heading = heading;
		this.mass = mass;

		// initialize energy
		// TODO This should be a function of the ship's fitting
		energy = 100;

		// Add always active behaviors
		addBehavior(new StarsContribution(this));
	}

	/**
	 * Adds a behavior to the ship if the needed morphs are present is the ship
	 * @param behavior
	 * @return true if it was possible to add the behavior
	 */
	public void addBehavior(Behavior behavior) {
		// Checks that the behavior can be added to the ship
		if (behavior != null
				&& behavior.getClass().isAnnotationPresent(Needs.class)) {
			ActivatedMorph[] needs = behavior.getClass().getAnnotation(Needs.class).value();

			for (ActivatedMorph need : needs) {
				for (Morph morph : morphs) {
					if (morph.getMorphType() == need.morphType()) {
						behaviorSet.add(behavior);
						return;
					}
				}
			}

			return;
		}

		behaviorSet.add(behavior);
	}

	public void addEnergy(float energyInc) {
		// TODO implement some kind of max energy
		energy += energyInc;
	}

	public void addMorph(Morph morph) {
		morphs.add(morph);

		List<Morph> list = morphsByType.get(morph.getMorphType());
		if (list == null) {
			list = new ArrayList<>();
			morphsByType.put(morph.getMorphType(), list);
		}
		list.add(morph);

		updateMorphDependantValues();
	}

	// IMPROVE remove effectiveForce from steeringForce management ?
	// movements should add a propulsion force to the ship
	private void applySteeringForce(Vect3D force) {
		steeringForce.add(force);
		effectiveForce.add(force);
	}

	public boolean consumeEnergy(float energyDec) {
		// return true if there is enough energy
		if (energy >= energyDec) {
			// TODO implement some kind of max energy
			energy -= energyDec;
			LOGGER.debug("Energy: " + energyDec + "/" + energy);
			return true;
		}

		// return false if there isn't enough energy
		LOGGER.debug("Not enough energy: " + energyDec + "/" + energy);
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

	public float getHeading() {
		return heading;
	}

	@Override
	public int getId() {
		return id;
	}

	public float getMass() {
		return mass;
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

	public float getMaxSpeed() {
		return maxSpeed;
	}

	public float getMaxSteeringForce() {
		return maxSteeringForce;
	}

	public Vect3D getPos() {
		return pos;
	}

	public Vect3D getSpeed() {
		return speed;
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

			for (int i = 0; i < 200; i++) {
				Model.getModel()
						.getParticleEngine()
						.addParticle(new Vect3D(pos), new Vect3D(200, 0, 0).rotate((float) (Math.random() * 360)).mult((float) Math.random()),
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

	@Override
	public boolean isSelected() {
		return selected;
	}

	private void processAI() {
		// TODO Outsource this AI to allow several kinds of AIs
		// TODO implement AI processing
		// Very simple AI : wander and attack

	}

	/**
	 * Removes a behavior from the ship's behavior collection.
	 * This method postpones the behavior deletion until the end of the processing loop.
	 * This way, the handling of behaviors is insensitive to the order in which they are processed and removed.
	 * @param behavior to remove
	 */
	public void removeBehavior(Behavior behavior) {
		pendingRemovalBehaviors.add(behavior);
	}

	/**
	 * Removes all the behaviors that are of the same type
	 * @param behaviorClass
	 */
	public void removeBehaviorsByClass(Class<?> behaviorClass) {
		if (behaviorClass == null) {
			LOGGER.error("This method parameter should not be null");
		}

		CollectionUtils.filter(behaviorSet, NotPredicate.getInstance(new SameClassPredicate(behaviorClass)));
	}

	@Override
	public void render(int glMode) {

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
		float massScale = mass / 10;
		boolean maxZoom = 64f * massScale * Model.getModel().getViewport().getZoomFactor() > 15;

		// Render selection circle around the ship
		renderSelection(massScale, maxZoom);

		// Render the ship in itself
		if (Model.getModel().isDebugMode()) {
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
		if (maxZoom) {
			GL11.glScalef(massScale, massScale, 0);
			baseTexture.bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(-64, 64);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(64, 64);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(64, -64);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(-64, -64);
			GL11.glEnd();
			GL11.glScalef(1 / massScale, 1 / massScale, 0);
		} else {
			float adjustedSize = 15 / Model.getModel().getViewport().getZoomFactor();
			zoomedOutTexture.bind();
			GL11.glBegin(GL11.GL_QUADS);
			GL11.glTexCoord2f(0, 0);
			GL11.glVertex2f(-adjustedSize, adjustedSize);
			GL11.glTexCoord2f(1, 0);
			GL11.glVertex2f(adjustedSize, adjustedSize);
			GL11.glTexCoord2f(1, 1);
			GL11.glVertex2f(adjustedSize, -adjustedSize);
			GL11.glTexCoord2f(0, 1);
			GL11.glVertex2f(-adjustedSize, -adjustedSize);
			GL11.glEnd();
		}

		GL11.glRotatef(-heading, 0, 0, 1);

		// Render ship forces
		if (Model.getModel().isDebugMode()) {
			GL11.glColor3f(1, 1, 0);
			effectiveForce.render(glMode, 1);
		}

		GL11.glTranslatef(-pos.x, -pos.y, -pos.z);

		// Render behaviors
		for (Behavior behavior : behaviorSet) {
			if (behavior instanceof Renderable) {
				((Renderable) behavior).render(glMode);
			}
		}

	}

	private void renderSelection(float massScale, boolean maxZoom) {
		if (selected) {
			// render limit of effect zone
			TextureImpl.bindNone();
			float tInt = 0; // temporary data holder
			float tExt = 0; // temporary data holder
			float xInt;
			float xExt;
			if (maxZoom) {
				xInt = 64 * massScale - 15; // radius
				xExt = 64 * massScale - 15 + 6 / Model.getModel().getViewport().getZoomFactor(); // radius
			} else {
				xInt = 15f / Model.getModel().getViewport().getZoomFactor(); // radius
				xExt = 21f / Model.getModel().getViewport().getZoomFactor(); // radius
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
			newHeading = new Vect3D(0, 1, 0).angleWith(steeringForce);
		} else if (headingFactor > 0) {
			newHeading = new Vect3D(0, 1, 0).angleWith(new Vect3D(steeringForce).mult(headingFactor).add(new Vect3D(speed).mult(1 - headingFactor / 3)));
		} else {
			newHeading = new Vect3D(0, 1, 0).angleWith(speed);
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
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public String toString() {
		return "ship:" + pos.toString();
	}

	@Override
	public void update() {
		float secondsSinceLastUpdate = Model.getModel().getSecondsSinceLastUpdate();
		if (secondsSinceLastUpdate == 0f) {
			return;
		}

		// TODO Is this really the proper way to do it ?
		accel.nullify();
		effectiveForce.nullify();
		steeringForce.nullify();

		// handle AI assignements if appropriate
		if (player.getPlayerType() == PlayerType.AI) {
			processAI();
		}

		// if no movement needed, no update needed
		for (Behavior behavior : behaviorSet) {
			if (behavior.isActive()) {
				behavior.run(secondsSinceLastUpdate);

				// if the behavior is a movement, use the generated steering force
				if (behavior instanceof Movement && ((Movement) behavior).consumeEnergy()) {
					applySteeringForce(((Movement) behavior).getSteeringForce());
				}

				// if the behavior is generating a force, we must apply it
				if (behavior instanceof ForceGeneratingBehavior) {
					effectiveForce.add(((ForceGeneratingBehavior) behavior).getNonSteeringForce());
				}

			}
		}

		// rotate and add trail according to the steering force vector
		rotateProperly();

		// acceleration = steering_force / mass
		accel.add(effectiveForce);
		// velocity = truncate (velocity + acceleration, max_speed)
		speed.add(new Vect3D(accel).mult(secondsSinceLastUpdate)).truncate(maxSpeed);
		// position = position + velocity
		pos.add(new Vect3D(speed).mult(secondsSinceLastUpdate));

		// Handle orders
		for (Order order : orderList) {
			handleOrder(order);
		}
		orderList.clear();
		orderList.addAll(newOrderList);
		newOrderList.clear();

		// If the mass of the current ship is null or below, remove it
		if (mass <= 0) {
			Model.getModel().removeEntity(this);
		}

		// update trail
		if (trailLastUpdate == 0 || Model.getModel().getLastUpdateTS() - trailLastUpdate > trailUpdateInterval) {
			for (int i = trail.length - 2; i >= 0; i--) {
				trail[i + 1] = trail[i];
			}
			trail[0] = new Vect3D(pos);
			trailLastUpdate += trailUpdateInterval;
		}

		// Cleaning
		for (Behavior behavior : pendingRemovalBehaviors) {
			behaviorSet.remove(behavior);
		}
	}

	private void updateMorphDependantValues() {
		// Compute morphs level dependant values
		// TODO Update these values each time a morph is upgraded
		int maxSimplePropulsorLevel = getMaxLevelForMorphType(MorphType.SIMPLE_PROPULSOR);
		maxSteeringForce = (float) (Conf.getIntProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXFORCE)
				* Math.pow(Conf.getFloatProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXFORCE_FACTORPERLEVEL), maxSimplePropulsorLevel));
		maxSpeed = (float) (Conf.getIntProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXSPEED)
				* Math.pow(Conf.getFloatProperty(ConfItem.MORPH_SIMPLEPROPULSOR_MAXSPEED_FACTORPERLEVEL), maxSimplePropulsorLevel));
	}
}
