package org.xbf.core.Data;

import java.util.HashMap;

import org.xbf.core.Data.Annotations.Ignore;
import org.xbf.core.Data.Annotations.Include;

public class SmartTableObject {

	@Include
	public int id;
	
	@Ignore
	public String smrttable;
	@Ignore
	public HashMap<String, Class<? extends SmartTableArrayObject>> smrtmappings;
	public SmartTableObject(String table) {
		this(table, null);
	}
	
	public SmartTableObject(String table, HashMap<String, Class<? extends SmartTableArrayObject>> map) {
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
