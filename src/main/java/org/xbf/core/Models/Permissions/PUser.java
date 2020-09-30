package org.xbf.core.Models.Permissions;

import java.util.ArrayList;

import org.xbf.core.Data.SmartTable;
import org.xbf.core.Data.SmartTableArrayObject;
import org.xbf.core.Data.SmartTableObject;
import org.xbf.core.Data.Annotations.IncludeAll;
import org.xbf.core.Utils.Map.FastMap;

@IncludeAll
public class PUser extends SmartTableObject {

	public PUser() {
		super("PUsers", new FastMap<String, Class<? extends SmartTableArrayObject>>().add("permissions", PUserPermission.class).getMap());
	}
	
	public String uid;
	public ArrayList<PUserPermission> permissions = new ArrayList<PUserPermission>();
	
	public static SmartTable<PUser> getSmartTable() {
		return new SmartTable<PUser>("PUsers", PUser.class);
	}
	
}
