package net.carmgate.morph.model.entities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Renderable;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 0 1 2 3 from top left clockwise
public class WorldArea implements Renderable {
	private static Integer nextId = 0;
	private static final int GRID_SIZE = 512;
	private static final Logger LOGGER = LoggerFactory.getLogger(WorldArea.class);
	private static Texture[] textures = new Texture[8];
	private final Vect3D center;
	private WorldArea parent;

	private WorldArea[] children;
	private final long geoHash;
	private final int level;
	private Integer id;
	private final int width;

	public WorldArea() {
		this(0, 0x22222222, new Vect3D());
	}

	private WorldArea(int level, long geoHash, Vect3D center) {
		synchronized (nextId) {
			id = nextId++;
		}

		this.level = level;
		this.geoHash = geoHash;
		this.center = center;
		width = GRID_SIZE << level + 1;
	}

	public WorldArea createDescendantWA(Vect3D focalPoint, int descendantLevel) {
		WorldArea currentWA = this;
		while (currentWA.getLevel() > descendantLevel) {
			if (focalPoint.x < currentWA.center.x) {
				if (focalPoint.y > currentWA.center.y) {
					currentWA = currentWA.getChild(0);
				} else {
					currentWA = currentWA.getChild(3);
				}
			} else {
				if (focalPoint.y > currentWA.center.y) {
					currentWA = currentWA.getChild(1);
				} else {
					currentWA = currentWA.getChild(2);
				}
			}
		}

		return currentWA;
	}

	public Vect3D getCenter() {
		return center;
	}

	public WorldArea getChild(int i) {
		return getChild(i, true);
	}

	public WorldArea getChild(int i, boolean lazyCreate) {
		if (level == 0) {
			return null;
		}

		// Create the table if it does not exist and if lazy creation is required
		if (children == null) {
			if (lazyCreate) {
				children = new WorldArea[4];
			} else {
				// if there is no children array, the rest of the method will throw a NPE.
				return null;
			}
		}

		// Create a new child if there is none and if lazy creation is required
		if (children[i] == null) {
			Vect3D childCenter = new Vect3D().copy(center);
			// IMPROVE Check what the maximum is and put constraint on the level.
			long levelWidth = GRID_SIZE << level;
			switch (i) {
			case 0:
				childCenter.copy(center.x - levelWidth / 2, center.y + levelWidth / 2, center.z);
				break;
			case 1:
				childCenter.copy(center.x + levelWidth / 2, center.y + levelWidth / 2, center.z);
				break;
			case 2:
				childCenter.copy(center.x + levelWidth / 2, center.y - levelWidth / 2, center.z);
				break;
			case 3:
				childCenter.copy(center.x - levelWidth / 2, center.y - levelWidth / 2, center.z);
				break;
			default:
				throw new IllegalArgumentException("getChild cannot be called with i > 3");
			}
			children[i] = new WorldArea(level - 1, geoHash + (i << 2 * (level - 1)), childCenter);
			children[i].setParent(this);
		}
		return children[i];
	}

	public long getGeoHash() {
		return geoHash;
	}

	public int getLevel() {
		return level;
	}

	public List<WorldArea> getNotNullChildren() {
		List<WorldArea> result = new ArrayList<>();

		if (children == null) {
			return result;
		}

		for (WorldArea wa : children) {
			if (wa != null) {
				result.add(wa);
			}
		}
		return result;
	}

	public Set<WorldArea> getOverlappingWAs(Vect3D searchCenter, float radius) {

		// go to root
		WorldArea root = this;
		while (root.hasParent()) {
			root = root.getParent();
		}

		Set<WorldArea> matchingAreas = new HashSet<>();
		List<WorldArea> currentlyExaminedAreas = new ArrayList<>();
		List<WorldArea> tempAreas = new ArrayList<>();
		tempAreas.add(root);
		if (tempAreas.isEmpty()) {
			matchingAreas.add(root);
		}

		while (!tempAreas.isEmpty()) {
			currentlyExaminedAreas.clear();
			currentlyExaminedAreas.addAll(tempAreas);
			tempAreas.clear();

			for (WorldArea wa : currentlyExaminedAreas) {
				if (wa.getNotNullChildren().isEmpty()) {
					matchingAreas.add(wa);
				} else {
					boolean match = false;
					for (WorldArea wa2 : wa.getNotNullChildren()) {
						if (Math.abs(wa2.center.x - searchCenter.x) <= wa2.width / 2 + radius
								&& Math.abs(wa2.center.y - searchCenter.y) <= wa2.width / 2 + radius) {
							tempAreas.add(wa2);
							match = true;
						}
					}
					if (!match) {
						matchingAreas.add(wa);
					}
				}
			}
		}

		return matchingAreas;
	}

	public WorldArea getParent() {
		if (parent == null) {
			long tempWidth = GRID_SIZE << level;
			Vect3D parentCenter;
			int currentQuadrant = (int) (geoHash >> 2 * level) % 4;
			switch (currentQuadrant) {
			case 0:
				parentCenter = new Vect3D(center.x + tempWidth, center.y - tempWidth, center.z);
				break;
			case 1:
				parentCenter = new Vect3D(center.x - tempWidth, center.y - tempWidth, center.z);
				break;
			case 2:
				parentCenter = new Vect3D(center.x - tempWidth, center.y + tempWidth, center.z);
				break;
			case 3:
				parentCenter = new Vect3D(center.x + tempWidth, center.y + tempWidth, center.z);
				break;
			default:
				throw new IllegalArgumentException("Wrong quadrant was extracted from the geoHash");
			}
			parent = new WorldArea(level + 1, geoHash >> 2 << 2, parentCenter);
			parent.setChild((int) (geoHash % 4), this);
		}
		return parent;
	}

	private boolean hasParent() {
		return parent != null;
	}

	@Override
	public void initRenderer() {
		for (int i = 0; i < textures.length; i++) {
			try (FileInputStream fileInputStream = new FileInputStream(ClassLoader.getSystemResource("img/stars" + (i + 1) + ".png").getPath())) {
				textures[i] = TextureLoader.getTexture("PNG", fileInputStream);
			} catch (IOException e) {
				LOGGER.error("Exception raised while loading texture", e);
			}
		}
	}

	@Override
	public void render(int glMode) {
		if (level > 0) {
			for (WorldArea wa : getNotNullChildren()) {
				wa.render(glMode);
			}
			return;
		}

		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
		GL11.glTranslatef(center.x, center.y, 0);
		GL11.glColor4f(1, 1, 1, 1);

		textures[id % 8].bind();
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2f(-GRID_SIZE, GRID_SIZE);
		GL11.glTexCoord2f(1, 0);
		GL11.glVertex2f(GRID_SIZE, GRID_SIZE);
		GL11.glTexCoord2f(1, 1);
		GL11.glVertex2f(GRID_SIZE, -GRID_SIZE);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2f(-GRID_SIZE, -GRID_SIZE);
		GL11.glEnd();

		GL11.glTranslatef(-center.x, -center.y, 0);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	private void setChild(int i, WorldArea child) {
		if (children == null) {
			children = new WorldArea[4];
		}
		children[i] = child;
	}

	private void setParent(WorldArea parent) {
		this.parent = parent;
	}

}
