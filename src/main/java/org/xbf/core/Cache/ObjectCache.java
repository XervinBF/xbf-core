package org.xbf.core.Cache;

import java.util.ArrayList;
import java.util.HashMap;

public class ObjectCache {

	public static HashMap<String, HashMap<String, Object>> cache = new HashMap<String, HashMap<String,Object>>();
	public static HashMap<String, HashMap<String, Long>> cacheTime = new HashMap<String, HashMap<String,Long>>();

	
	public String cacheName;
	public long cacheKeep = 120000;
	
	/**
	 * Accesses a cache
	 * Keeps values for 2 min
	 * @param name The name of the cache
	 */
	public ObjectCache(String name) {
		this(name, 120000);
	}
	
	/**
	 * Accesses a cache
	 * @param name The name of the cache
	 * @param time The time to keep the values in the cache in milliseconds
	 */
	public ObjectCache(String name, long time) {
		cacheName = name;
		if(!cache.containsKey(name))
			cache.put(name, new HashMap<String, Object>());
		if(!cacheTime.containsKey(name))
			cacheTime.put(name, new HashMap<String, Long>());
		cacheKeep = time;
	}
	
	public void set(String key, Object value) {
		cacheCheck();
		if(cache.get(cacheName).containsKey(key)) {
			cache.get(cacheName).remove(key);
		}
		if(value != null)
			cache.get(cacheName).put(key, value);
		if(cacheTime.get(cacheName).containsKey(key)) {
			cacheTime.get(cacheName).remove(key);
		}
		if(value != null)
			cacheTime.get(cacheName).put(key, System.currentTimeMillis());
	}
	
	public Object get(String key) {
		cacheCheck();
		if(!cache.get(cacheName).containsKey(key)) {
			return null;
		}
		return cache.get(cacheName).get(key);
	}
	
	private void cacheCheck() {
		if(!cache.containsKey(cacheName))
			cache.put(cacheName, new HashMap<String, Object>());
		if(!cacheTime.containsKey(cacheName))
			cacheTime.put(cacheName, new HashMap<String, Long>());
		ArrayList<String> remove = new ArrayList<String>();
		for (String key : cacheTime.get(cacheName).keySet()) {
			long time = cacheTime.get(cacheName).get(key);
			long diff = System.currentTimeMillis() - time;
			if(diff > cacheKeep) {
//				System.out.println("'" + key + "' age '" + diff + "' tagged for removal");
				remove.add(key);
			}
		}
		for (String string : remove) {
			cacheTime.get(cacheName).remove(string);
			cache.get(cacheName).remove(string);
		}
	}
	
	public HashMap<String, Object> getCache() {
		return cache.get(cacheName);
	}
	
	public void clear() {
		if(cache.containsKey(cacheName))
			cache.remove(cacheName);
		if(cacheTime.containsKey(cacheName))
			cacheTime.remove(cacheName);
		if(!cache.containsKey(cacheName))
			cache.put(cacheName, new HashMap<String, Object>());
		if(!cacheTime.containsKey(cacheName))
			cacheTime.put(cacheName, new HashMap<String, Long>());
	}
	
}
