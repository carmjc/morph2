package net.carmgate.morph.actions.common;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.carmgate.morph.actions.common.Event.EventType;

public class InteractionStack {
	private static final int STACK_SIZE = 10;

	private final Deque<Event> stack = new LinkedList<>();

	/**
	 * Default constructor.
	 * Fills the stack with NOOP, so that the stack is always full,
	 * even at start.
	 */
	public InteractionStack() {
		for (int i = 0; i < STACK_SIZE; i++) {
			addEvent(new Event(EventType.NOOP));
		}
	}

	public void addEvent(Event event) {
		stack.addFirst(event);
		if (stack.size() > STACK_SIZE) {
			stack.removeLast();
		}
	}

	public Event getLastEvent() {
		return stack.getFirst();
	}

	public List<Event> getLastEvents(int n) {
		List<Event> result = new ArrayList<>();
		int i = 0;
		for (Event event : stack) {
			if (i++ >= n) {
				break;
			}
			result.add(event);
		}
		return result;
	}

	public int size() {
		return stack.size();
	}
}
