package org.xbf.core.Data;

import java.util.HashMap;

import org.xbf.core.Data.Annotations.Include;

public class SmartTableArrayObject extends SmartTableObject {

	public SmartTableArrayObject(String table) {
		super(table);
	}
	
	@Include
	public Object parent;
	
	public HashMap<String, String> getQuery() {
		return null;
	}
	
}
