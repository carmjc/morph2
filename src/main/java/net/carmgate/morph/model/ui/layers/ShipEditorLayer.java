package net.carmgate.morph.model.ui.layers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.carmgate.morph.model.entities.Morph;
import net.carmgate.morph.model.entities.Morph.MorphType;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.entities.common.Renderable;

import org.lwjgl.opengl.GL11;

public class ShipEditorLayer implements Renderable {

	private Ship ship;

	@Override
	public void initRenderer() {
		// Nothing to do
	}

	@Override
	public void render(int glMode) {
		List<Morph> morphsToDraw = new ArrayList<>();
		for (MorphType morphType : MorphType.values()) {
			List<Morph> morphsByType = ship.getMorphsByType(morphType);
			if (morphsByType != null) {
				morphsToDraw.addAll(morphsByType);
			}
		}

		int layer = 0;
		Iterator<Morph> morphIt = morphsToDraw.iterator();
		while (morphIt.hasNext()) {

			// draw the most centric one
			if (layer == 0) {
				if (morphIt.hasNext()) {
					Morph morph = morphIt.next();
					GL11.glPushName(morph.getId());
					morph.render(glMode);
					GL11.glPopName();
					GL11.glTranslatef(-64, 0, 0);
				}
			}

			GL11.glTranslatef(64, 0, 0);
			GL11.glRotatef(60, 0, 0, 1);
			for (int i = 0; i < 6; i++) {
				GL11.glRotatef(60, 0, 0, 1);
				for (int j = 0; j < layer; j++) {
					if (morphIt.hasNext()) {
						Morph morph = morphIt.next();
						GL11.glRotatef(-(i + 2) * 60, 0, 0, 1);
						GL11.glPushName(morph.getId());
						morph.render(glMode);
						GL11.glPopName();
						GL11.glRotatef((i + 2) * 60, 0, 0, 1);
					}
					GL11.glTranslatef(64, 0, 0);
				}
			}
			GL11.glRotatef(-60, 0, 0, 1);

			layer++;
		}
		GL11.glTranslatef(-(layer - 1) * 64, 0, 0);
	}

	public void setShip(Ship ship) {
		this.ship = ship;
	}
}
