package net.carmgate.morph.ui;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * A mouse or keyboard event is any simple (non-composed) interaction with the mouse or keyboard.
 * For instance, a button down event.
 * TODO Several events might be merged in one. For instance, a button down/button up, without mouse movement might be a button click)
 * TODO We should split between the conf part (event type and button) and the execution part (mouse position and time of event).
 * TODO Some kind of model elements might be valid event members in terms of configuration.
 */
public class Event {

	public static enum EventType {
		MOUSE_BUTTON_DOWN(HardwareType.MOUSE),
		MOUSE_BUTTON_UP(HardwareType.MOUSE),
		MOUSE_MOVE(HardwareType.MOUSE),
		NOOP(HardwareType.NONE),
		KEYBOARD_DOWN(HardwareType.KEYBOARD),
		KEYBOARD_UP(HardwareType.KEYBOARD);

		private final Event.HardwareType hardwareType;

		private EventType(Event.HardwareType hardwareType) {
			this.hardwareType = hardwareType;

		}

		public Event.HardwareType getHardwareType() {
			return hardwareType;
		}
	}

	public static enum HardwareType {
		MOUSE, KEYBOARD, NONE;
	}

	protected final int[] mousePositionInWindow;
	protected final long timeOfEventInMillis;
	protected final Event.EventType eventType;
	protected final int button;

	public Event(Event.EventType eventType) {
		this(eventType, -1);
	}

	public Event(Event.EventType eventType, int button) {
		this(eventType, button, null);
	}

	public Event(Event.EventType eventType, int button, int[] mousePositionInWindow) {
		this(eventType, button, mousePositionInWindow, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
	}

	public Event(Event.EventType eventType, int button, int[] mousePositionInWindow, long timeOfEventInMillis) {
		this.mousePositionInWindow = mousePositionInWindow;
		this.timeOfEventInMillis = timeOfEventInMillis;
		this.eventType = eventType;
		this.button = button;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Event other = (Event) obj;
		if (button != other.button) {
			return false;
		}
		if (eventType != other.eventType) {
			return false;
		}
		return true;
	}

	/**
	 * @return the id of the button (0 == LEFT)
	 */
	public int getButton() {
		return button;
	}

	/**
	 * @return the event type (button down, button up, mouse wheel, etc.)
	 */
	public Event.EventType getEventType() {
		return eventType;
	}

	/**
	 * @return the position of the mouse in the window coordinate system ({x,
	 *         y}).
	 */
	public int[] getPositionInWindow() {
		return mousePositionInWindow;
	}

	/**
	 * @return the timestamp of the date/time when the event was created.
	 */
	public long getTimeOfEventInMillis() {
		return timeOfEventInMillis;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + button;
		result = prime * result + (eventType == null ? 0 : eventType.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "eventType: " + eventType + ", button: " + button;
	}

}