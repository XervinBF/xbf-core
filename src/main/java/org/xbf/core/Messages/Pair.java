package org.xbf.core.Messages;

/**
 * A Key value pair
 * @author BL19
 *
 * @param <T> Any Object
 * @param <K> Any Object
 */
public class Pair<T,K> {

	public Pair(T tv, K kv) {
		key = tv;
		value = kv;
	}
	
	T key;
	K value;
	
	public T getKey() {
		return key;
	}
	
	public K getValue() {
		return value;
	}
	
	public void setKey(T key) {
		this.key = key;
	}
	
	public void setValue(K value) {
		this.value = value;
	}
	
}
