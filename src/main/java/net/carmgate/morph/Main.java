package net.carmgate.morph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.ui.UIEvent;
import net.carmgate.morph.ui.UIEvent.EventType;
import net.carmgate.morph.ui.renderer.Renderer.RenderingType;
import net.carmgate.morph.ui.renderer.ShipRenderer;
import net.carmgate.morph.uihandler.drag.DragContext;
import net.carmgate.morph.uihandler.drag.DraggedWorld;
import net.carmgate.morph.uihandler.drag.DraggingWorld;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

	private static Logger LOGGER = LoggerFactory.getLogger(Main.class);

	/**
	 * Main Class
	 */
	public static void main(String[] argv) {
		Main sample = new Main();
		sample.start();
	}

	private final Model globalModel = Model.getModel();

	private final Vect3D holdWorldMousePos = null;

	private Vect3D oldFP;
	private Vect3D oldMousePosInWindow;
	private final Map<List<UIEvent>, List<Runnable>> uiHandlerConfs = new HashMap<>();

	// public void pick(int x, int y) {
	//
	// logger.debug("Picking at " + x + " " + y);
	// if (World.getWorld().getSelectedShip() != null) {
	// logger.debug("Selected ship: " + World.getWorld().getSelectedShip().pos);
	// }
	//
	// // get viewport
	// IntBuffer viewport = BufferUtils.createIntBuffer(16);
	// GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
	//
	// IntBuffer selectBuf = BufferUtils.createIntBuffer(512);
	// GL11.glSelectBuffer(selectBuf);
	// GL11.glRenderMode(GL11.GL_SELECT);
	//
	// GL11.glInitNames();
	// GL11.glPushName(-1);
	//
	// GL11.glMatrixMode(GL11.GL_PROJECTION);
	// GL11.glPushMatrix();
	// GL11.glLoadIdentity();
	// // GL11.glScalef(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
	// float pickMatrixX = x; // SCALE_FACTOR;
	// float pickMatrixY = y; // SCALE_FACTOR;
	// GLU.gluPickMatrix(pickMatrixX, pickMatrixY, 6.0f, 6.0f, viewport);
	// GLU.gluOrtho2D(0, WIDTH, 0, HEIGHT);
	//
	// worldRenderer.render(GL11.GL_SELECT, null, globalModel);
	//
	// GL11.glMatrixMode(GL11.GL_PROJECTION);
	// GL11.glPopMatrix();
	// GL11.glFlush();
	//
	// int hits = GL11.glRenderMode(GL11.GL_RENDER);
	//
	// if (hits == 0) {
	// if (globalModel.getSelectedShip() != null) {
	// globalModel.getSelectedShip().setSelectedMorph(-1);
	// }
	// globalModel.setSelectedShip(-1);
	// return;
	// }
	//
	// int j = 0;
	// Ship lastSelectedShip = globalModel.getSelectedShip();
	// int index = selectBuf.get(j + 4);
	// globalModel.setSelectedShip(index);
	// if (lastSelectedShip != null && lastSelectedShip ==
	// globalModel.getSelectedShip()) {
	// globalModel.getSelectedShip().toggleSelectedMorph(selectBuf.get(j + 5));
	// }
	// }
	//

	/**
	 * Initialise the GL display
	 * 
	 * @param width
	 *            The width of the display
	 * @param height
	 *            The height of the display
	 */
	private void initGL(int width, int height) {
		try {
			Display.setDisplayMode(new DisplayMode(width, height));
			Display.create();
			Display.setTitle("Morph");
			Display.setVSyncEnabled(true);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.exit(0);
		}

		// enable texturing
		// GL11.glEnable(GL11.GL_TEXTURE_2D);
		// It seems it's not needed, but I do not understand why ...

		// set clear color - Wont be needed once we have a background
		GL11.glClearColor(0f, 0f, 0f, 0);

		// enable alpha blending
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		// set viewport
		// TODO: compute the viewport from the windows size and zoom/scale
		ViewPort viewport = globalModel.getViewport();
		Vect3D focalPoint = viewport.getFocalPoint();

		// TODO : test the zoom factor
		GLU.gluOrtho2D(focalPoint.x - width / viewport.getZoomFactor() / 2, focalPoint.x + width / viewport.getZoomFactor() / 2, focalPoint.y + height
				/ viewport.getZoomFactor() / 2, focalPoint.y - height / viewport.getZoomFactor() / 2);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	/**
	 * This method initializes UI handlers.
	 * Some special case handlers can not be initialized dynamically at the moment.
	 */
	private void initUIHandlers() {
		// Init dragging UI Handlers
		DragContext dragContext = new DragContext();
		DraggingWorld draggingWorld = new DraggingWorld(dragContext);
		DraggedWorld draggedWorld = new DraggedWorld(dragContext);
		LinkedList<UIEvent> confList = new LinkedList<>();
		confList.add(new UIEvent(EventType.MOUSE_BUTTON_DOWN, 0, null, 0));
		uiHandlerConfs.put(confList, new ArrayList<Runnable>());
		uiHandlerConfs.get(confList).add(draggingWorld);
		confList = new LinkedList<>(confList);
		confList.add(new UIEvent(EventType.MOUSE_BUTTON_UP, 0, null, 0));
		uiHandlerConfs.put(confList, new ArrayList<Runnable>());
		uiHandlerConfs.get(confList).add(draggedWorld);
	}

	/**
	 * draw a quad with the image on it
	 */
	public void render() {

		Vect3D focalPoint = globalModel.getViewport().getFocalPoint();
		GL11.glTranslatef(focalPoint.x, focalPoint.y, focalPoint.z);
		GL11.glRotatef(globalModel.getViewport().getRotation(), 0, 0, 1);

		// TODO draw world
		// RenderStyle renderStyle = RenderStyle.NORMAL;
		// if (WorldRenderer.debugDisplay) {
		// renderStyle = RenderStyle.DEBUG;
		// }
		// worldRenderer.render(GL11.GL_RENDER, renderStyle, globalModel);

		// TODO render the world
		new ShipRenderer().render(GL11.GL_RENDER, RenderingType.NORMAL, new Ship(0, 0, 0));

		GL11.glRotatef(-globalModel.getViewport().getRotation(), 0, 0, 1);
		GL11.glTranslatef(-focalPoint.x, -focalPoint.y, -focalPoint.z);

		// TODO Interface rendering
		// GL11.glTranslatef(WorldRenderer.focalPoint.x,
		// WorldRenderer.focalPoint.y, WorldRenderer.focalPoint.z);
		// interfaceRenderer.render(GL11.GL_RENDER, WorldRenderer.debugDisplay ?
		// RenderStyle.DEBUG : RenderStyle.NORMAL);
		// GL11.glTranslatef(-WorldRenderer.focalPoint.x,
		// -WorldRenderer.focalPoint.y, -WorldRenderer.focalPoint.z);

		// move world
		// NEEDED ?
		// globalModel.update();

		// udpate IAs
		// MOVE THIS SOMEWHERE ELSE
		// for (Ship ship : globalModel.getShipList()) {
		// List<IA> iasToRemove = new ArrayList<IA>();
		// for (IA ia : ship.getIAList()) {
		// if (ia != null) {
		// if (ia.done()) {
		// iasToRemove.add(ia);
		// } else {
		// ia.compute();
		// }
		// }
		// }
		// for (IA ia : iasToRemove) {
		// ship.getIAList().remove(ia);
		// }
		// iasToRemove.clear();
		// }
	}

	/**
	 * Start the application
	 */
	public void start() {
		initGL(Conf.getIntProperty("window.initialWidth"), Conf.getIntProperty("window.initialHeight"));
		// TODO Init renderers should be done BY renderers
		// MorphRenderer.init();

		// Initializes the world and its renderer
		// TODO do world init in the constructor
		// globalModel = World.getWorld();
		// globalModel.init();
		// worldRenderer = new WorldRenderer();
		// interfaceRenderer = new InterfaceRenderer();
		// interfaceRenderer.init();

		// Configure UI Handlers

		initUIHandlers();

		// Rendering loop
		while (true) {
			// Renders everything
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			render();

			// updates display and sets frame rate
			Display.update();
			Display.sync(100);

			// Get mouse position in world coordinates
			// Vect3D worldMousePos = new Vect3D(GameMouse.getXInWorld(),
			// GameMouse.getYInWorld(), 0);
			// Get mouse position in pixels on the display.
			// TODO is this necessary ?
			// Vect3D mousePos = new Vect3D(WorldMouse.getX(),
			// WorldMouse.getY(), 0);

			// To be able to render the scene while it's being dragged, we do
			// not change the focus point as long as the mouse button has not
			// been released

			if (Mouse.next()) {
				// add interaction to ui context
				EventType evtType = null;
				if (Mouse.getEventButton() >= 0) {
					if (Mouse.getEventButtonState()) {
						evtType = EventType.MOUSE_BUTTON_DOWN;
					} else {
						evtType = EventType.MOUSE_BUTTON_UP;
					}
					UIEvent evt = new UIEvent(evtType, Mouse.getEventButton(), new int[] { Mouse.getEventX(), Mouse.getEventY() });
					globalModel.getUIContext().getEventQueue().add(evt);
				}

				// First see if it triggered an event on a model element
				// TODO implement model elements event handling

				// // Then handle drag
				// // Drag is done with left button
				// if (Mouse.getEventButton() == 0) {
				// if (Mouse.getEventButtonState()) {
				// LOGGER.debug("mouse down");
				// // Handle drag if the mouse position delta has reached
				// // the threshold.
				// // TODO implement this
				//
				// oldFP = new Vect3D(GlobalModel.getModel().getViewport().getFocalPoint());
				// oldMousePosInWindow = new Vect3D(GameMouse.getX(), GameMouse.getY(), 0);
				//
				// } else {
				// oldFP = null;
				// oldMousePosInWindow = null;
				// LOGGER.debug("mouse up");
				// }
				// }
			}

			if (!globalModel.getUIContext().getEventQueue().isEmpty()) {
				List<Runnable> list = uiHandlerConfs.get(Collections.unmodifiableList(globalModel.getUIContext().getEventQueue()));
				if (list != null) {
					for (Runnable uiHandler : list) {
						uiHandler.run();
					}
				}
			}

			// If left button is down, we are currently dragging
			// FIXME we might be dragging for something else than moving the
			// scene around.
			// TODO Transform this into an action processing thing
			// if (Mouse.isButtonDown(0)) {
			//
			// // Update focus point
			// if (oldFP != null) {
			// ViewPort viewport = GlobalModel.getModel().getViewport();
			// Vect3D fp = viewport.getFocalPoint();
			// fp.x = oldFP.x + (Mouse.getX() - oldMousePosInWindow.x) / GlobalModel.getModel().getViewport().getZoomFactor();
			// fp.y = oldFP.y - (Mouse.getY() - oldMousePosInWindow.y) / GlobalModel.getModel().getViewport().getZoomFactor();
			// }
			//
			// }

			// Handling world moving around by drag and dropping the world.
			// This portion of code is meant to allow the engine to show the
			// world while it's being dragged.
			// TODO migrate this
			// if (holdWorldMousePos != null) {
			// if (Math.abs(holdWorldMousePos.x - MouseInWorld.getX()) >
			// MIN_MOVE_FOR_DRAG || Math.abs(holdWorldMousePos.y -
			// MouseInWorld.getY()) > MIN_MOVE_FOR_DRAG) {
			// WorldRenderer.focalPoint.add(holdWorldMousePos);
			// WorldRenderer.focalPoint.substract(worldMousePos);
			// GL11.glMatrixMode(GL11.GL_PROJECTION);
			// GL11.glLoadIdentity();
			// GLU.gluOrtho2D(WorldRenderer.focalPoint.x - WIDTH *
			// WorldRenderer.scale / 2,
			// WorldRenderer.focalPoint.x + WIDTH * WorldRenderer.scale / 2,
			// WorldRenderer.focalPoint.y + HEIGHT * WorldRenderer.scale / 2,
			// WorldRenderer.focalPoint.y - HEIGHT * WorldRenderer.scale / 2);
			// holdWorldMousePos.x = MouseInWorld.getX();
			// holdWorldMousePos.y = MouseInWorld.getY();
			//
			// }
			// }

			// If a mouse event has fired, Mouse.next() returns true.
			// TODO Migrate user interactions
			// if (Mouse.next()) {
			//
			// // Event button == 0 : Left button related event
			// if (Mouse.getEventButton() == 0) {
			// // if event button state is false, the button is being released
			// if (!Mouse.getEventButtonState()) {
			// if (Math.abs(holdWorldMousePos.x - MouseInWorld.getX()) >
			// MIN_MOVE_FOR_DRAG || Math.abs(holdWorldMousePos.y -
			// MouseInWorld.getY()) > MIN_MOVE_FOR_DRAG) {
			// WorldRenderer.focalPoint.add(holdWorldMousePos);
			// WorldRenderer.focalPoint.substract(worldMousePos);
			// GL11.glMatrixMode(GL11.GL_PROJECTION);
			// GL11.glLoadIdentity();
			// GLU.gluOrtho2D(WorldRenderer.focalPoint.x - WIDTH *
			// WorldRenderer.scale / 2,
			// WorldRenderer.focalPoint.x + WIDTH * WorldRenderer.scale / 2,
			// WorldRenderer.focalPoint.y + HEIGHT * WorldRenderer.scale / 2,
			// WorldRenderer.focalPoint.y - HEIGHT * WorldRenderer.scale / 2);
			// } else {
			// pick(MouseInWorld.getX(),MouseInWorld.getY());
			// }
			// holdWorldMousePos = null;
			// } else {
			// // the mouse left button is being pressed
			// holdWorldMousePos = worldMousePos;
			// }
			// }
			//
			// // Event button == 0 : Right button related event
			// if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState()
			// && globalModel.getSelectedShip() != null && !World.combat) {
			// // Right mouse button has been released and a ship is selected
			// // Activate or deactivate the morph under mouse pointer.
			// for (Morph morph :
			// globalModel.getSelectedShip().getSelectedMorphList()) {
			// if (morph.getShip().toggleActiveMorph(morph)) {
			// if (!morph.disabled) {
			// morph.activate();
			// }
			// } else {
			// morph.deactivate();
			// }
			// }
			//
			// // If no morph is selected, the right click should be understood
			// as a moveto order.
			// if
			// (globalModel.getSelectedShip().getSelectedMorphList().isEmpty())
			// {
			// List<IA> iaList = globalModel.getSelectedShip().getIAList();
			//
			// // Look for existing tracker
			// for (IA ia : iaList) {
			// if (ia instanceof FixedPositionTracker) {
			// ((FixedPositionTracker) ia).setTargetPos(worldMousePos);
			// }
			// }
			//
			// iaList.add(new
			// FixedPositionTracker(globalModel.getSelectedShip(),
			// worldMousePos));
			// }
			// }
			//
			// // Handling shoot
			// if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState()
			// && globalModel.getSelectedShip() != null && World.combat) {
			// globalModel.getSelectedShip().getIAList().add(new
			// WorldPositionFirer(globalModel.getSelectedShip(),
			// worldMousePos));
			// }
			//
			// // int dWheel = Mouse.getDWheel();
			// // if (dWheel != 0) {
			// // float scale = (float) (Math.pow(1 + Math.pow(4, -5 +
			// Math.abs(dWheel / 120)), Math.signum(dWheel)));
			// // GL11.glScalef(scale, scale, scale);
			// // }
			// }
			//
			// if (Keyboard.next()) {
			// toggleDebugAction.run();
			// toggleCombatMode.run();
			// toggleFreezeAction.run();
			// }

			if (Display.isCloseRequested()) {
				Display.destroy();
				System.exit(0);
			}
		}
	}
}