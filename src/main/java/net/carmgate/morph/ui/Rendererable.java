package net.carmgate.morph.ui;

import org.lwjgl.opengl.GL11;

/**
 * Classes inheriting this interface allow to draw themselves in the gl scene.
 */
public interface Rendererable {

	/**
	 * Allows to switch between normal and debug rendering.
	 */
	public static enum RenderingType {
		NORMAL,
		DEBUG
	}

	/**
	 * Initialize resources if needed.
	 * This method is called only once during all game execution.
	 */
	void initRenderer();

	/**
	 * Draw the current object in the scene.
	 * @param glMode {@link GL11#GL_RENDER} or {@link GL11#GL_SELECT}
	 * @param renderingType {@link RenderingType}
	 */
	void render(int glMode, RenderingType renderingType);
}
