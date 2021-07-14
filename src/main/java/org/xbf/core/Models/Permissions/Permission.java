package org.xbf.core.Models.Permissions;

import org.xbf.core.DBConfig;
import org.xbf.core.XBF;
import org.xbf.core.Data.SmartTable;
import org.xbf.core.Data.SmartTableObjectNoKey;
import org.xbf.core.Data.Annotations.Include;
import org.xbf.core.Data.Annotations.Key;
import org.xbf.core.Utils.Map.FastMap;

public class Permission extends SmartTableObjectNoKey{

	public Permission() {
		super("permissions");
	}
	
	@Include
	@Key
	public String perm;
	
	@Include
	public String editPermission;
	
	@Include
	public String name;
	
	@Include
	public String description;
	
	public static SmartTable<Permission> getSmartTable() {
		return new SmartTable<Permission>("permissions", Permission.class);
	}
	
	public static void validatePermissionExists(String perm) {
		Permission dbPerm = Permission.getSmartTable().get(new FastMap<String, String>().add("perm", perm));
		if(dbPerm != null) return;
		XBF.tryRegisterPermission(perm);
	}
	
	public static void registerPermissionIfNotFound(String perm, String name, String description) {
		registerPermissionIfNotFound(perm, name, description, "xbf.permissions.edit.all");
	}
	
	public static void registerPermissionIfNotFound(String perm, String name, String description, String editPermission) {
		Permission dbPerm = Permission.getSmartTable().get(new FastMap<String, String>().add("perm", perm));
		if(dbPerm != null) return;
		dbPerm = new Permission();
		dbPerm.perm = perm;
		dbPerm.editPermission = editPermission;
		dbPerm.name = name;
		dbPerm.description = description;
		Permission.getSmartTable().set(dbPerm);
	}
	
	
}
