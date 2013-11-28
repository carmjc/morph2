package net.carmgate.morph.model.events;

import net.carmgate.morph.model.entities.Morph;

public class MorphLevelUp implements Event {

	private Morph morph;

	public MorphLevelUp(Morph morph) {
		this.morph = morph;

	}

	public Morph getMorph() {
		return morph;
	}
}
