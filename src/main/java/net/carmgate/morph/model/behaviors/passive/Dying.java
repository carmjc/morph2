package net.carmgate.morph.model.behaviors.passive;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.behaviors.common.Behavior;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.model.entities.common.listener.DeathListener;

public class Dying implements Behavior, Renderable {

	private Entity target;

	@Deprecated
	public Dying() {
	}

	public Dying(Entity target) {
		this.target = target;

	}

	@Override
	public Behavior cloneForEntity(Entity entity) {
		return null;
	}

	@Override
	public void computeXpContribution() {
	}

	@Override
	public void initRenderer() {
	}

	@Override
	public void render(int glMode) {
		for (int i = 0; i < 200; i++) {
			Model.getModel()
			.getParticleEngine()
			.addParticle(
					new Vect3D(target.getPos()),
					new Vect3D(200, 0, 0).rotate((float) (Math.random() * 360)).mult((float) Math.random()).add(
							target.getSpeed()),
							2, 0.5f,
							0.5f, 0.05f);
		}

	}

	@Override
	public void run() {
		target.setDead(true);
		Model.getModel().removeEntity(target);

		// TODO maybe this should better be handled by the Model or the Entity ?
		for (DeathListener lst : target.getDeathListeners()) {
			lst.handleDeathEvent(target);
		}

	}

}
