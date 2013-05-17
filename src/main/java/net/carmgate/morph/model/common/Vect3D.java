package net.carmgate.morph.model.common;

import net.carmgate.morph.model.entities.Renderable;

import org.lwjgl.opengl.GL11;

/**
 * A vector class holding some common vector operations.
 * @author Carm
 */
public class Vect3D implements Renderable {

	public static final Vect3D NORTH = new Vect3D(0, -1, 0);
	public static final Vect3D NULL = new Vect3D(0, 0, 0);

	public float x;
	public float y;
	public float z;

	public Vect3D() {
	}

	/**
	 * Simple constructor.
	 * @param x
	 * @param y
	 * @param z
	 */
	public Vect3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Recopy constructor.
	 * @param p3d
	 */
	public Vect3D(Vect3D p3d) {
		this(p3d.x, p3d.y, p3d.z);
	}

	/**
	 * Addition
	 * @param vector
	 */
	public Vect3D add(Vect3D vector) {
		x += vector.x;
		y += vector.y;
		z += vector.z;
		return this;
	}

	/**
	 * @param vector vector 2.
	 * @return the angle (in degrees) between current vector and the one provided.
	 */
	public float angleWith(Vect3D vector) {
		float scal = x * vector.x + y * vector.y;
		float vect = x * vector.y - y * vector.x;

		float angle; // in radians
		if (Math.abs(scal) > 0.0000001) { // the minimal value must be big enough to get rid of float rounding errors
			angle = (float) Math.atan(vect / scal);
		} else {
			angle = (float) Math.PI / 2;
		}
		if (scal < 0) {
			angle = (float) (Math.PI + angle);
		}

		// Convert in degrees before returning
		float angleInDegrees = (float) Math.toDegrees(angle);
		if (angleInDegrees > 180) {
			angleInDegrees = angleInDegrees - 360;
		}
		return angleInDegrees;
	}

	/**
	 * Copy the coordinates of the provided vector.
	 * @param vect3d
	 */
	public Vect3D copy(Vect3D vect3d) {
		x = vect3d.x;
		y = vect3d.y;
		z = vect3d.z;
		return this;
	}

	/**
	 * @param vect
	 * @return returns the distance between two points given as Vect3D.
	 */
	public float distance(Vect3D vect) {
		Vect3D tmpVect = new Vect3D(this);
		tmpVect.substract(vect);
		return tmpVect.modulus();
	}

	/**
	 * A vector is considered equal to the current one if their coordinates are exactly the same.
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vect3D)) {
			return false;
		}

		Vect3D vect3d = (Vect3D) obj;

		return x == vect3d.x && y == vect3d.y && z == vect3d.z;
	}

	/**
	 * @return x.10^10 + y.10^5 + z
	 */
	@Override
	public int hashCode() {
		return (int) (x * Math.pow(10, 10) + y * Math.pow(10, 5) + z);
	}

	@Override
	public void initRenderer() {
		// nothing to do
	}

	/**
	 * @return the modulus of current vector.
	 */
	public float modulus() {
		return (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
	}

	public Vect3D mult(float scalar) {
		x *= scalar;
		y *= scalar;
		z *= scalar;
		return this;
	}

	/**
	 * Changes coordinates so that returnedVector = (newModulus/currentModulus).currentVector.
	 * The new vector has a modulus equal to newModulus.
	 * @param newModulus
	 */
	public Vect3D normalize(float newModulus) {
		float oldModulus = modulus();

		if (oldModulus == 0) {
			// impossible to normalize
			return this;
		}

		x = x / oldModulus * newModulus;
		y = y / oldModulus * newModulus;
		z = z / oldModulus * newModulus;
		return this;
	}

	/**
	 * @param vector
	 * @return the scalar product of the current vector and the provided one.
	 */
	public float prodScal(Vect3D vector) {
		return vector.x * x + vector.y * y + vector.z * z;
	}

	/**
	 * @param vect
	 * @return the project on the Z-axis of the cross product of currentVect and parameter vect
	 */
	public float prodVectOnZ(Vect3D vect) {
		return x * vect.y - y * vect.x;
	}

	@Override
	public void render(int glMode, RenderingType renderingType) {
		render(glMode, renderingType, 1);
	}

	public void render(int glMode, RenderingType renderingType, int exagerate) {
		Vect3D vector = new Vect3D(this).mult(exagerate);
		Vect3D origin = new Vect3D(vector);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(2.0f);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glBegin(GL11.GL_LINES);

		// main line
		GL11.glVertex3f(0, 0, 0);
		GL11.glVertex3f(vector.x, vector.y, vector.z);

		// arrow detail
		vector.normalize(vector.modulus() / 3);
		vector.rotate(20);
		GL11.glVertex3f(origin.x, origin.y, origin.z);
		GL11.glVertex3f(origin.x - vector.x,
				origin.y - vector.y,
				origin.z - vector.z);
		vector.rotate(-40);
		GL11.glVertex3f(origin.x, origin.y, origin.z);
		GL11.glVertex3f(origin.x - vector.x, origin.y - vector.y, origin.z - vector.z);

		GL11.glEnd();
	}

	/**
	 * Rotates a vector by the given angles in degrees.
	 * @param angle the rotation angles in degrees along the z axis.
	 */
	public Vect3D rotate(float angle) {
		float newX = (float) (Math.cos(Math.toRadians(angle)) * x - Math.sin(Math.toRadians(angle)) * y);
		float newY = (float) (Math.sin(Math.toRadians(angle)) * x + Math.cos(Math.toRadians(angle)) * y);
		x = newX;
		y = newY;
		return this;
	}

	/**
	 * Substraction.
	 * @param vector
	 */
	public Vect3D substract(Vect3D vector) {
		x -= vector.x;
		y -= vector.y;
		z -= vector.z;
		return this;
	}

	@Override
	public String toString() {
		return "(" + x + ", " + y + ", " + z + ")";
	}

	public Vect3D truncate(float scalar) {
		normalize(Math.min(modulus(), scalar));
		return this;
	}

}
