package org.xbf.core.Models.Permissions;

import java.util.ArrayList;

import org.xbf.core.Data.SmartTable;
import org.xbf.core.Data.SmartTableArrayObject;
import org.xbf.core.Data.SmartTableObject;
import org.xbf.core.Data.Annotations.IncludeAll;
import org.xbf.core.Utils.Map.FastMap;

@IncludeAll
public class PGroup extends SmartTableObject {

	
	public PGroup() {
		super("PGroups", new FastMap<String, Class<? extends SmartTableArrayObject>>().add("permissions", PGroupPermission.class).getMap());
	}
	
	public String gId;
	public String displayName;
	public ArrayList<PGroupPermission> permissions = new ArrayList<>();;
	
	public static SmartTable<PGroup> getSmartTable() {
		return new SmartTable<PGroup>("PGroups", PGroup.class);
	}

}
