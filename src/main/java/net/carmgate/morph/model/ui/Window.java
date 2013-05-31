package net.carmgate.morph.model.ui;

import net.carmgate.morph.conf.Conf;

public class Window {
	private int width = Conf.getIntProperty("window.initialWidth");
	private int height = Conf.getIntProperty("window.initialHeight");

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

}
