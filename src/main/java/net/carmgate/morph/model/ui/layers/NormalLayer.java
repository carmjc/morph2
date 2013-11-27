package net.carmgate.morph.model.ui.layers;

import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.common.Entity;
import net.carmgate.morph.model.entities.common.Renderable;
import net.carmgate.morph.ui.common.RenderingSteps;

import org.lwjgl.opengl.GL11;

public class NormalLayer implements Renderable {

	@Override
	public void initRenderer() {
	}

	@Override
	public void render(int glMode) {
		Vect3D focalPoint = Model.getModel().getViewport().getFocalPoint();
		float zoomFactor = Model.getModel().getViewport().getZoomFactor();
		if (Model.getModel().getViewport().getLockedOnEntity() != null) {
			focalPoint.copy(new Vect3D().add(Model.getModel().getViewport().getLockedOnEntity().getPos()).mult(
					zoomFactor));
		}

		GL11.glTranslatef(-focalPoint.x, -focalPoint.y, -focalPoint.z);
		GL11.glScalef(zoomFactor, zoomFactor, 1);

		Model.getModel().getRootWA().render(glMode);

		// Rendering all renderable elements
		for (RenderingSteps renderingStep : RenderingSteps.values()) {
			if (Model.getModel().getEntitiesByRenderingType(renderingStep) != null) {
				for (Entity renderable : Model.getModel().getEntitiesByRenderingType(renderingStep).values()) {
					renderable.render(glMode);
				}
			}
		}

		// Render particles
		Model.getModel().getParticleEngine().render(glMode);

		GL11.glScalef(1f / zoomFactor, 1f / zoomFactor, 1);
		GL11.glTranslatef(focalPoint.x, focalPoint.y, focalPoint.z);
	}
}