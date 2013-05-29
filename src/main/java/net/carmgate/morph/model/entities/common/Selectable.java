package net.carmgate.morph.model.entities.common;

public interface Selectable {
	int getId();

	boolean isSelected();

	void setSelected(boolean selected);
}
