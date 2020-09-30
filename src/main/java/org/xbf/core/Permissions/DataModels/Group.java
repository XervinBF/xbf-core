package org.xbf.core.Permissions.DataModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xbf.core.Models.Permissions.PGroup;
import org.xbf.core.Models.Permissions.PGroupPermission;
import org.xbf.core.Permissions.PermissionRegistry;
import org.xbf.core.Utils.Map.MapUtils;

/**
 * Wrapper for PGroup
 * @author elias
 *
 */
public class Group {

	public PGroup g;
	public String id;
	public String displayName;
	
	public Group() {
	}
	
	public Group(PGroup p) {
		if(p != null) {
			displayName = p.displayName;
			this.id = p.gId;
			g = p;
		}
	}
	
	public Group setPermissions(List<Permission> permissions) {
		ArrayList<PGroupPermission> p = new ArrayList<>();
		for (Permission pr : permissions) {
			PGroupPermission prm = new PGroupPermission();
			prm.keyIndex = pr.key;
			prm.value = pr.value;
			p.add(prm);
		}
		g.permissions = p;
		PGroup.getSmartTable().set(g);
		return this;
	}
	
	public Group addPermission(String permission, boolean state) {
		g.permissions.add(new PGroupPermission() {{
			keyIndex = permission;
			value = state;
		}});
		PGroup.getSmartTable().set(g);
		return this;
	}
	
	public ArrayList<Permission> getAsPermissions() {
		ArrayList<Permission> pr = new ArrayList<Permission>();
		for (PGroupPermission p : g.permissions) {
			pr.add(new Permission(p.keyIndex, p.value));
		}
		return pr;
	}
	
	public HashMap<String, Boolean> getPermissionMap() {
		HashMap<String, Boolean> map = new HashMap<>();
		for (PGroupPermission pu : g.permissions) {
			if(!pu.keyIndex.startsWith("group.")) continue;
				map = new MapUtils<String, Boolean>().mergeMap(PermissionRegistry.getGroup(pu.keyIndex.replace("group.", "")).getPermissionMap(), map);
		}
		for (PGroupPermission pu : g.permissions) {
			if(pu.keyIndex.startsWith("group.")) continue;
			if(!map.containsKey(pu.keyIndex))
				map.put(pu.keyIndex, pu.value);
		}
		return map;
	}
	
	public ArrayList<String> getFullPermissionList() {
		ArrayList<String> perms = new ArrayList<String>();
		for (PGroupPermission gp : g.permissions) {
			Permission p = new Permission(gp.keyIndex, gp.value);
			if(!p.value) continue;
			if(p.type.equals("permission")) {
				perms.add(p.key);
			} else if(p.type.equals("inheritance")) {
				for (String string : PermissionRegistry.getGroup(p.key.replace("group.", "")).getFullPermissionList()) {
					perms.add(string);
				}
			}
		}
		return perms;
	}

	

}
