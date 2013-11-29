package net.carmgate.morph.model.ai;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.InflictLaserDamage;
import net.carmgate.morph.model.behaviors.common.Movement;
import net.carmgate.morph.model.behaviors.steering.Break;
import net.carmgate.morph.model.behaviors.steering.Flee;
import net.carmgate.morph.model.behaviors.steering.Follow;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.events.Event;
import net.carmgate.morph.model.events.TakeDamage;
import net.carmgate.morph.model.player.Player.PlayerType;

import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BalancedAI {

	private class DamageTaken {
		protected long firstHitTs;
		protected long lastHitTs;
		protected float totalDamageTaken;
		protected float dps;
	}

	private final static Logger LOGGER = LoggerFactory.getLogger(BalancedAI.class);

	private Map<Entity, DamageTaken> damage = new HashMap<>();
	private Set<Entity> enemies = new HashSet<>();
	private Entity mostDangerousEnemy;
	private Ship ship;

	/** Instant DPS from all damage sources. */
	private float currentTotalDpsTaken;
	private long lastTsOfDamageTaken;

	private boolean fleeing;

	private Entity target;

	public BalancedAI(Ship ship) {
		this.ship = ship;
	}

	public BalancedAI cloneForShip(Ship ship) {
		BalancedAI balancedAI = new BalancedAI(ship);
		return balancedAI;
	}

	/**
	 * Compute values needed by the AI.
	 */
	private void computePremices() {
		computePremicesDamageTaken();
	}

	/**
	 * Compute values needed by the AI regarding damage taken.
	 */
	private void computePremicesDamageTaken() {
		float maxDps = 0;
		currentTotalDpsTaken = 0;

		for (Entry<Entity, DamageTaken> entry : damage.entrySet()) {
			DamageTaken dt = entry.getValue();

			// Compute dps taken by Ship as a whole
			if (Model.getModel().getCurrentTS() - dt.firstHitTs > 500) {
				dt.dps = dt.totalDamageTaken / (Model.getModel().getCurrentTS() - dt.firstHitTs) * 1000;
				LOGGER.debug("taking dps: " + dt.dps);
			}

			// Assume dps = 0 if no hit during last second
			// TODO We should compute a moving average considering the hitting frequency of the damage source
			if (Model.getModel().getCurrentTS() - dt.lastHitTs > 1000) {
				LOGGER.debug("No damage for last 1s");
				dt.dps = 0;
			}

			currentTotalDpsTaken += dt.dps;
			if (dt.dps > maxDps) {
				mostDangerousEnemy = entry.getKey();
			}
		}
	}

	private Set<Entity> detectEnemies() {
		return Model.getModel().findEntitiesWithinDistanceOfLocationAndNotPlayerOwned(ship.getPos(),
				1000, new Predicate() {

			@Override
			public boolean evaluate(Object object) {
				Entity entity = (Entity) object;
						return entity.getPlayer() != ship.getPlayer()
								&& entity.getPlayer().getPlayerType() != PlayerType.NOONE;
			}
		});
	}

	public void handleEvent(Event event) {
		if (event instanceof TakeDamage) {
			LOGGER.debug("Caught event : " + ((TakeDamage) event).getDamageAmount());

			Entity entity = ((TakeDamage) event).getSourceOfDamage();
			DamageTaken dt = damage.get(entity);
			if (dt == null) {
				dt = new DamageTaken();
				damage.put(entity, dt);
				enemies.add(entity);
			}

			if (dt.firstHitTs == 0) {
				dt.firstHitTs = Model.getModel().getCurrentTS();
			}
			dt.lastHitTs = Model.getModel().getCurrentTS();
			dt.totalDamageTaken += ((TakeDamage) event).getDamageAmount();

			lastTsOfDamageTaken = Model.getModel().getCurrentTS();

			// TODO Compute the highest distance at which the enemy has dealt some damage, to set it as the min fleeing
			// distance.
		}
	}

	public void run() {
		computePremices();
		Set<Entity> detectedEnemies = detectEnemies();

		// If dps is too high to win the battle : flee
		if (currentTotalDpsTaken > ship.getMaxDpsInflictable()) {
			LOGGER.debug("If I do not flee, the battle will end in a loss or a draw (" + currentTotalDpsTaken + "/"
					+ ship.getMaxDpsInflictable() + ")");
			if (!ship.hasBehaviorByClass(Flee.class)) {
				// TODO We should flee the barycenter of enemies positions weighted by the dps they deal
				LOGGER.debug("Run, Forest, run !!");
				ship.removeBehaviorsByClass(Movement.class);
				ship.addBehavior(new Flee(ship, mostDangerousEnemy, 1000));
				fleeing = true;
			}
		}

		// if we are fleeing but have not taken any damage for more than 2s, the break and reset AI
		if (fleeing) {
			if (ship.getPos().distance(new Vect3D(mostDangerousEnemy.getPos()).add(mostDangerousEnemy.getSpeed())) > 1000) {
				LOGGER.debug("Now, we're safe ... no damage for more than 3s");
				ship.removeBehaviorsByClass(Flee.class);
				ship.addBehavior(new Break(ship));
			} else {
				if (!ship.hasBehaviorByClass(Flee.class)) {
					ship.removeBehaviorsByClass(Movement.class);
					ship.addBehavior(new Flee(ship, mostDangerousEnemy, 200)); // The 200 is useless for now as it's not used by
				}
			}
		} else {
			// LOGGER.debug(fleeing + " - " + (Model.getModel().getCurrentTS() - lastTsOfDamageTaken) + " - "
			// + ship.hasBehaviorByClass(Flee.class) + " - " + ship.hasBehaviorByClass(Break.class));
			// LOGGER.debug("Inflictable damage: " + ship.getMaxDpsInflictable() + " - detected enemies: "
			// + detectedEnemies.size());

			if (ship.getMaxDpsInflictable() > 0 && !detectedEnemies.isEmpty() && target == null) {
				target = detectedEnemies.iterator().next();
				LOGGER.debug("Removing movement");
				ship.removeBehaviorsByClass(Movement.class);
				ship.addBehavior(new Follow(ship, target, 200));
				ship.addBehavior(new InflictLaserDamage(ship, target));
			}
		}

	}
}
