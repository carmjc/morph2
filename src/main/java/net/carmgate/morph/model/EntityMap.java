package net.carmgate.morph.model;

import java.util.Collection;
import java.util.HashMap;

import net.carmgate.morph.ui.Renderable;
import net.carmgate.morph.ui.Selectable;

public class EntityMap<T extends Selectable & Renderable> {
	private final HashMap<Integer, T> backingMap = new HashMap<>();

	public T get(Object key) {
		return backingMap.get(key);
	}

	protected T put(Integer key, T value) {
		return backingMap.put(key, value);
	}

	public Collection<T> values() {
		return backingMap.values();
	}

}
