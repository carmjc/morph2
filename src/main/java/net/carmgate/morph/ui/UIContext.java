package net.carmgate.morph.ui;

import java.util.LinkedList;

public class UIContext {

	private final LinkedList<UIEvent> eventQueue = new LinkedList<>();

	public LinkedList<UIEvent> getEventQueue() {
		return eventQueue;
	}

}
