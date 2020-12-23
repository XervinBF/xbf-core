package org.xbf.core.Data;

import java.util.HashMap;

import org.xbf.core.Data.Annotations.Ignore;

public class SmartTableObjectNoKey {

	@Ignore
	public Object smrtref;
	@Ignore
	public String smrttable;
	@Ignore
	public HashMap<String, Class<? extends SmartTableArrayObject>> smrtmappings;
	public SmartTableObjectNoKey(String table) {
		this(table, null);
	}
	
	public SmartTableObjectNoKey(String table, HashMap<String, Class<? extends SmartTableArrayObject>> map) {
		this.smrttable = table;
		this.smrtmappings = map;
	}

	public String getTable() {
		return smrttable;
	}
	
	public HashMap<String, Class<? extends SmartTableArrayObject>> getArrayMappings() {
		return smrtmappings;
	}
	
	public void objectLoaded() {
		
	}
	
}
