package net.carmgate.morph.ui;

import java.util.LinkedList;

public class Context {

	private final LinkedList<Event> eventQueue = new LinkedList<>();

	public LinkedList<Event> getEventQueue() {
		return eventQueue;
	}

}
