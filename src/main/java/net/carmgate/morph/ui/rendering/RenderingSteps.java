package net.carmgate.morph.ui.rendering;

import org.apache.commons.collections.CollectionUtils;

public enum RenderingSteps {
	WORLDAREA,
	SHIP;

	private static RenderingSteps[] reverseValuesArray;

	public static RenderingSteps[] reverseValues() {
		if (reverseValuesArray == null) {
			reverseValuesArray = values().clone();
			CollectionUtils.reverseArray(reverseValuesArray);
		}
		return reverseValuesArray;
	}
}
