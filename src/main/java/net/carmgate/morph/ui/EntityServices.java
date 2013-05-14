package net.carmgate.morph.ui;

import net.carmgate.morph.ui.renderer.Renderer;

public class EntityServices<T> {
	private Renderer<T> renderer;

	public <R extends T> Renderer<T> getRenderer() {
		return renderer;
	}

	public void setRenderer(Renderer<T> renderer) {
		this.renderer = renderer;
	}
}
