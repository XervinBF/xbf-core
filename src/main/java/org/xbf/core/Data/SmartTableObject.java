package org.xbf.core.Data;

import java.util.HashMap;

import org.xbf.core.Data.Annotations.Ignore;
import org.xbf.core.Data.Annotations.Include;
import org.xbf.core.Data.Annotations.Key;

public class SmartTableObject extends SmartTableObjectNoKey {

	public SmartTableObject(String table) {
		super(table);
	}
	
	public SmartTableObject(String table, HashMap<String, Class<? extends SmartTableArrayObject>> map) {
		super(table, map);
	}

	@Include
	@Key
	public int id;
	

	
	
	
}
