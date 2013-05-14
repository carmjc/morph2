package net.carmgate.morph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Entity;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.ui.Rendererable;
import net.carmgate.morph.ui.Rendererable.RenderingType;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;
import net.carmgate.morph.uihandler.Action;
import net.carmgate.morph.uihandler.Select;
import net.carmgate.morph.uihandler.drag.DragContext;
import net.carmgate.morph.uihandler.drag.DraggedWorld;
import net.carmgate.morph.uihandler.drag.DraggingWorld;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.reflections.Reflections;
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

	private final Model model = Model.getModel();

	private final Map<List<Event>, List<Action>> uiHandlerConfs = new HashMap<>();
	/** This is used to retrieve picked entities. */
	private final Map<Integer, Class<?>> entitiesMap = new HashMap<>();

	/**
	* Scan for entities and registers them in the entitiesMap.
	* that allows for fast retrieval of the Class<?> of the entity with its uniqueId.
	*/
	private void initEntities() {
		// Init the reflection API
		Reflections reflections = new Reflections("net.carmgate.morph");

		// Look for classes annotated with the @Entity annotation
		Set<Class<?>> entities = reflections.getTypesAnnotatedWith(Entity.class);

		for (Class<?> entity : entities) {
			entitiesMap.put(entity.getAnnotation(Entity.class).uniqueId(), entity);
		}
	}

	/**
	 * Initialise the GL display
	 * 
	 * @param width The width of the display
	 * @param height The height of the display
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
		ViewPort viewport = model.getViewport();
		Vect3D focalPoint = viewport.getFocalPoint();

		// TODO : test the zoom factor
		GLU.gluOrtho2D(focalPoint.x - width / viewport.getZoomFactor() / 2, focalPoint.x + width / viewport.getZoomFactor() / 2, focalPoint.y + height
				/ viewport.getZoomFactor() / 2, focalPoint.y - height / viewport.getZoomFactor() / 2);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	private void initModel() {
		Ship ship = new Ship(0, 0, 0, 10);
		Map<Integer, Object> shipsMap = new HashMap<>();
		Model.getModel().getEntities().put(Ship.class.getAnnotation(Entity.class).uniqueId(), shipsMap);
		shipsMap.put(ship.getId(), ship);
		ship = new Ship(100, 0, 0, 40);
		shipsMap.put(ship.getId(), ship);
	}

	/**
	 * Scans the classpath looking for renderers (classes annotated with @{@link Renders})
	 * initializes them and add them to the maps of the renderers
	 */
	private void initRenderers() {
		// Init the reflection API
		Reflections reflections = new Reflections("net.carmgate.morph");

		// Look for classes annotated with the @Renders annotation
		Set<Class<? extends Rendererable>> renderers = reflections.getSubTypesOf(Rendererable.class);

		// Iterate over the result set to register the renderers with the model classes
		for (Class<? extends Rendererable> renderer : renderers) {
			try {
				renderer.newInstance().initRenderer();
			} catch (InstantiationException | IllegalAccessException e) {
				LOGGER.error("Exception raised while trying to init renderer " + renderer.getName(), e);
			}
		}
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
		LinkedList<Event> confList = new LinkedList<>();
		confList.add(new Event(EventType.MOUSE_BUTTON_DOWN, 0, null, 0));
		uiHandlerConfs.put(confList, new ArrayList<Action>());
		uiHandlerConfs.get(confList).add(draggingWorld);
		confList = new LinkedList<>(confList);
		confList.add(new Event(EventType.MOUSE_BUTTON_UP, 0, null, 0));
		uiHandlerConfs.put(confList, new ArrayList<Action>());
		uiHandlerConfs.get(confList).add(draggedWorld);
		uiHandlerConfs.get(confList).add(new Select());
	}

	/**
	 * draw a quad with the image on it
	 */
	public void render() {

		Vect3D focalPoint = model.getViewport().getFocalPoint();
		GL11.glTranslatef(focalPoint.x, focalPoint.y, focalPoint.z);
		GL11.glRotatef(model.getViewport().getRotation(), 0, 0, 1);

		// TODO draw world
		// RenderStyle renderStyle = RenderStyle.NORMAL;
		// if (WorldRenderer.debugDisplay) {
		// renderStyle = RenderStyle.DEBUG;
		// }
		// worldRenderer.render(GL11.GL_RENDER, renderStyle, globalModel);

		// TODO render the world
		Map<Integer, Ship> shipsMap = Model.getModel().getEntityMap(Ship.class.getAnnotation(Entity.class).uniqueId());
		for (Ship ship : shipsMap.values()) {
			ship.render(GL11.GL_RENDER, RenderingType.NORMAL);
		}

		GL11.glRotatef(-model.getViewport().getRotation(), 0, 0, 1);
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
		// init OpenGL context
		initGL(Conf.getIntProperty("window.initialWidth"), Conf.getIntProperty("window.initialHeight"));

		// scan for entities
		initEntities();
		// scan for renderers
		initRenderers();
		// init world model
		initModel();

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
					Event evt = new Event(evtType, Mouse.getEventButton(), new int[] { Mouse.getEventX(), Mouse.getEventY() });
					model.getUIContext().getEventQueue().add(evt);
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

			if (!model.getUIContext().getEventQueue().isEmpty()) {
				List<Action> list = uiHandlerConfs.get(model.getUIContext().getEventQueue());
				if (list != null) {
					for (Action uiHandler : list) {
						uiHandler.run(null);
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