package org.xbf.core.Utils.Map;

import java.util.HashMap;

public class MapUtils<K, T> {

	/**
	 * Merges two maps by inserting the lowPriorityMap first and then the highPriority map
	 * @param lowPriority First to be inserted
	 * @param highPriority Inserted last
	 * @return The merged map
	 */
	public HashMap<K, T> mergeMap(HashMap<K,T> lowPriority, HashMap<K,T> highPriority) {
		HashMap<K, T> map = new HashMap<>();
		for (K k : lowPriority.keySet()) {
			if(!map.containsKey(k))
				map.put(k, lowPriority.get(k));
		}
		
		for (K k : highPriority.keySet()) {
			if(map.containsKey(k))
				map.remove(k);
			map.put(k, highPriority.get(k));
		}
		return map;
	}
	
}
