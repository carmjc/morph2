package net.carmgate.morph.model.behaviors;

public interface Behavior {

	abstract boolean isActive();

	abstract void run(float secondsSinceLastUpdate);

}
