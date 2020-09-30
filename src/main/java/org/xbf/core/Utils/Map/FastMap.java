package org.xbf.core.Utils.Map;

import java.util.HashMap;

/**
 * A map building util
 * @author elias
 *
 * @param <A> The Key type
 * @param <B> The value type
 */
public class FastMap<A, B> {

	HashMap<A,B> map = new HashMap<A, B>();
	
	public FastMap<A,B> add(A key, B value) {
		map.put(key, value);
		return this;
	}
	
	public HashMap<A, B> getMap() {
		return map;
	}
	
}
