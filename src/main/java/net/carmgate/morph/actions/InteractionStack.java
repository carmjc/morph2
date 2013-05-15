package net.carmgate.morph.actions;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import net.carmgate.morph.ui.Event;

public class InteractionStack {
	private static final int STACK_SIZE = 10;

	private final Deque<Event> stack = new LinkedList<>();

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
