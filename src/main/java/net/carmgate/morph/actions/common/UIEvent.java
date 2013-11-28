package net.carmgate.morph.actions.common;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * A mouse or keyboard event is any simple (non-composed) interaction with the mouse or keyboard.
 * For instance, a button down event.
 */
public class UIEvent {

	public static enum EventType {
		MOUSE_BUTTON_DOWN(HardwareType.MOUSE),
		MOUSE_BUTTON_UP(HardwareType.MOUSE),
		MOUSE_MOVE(HardwareType.MOUSE),
		MOUSE_WHEEL(HardwareType.MOUSE),
		NOOP(HardwareType.NONE),
		KEYBOARD_DOWN(HardwareType.KEYBOARD),
		KEYBOARD_UP(HardwareType.KEYBOARD);

		private final UIEvent.HardwareType hardwareType;

		private EventType(UIEvent.HardwareType hardwareType) {
			this.hardwareType = hardwareType;

		}

		public UIEvent.HardwareType getHardwareType() {
			return hardwareType;
		}
	}

	public static enum HardwareType {
		MOUSE, KEYBOARD, NONE;
	}

	protected final int[] mousePositionInWindow;
	protected final long timeOfEventInMillis;
	protected final UIEvent.EventType eventType;
	protected final int button;

	public UIEvent(UIEvent.EventType eventType) {
		this(eventType, -1);
	}

	public UIEvent(UIEvent.EventType eventType, int button) {
		this(eventType, button, null);
	}

	public UIEvent(UIEvent.EventType eventType, int button, int[] mousePositionInWindow) {
		this(eventType, button, mousePositionInWindow, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
	}

	public UIEvent(UIEvent.EventType eventType, int button, int[] mousePositionInWindow, long timeOfEventInMillis) {
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
		UIEvent other = (UIEvent) obj;
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
	public UIEvent.EventType getEventType() {
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