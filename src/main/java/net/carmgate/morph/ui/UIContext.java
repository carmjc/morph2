package net.carmgate.morph.ui;

import java.util.Calendar;
import java.util.Deque;
import java.util.LinkedList;
import java.util.TimeZone;


public class UIContext {

	/**
	 * A mouse event is any simple (non-composed) interaction with the mouse.
	 * For instance, a button down event.
	 * TODO Several events might be merged in one. For instance, a button down/button up, without mouse movement might be a button click)
	 */
	public static class MouseEvent extends UIEvent {
		public static enum EventType {
			BUTTON_DOWN,
			BUTTON_UP;
		}

		private final EventType eventType;
		private final int button;
		
		public MouseEvent(EventType eventType) {
			this(eventType, -1);
		}

		public MouseEvent(EventType eventType, int button) {
			this(eventType, button, null);
		}

		public MouseEvent(EventType eventType, int button, int[] mousePositionInWindow) {
			this(eventType, button, mousePositionInWindow, Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis());
		}

		public MouseEvent(EventType eventType, int button, int[] mousePositionInWindow, long timeOfEventInMillis) {
			super(mousePositionInWindow, timeOfEventInMillis);
			this.eventType = eventType;
			this.button = button;
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
		public EventType getEventType() {
			return eventType;
		}
	}

	public static class UIEvent {

		protected final int[] positionInWindow;
		protected final long timeOfEventInMillis;

		public UIEvent(int[] positionInWindow, long timeOfEventInMillis) {
			this.positionInWindow = positionInWindow;
			this.timeOfEventInMillis = timeOfEventInMillis;
			
		}
		
		/**
		 * @return the position of the mouse in the window coordinate system ({x, y}).
		 */
		public int[] getPositionInWindow() {
			return positionInWindow;
		}

		/**
		 * @return the timestamp of the date/time when the event was created.
		 */
		public long getTimeOfEventInMillis() {
			return timeOfEventInMillis;
		}
	}

	private final Deque<UIEvent> eventQueue = new LinkedList<>();

	public Deque<UIEvent> getEventQueue() {
		return eventQueue;
	}

}
