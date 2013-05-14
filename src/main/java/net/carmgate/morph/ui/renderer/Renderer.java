package net.carmgate.morph.ui.renderer;

import org.lwjgl.opengl.GL11;

/**
 * Classes inheriting this interface allow to draw a member object in the gl scene.
 */
public interface Renderer<T> {

	/**
	 * Allows to switch between normal and debug rendering.
	 */
	public static enum RenderingType {
		NORMAL,
		DEBUG
	}

	/**
	 * Initialize resources if needed.
	 */
	void init();

	/**
	 * Draw the member object in the scene.
	 * @param glMode {@link GL11#GL_RENDER} or {@link GL11#GL_SELECT}
	 * @param renderingType {@link RenderingType}
	 * @param sceneItem the scene item to render.
	 */
	void render(int glMode, RenderingType renderingType, T sceneItem);
}
