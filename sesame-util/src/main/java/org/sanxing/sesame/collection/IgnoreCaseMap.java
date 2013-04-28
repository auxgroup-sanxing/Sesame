package org.sanxing.sesame.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class IgnoreCaseMap<V> implements Map<String, V> {
	private Map<String, V> delegate = null;

	public IgnoreCaseMap() {
		this.delegate = new HashMap();
	}

	public IgnoreCaseMap(int initialCapacity) {
		this.delegate = new HashMap(initialCapacity);
	}

	public IgnoreCaseMap(int initialCapacity, float loadFactor) {
		this.delegate = new HashMap(initialCapacity, loadFactor);
	}

	private String convertKey(String key) {
		return key.toUpperCase();
	}

	public void clear() {
		this.delegate.clear();
	}

	public boolean containsKey(Object key) {
		return this.delegate.containsKey(convertKey(key.toString()));
	}

	public boolean containsValue(Object value) {
		return this.delegate.containsValue(value);
	}

	public Set<Map.Entry<String, V>> entrySet() {
		return this.delegate.entrySet();
	}

	public boolean equals(Object o) {
		return this.delegate.equals(o);
	}

	public V get(Object key) {
		return this.delegate.get(convertKey(key.toString()));
	}

	public int hashCode() {
		return this.delegate.hashCode();
	}

	public boolean isEmpty() {
		return this.delegate.isEmpty();
	}

	public Set<String> keySet() {
		return this.delegate.keySet();
	}

	public V put(String key, V value) {
		return this.delegate.put(convertKey(key.toString()), value);
	}

	public void putAll(Map<? extends String, ? extends V> map) {
		for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
			Map.Entry e = (Map.Entry) iter.next();
			put((String) e.getKey(), (V) e.getValue());
		}
	}

	public V remove(Object key) {
		return this.delegate.remove(convertKey(key.toString()));
	}

	public int size() {
		return this.delegate.size();
	}

	public Collection<V> values() {
		return this.delegate.values();
	}
}