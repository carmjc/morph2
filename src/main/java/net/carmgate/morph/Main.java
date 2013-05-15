package net.carmgate.morph;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.carmgate.morph.actions.Action;
import net.carmgate.morph.actions.Select;
import net.carmgate.morph.actions.drag.DragContext;
import net.carmgate.morph.actions.drag.DraggedWorld;
import net.carmgate.morph.actions.drag.DraggingWorld;
import net.carmgate.morph.conf.Conf;
import net.carmgate.morph.model.Model;
import net.carmgate.morph.model.common.Vect3D;
import net.carmgate.morph.model.entities.Entity;
import net.carmgate.morph.model.entities.Ship;
import net.carmgate.morph.model.view.ViewPort;
import net.carmgate.morph.ui.Event;
import net.carmgate.morph.ui.Event.EventType;
import net.carmgate.morph.ui.Renderable;
import net.carmgate.morph.ui.Renderable.RenderingType;

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

	/** This is used to retrieve picked entities. */
	private final Map<Integer, Class<?>> entitiesMap = new HashMap<>();

	private final List<Action> mouseActions = new LinkedList<>();

	/**
	 * This method initializes UI handlers.
	 * Some special case handlers can not be initialized dynamically at the moment.
	 */
	private void initActions() {
		DragContext dragContext = new DragContext();
		mouseActions.add(new DraggingWorld(dragContext));
		mouseActions.add(new Select());
		mouseActions.add(new DraggedWorld(dragContext));
	}

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
		ship = new Ship(50, 0, 0, 40);
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
		Set<Class<? extends Renderable>> renderers = reflections.getSubTypesOf(Renderable.class);

		// Iterate over the result set to register the renderers with the model classes
		for (Class<? extends Renderable> renderer : renderers) {
			try {
				renderer.newInstance().initRenderer();
			} catch (InstantiationException | IllegalAccessException e) {
				LOGGER.error("Exception raised while trying to init renderer " + renderer.getName(), e);
			}
		}
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

		// Configure Actions
		initActions();

		// Rendering loop
		while (true) {
			// Renders everything
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			render();

			// updates display and sets frame rate
			Display.update();
			Display.sync(100);

			// Fire events accordingly
			if (Mouse.next()) {
				// add interaction to ui context
				EventType evtType = null;
				if (Mouse.getEventButton() >= 0) {
					if (Mouse.getEventButtonState()) {
						evtType = EventType.MOUSE_BUTTON_DOWN;
					} else {
						evtType = EventType.MOUSE_BUTTON_UP;
					}
					Event event = new Event(evtType, Mouse.getEventButton(), new int[] { Mouse.getEventX(), Mouse.getEventY() });
					Model.getModel().getInteractionStack().addEvent(event);
					for (Action action : mouseActions) {
						action.run();
					}
				}
			}

			int dx = Mouse.getDX();
			int dy = Mouse.getDY();
			if (dx != 0 || dy != 0) {
				for (Action action : mouseActions) {
					Event event = new Event(EventType.MOUSE_MOVE, Mouse.getEventButton(), new int[] { Mouse.getX(), Mouse.getY() });
					if (Model.getModel().getInteractionStack().getLastEvent().getEventType() != EventType.MOUSE_MOVE) {
						Model.getModel().getInteractionStack().addEvent(event);
					}
					action.run();
				}
			}

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