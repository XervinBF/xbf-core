package org.xbf.core.Models.Permissions;

import java.util.HashMap;

import org.xbf.core.Data.SmartTableArrayObject;
import org.xbf.core.Data.Annotations.IncludeAll;
import org.xbf.core.Utils.Map.FastMap;

@IncludeAll
public class PUserPermission extends SmartTableArrayObject {

	public PUserPermission() {
		super("PUserPermissions");
	}
	public String keyIndex;
	public boolean value;
	
	@Override
	public HashMap<String, String> getQuery() {
		return new FastMap<String, String>()
				.add("keyIndex", keyIndex)
				.getMap();
	}
	
	
}
