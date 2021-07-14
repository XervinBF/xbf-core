package org.xbf.core.Plugins;

import java.util.HashMap;

public class PluginPermissionFile {

	public HashMap<String, PluginDefinedPermission> permissions;
	
	public static class PluginDefinedPermission {
		
		public String name;
		public String description;
		public String editPermission;
		
	}
	
}
