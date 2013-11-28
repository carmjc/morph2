package net.carmgate.morph.actions.common;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.carmgate.morph.actions.common.UIEvent.EventType;

public class InteractionStack {
	private static final int STACK_SIZE = 10;

	private final Deque<UIEvent> stack = new LinkedList<>();

	/**
	 * Default constructor.
	 * Fills the stack with NOOP, so that the stack is always full,
	 * even at start.
	 */
	public InteractionStack() {
		for (int i = 0; i < STACK_SIZE; i++) {
			addEvent(new UIEvent(EventType.NOOP));
		}
	}

	public void addEvent(UIEvent event) {
		stack.addFirst(event);
		if (stack.size() > STACK_SIZE) {
			stack.removeLast();
		}
	}

	public UIEvent getLastEvent() {
		return stack.getFirst();
	}

	public List<UIEvent> getLastEvents(int n) {
		List<UIEvent> result = new ArrayList<>();
		int i = 0;
		for (UIEvent event : stack) {
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
