package net.carmgate.morph.model;

import java.util.Collection;
import java.util.HashMap;

import net.carmgate.morph.model.entities.common.Entity;

public class EntityMap {
	private final HashMap<Integer, Entity> backingMap = new HashMap<>();

	public Entity get(Object key) {
		return backingMap.get(key);
	}

	protected Entity put(Integer key, Entity value) {
		return backingMap.put(key, value);
	}

	public Entity remove(Object arg0) {
		return backingMap.remove(arg0);
	}

	public Collection<Entity> values() {
		return backingMap.values();
	}

}
