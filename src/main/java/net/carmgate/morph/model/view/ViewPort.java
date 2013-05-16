package net.carmgate.morph.model.view;

import net.carmgate.morph.model.common.Vect3D;

/**
 * This class represents the properties of the view through which the world is seen.
 * The viewport is rectangular in shape.
 */
public class ViewPort {

	/** The intersection of the diagonals of the viewport (in world coordinates). */
	private final Vect3D focalPoint = new Vect3D();

	/** The rotation of the scene around the focal point (in radians). */
	private final float rotation = 0;

	/** The zoom factor. > 1 means what you see is bigger. */
	private float zoomFactor = 0.5f;

	/** The intersection of the diagonals of the viewport (in <b>world coordinates</b>).*/
	public Vect3D getFocalPoint() {
		return focalPoint;
	}

	/** The rotation of the scene around the focal point. */
	public float getRotation() {
		return rotation;
	}

	/** The zoom factor. > 1 means what you see is bigger. */
	public float getZoomFactor() {
		return zoomFactor;
	}

	public void setZoomFactor(float zoomFactor) {
		this.zoomFactor = zoomFactor;
	}

}
