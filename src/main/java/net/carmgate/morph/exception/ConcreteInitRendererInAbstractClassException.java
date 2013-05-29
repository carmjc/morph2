package net.carmgate.morph.exception;

import net.carmgate.morph.model.entities.common.Renderable;

public class ConcreteInitRendererInAbstractClassException extends Exception {

	private final Class<? extends Renderable> renderable;

	public ConcreteInitRendererInAbstractClassException(Class<? extends Renderable> renderable) {
		super("There is a concrete initRenderer method in class " + renderable.getCanonicalName());
		this.renderable = renderable;
	}

	public Class<? extends Renderable> getRenderable() {
		return renderable;
	}

}
