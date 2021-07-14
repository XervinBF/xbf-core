package org.xbf.core.Permissions.DataModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xbf.core.XBF;
import org.xbf.core.Cache.ObjectCache;
import org.xbf.core.Models.UserProvider;
import org.xbf.core.Models.XUser;
import org.xbf.core.Models.Permissions.PUser;
import org.xbf.core.Models.Permissions.PUserPermission;
import org.xbf.core.Permissions.PermissionRegistry;
import org.xbf.core.Plugins.Handler;
import org.xbf.core.Utils.Map.MapUtils;
import org.xbf.core.Utils.Timings.Stopwatch;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class PermissionUser {

	public PUser p;
	public String id;
	public String displayName;
	public static ObjectCache c = new ObjectCache("PermCache", XBF.getConfig().cacheDurations.permissionFlatCacheDuration);
	public static ObjectCache cMap = new ObjectCache("PermCacheMap", XBF.getConfig().cacheDurations.permissionMapCacheDuration);
	
	public PermissionUser(PUser usr) {
		id = usr.uid;
		p = usr;
		displayName = new XUser(Integer.parseInt(id)).getName("*");
	}

	public ArrayList<Permission> getAsPermissions() {
		ArrayList<Permission> pr = new ArrayList<Permission>();
		for (PUserPermission p : p.permissions) {
			pr.add(new Permission(p.keyIndex, p.value));
		}
		return pr;
	}
	
	public PermissionUser setPermissions(List<Permission> permissions) {
		System.out.println(new Gson().toJson(permissions));
		ArrayList<PUserPermission> p = new ArrayList<>();
		for (Permission pr : permissions) {
			PUserPermission prm = new PUserPermission();
			if(pr.key.startsWith("displayname")) continue;
			prm.keyIndex = pr.key;
			prm.value = pr.value;
			p.add(prm);
		}
		this.p.permissions = p;	
		c.set(id, null);
		cMap.set(id, null);
		PUser.getSmartTable().set(this.p);
		return this;
	}
	
	public HashMap<String, Boolean> getPermissionMap() {
		String ch = (String) cMap.get(id);
		if(ch != null) {
			return new Gson().fromJson(ch, new TypeToken<HashMap<String, Boolean>>() {
			}.getType());
		}
		HashMap<String, Boolean> map = new HashMap<>();
		for (PUserPermission pu : p.permissions) {
			if(!pu.keyIndex.startsWith("group.")) continue;
				map = new MapUtils<String, Boolean>().mergeMap(PermissionRegistry.getGroup(pu.keyIndex.replace("group.", "")).getPermissionMap(), map);
		}
		for (Handler handler : XBF.getHandlers()) {
			try {
				for (String group : handler.getProviderGroups(Integer.parseInt(id))) {
					map = new MapUtils<String, Boolean>().mergeMap(PermissionRegistry.getGroup(group).getPermissionMap(), map);
				}
			} catch (Exception e) {
			}
		}
		for (PUserPermission pu : p.permissions) {
			if(pu.keyIndex.startsWith("group.")) continue;
			if(!map.containsKey(pu.keyIndex))
				map.put(pu.keyIndex, pu.value);
		}
		cMap.set(id, new Gson().toJson(map));
		return map;
	}
	
	public synchronized ArrayList<String> getFullPermissionList() {
		String ch = (String) c.get(id);
		if(ch != null)
			return new Gson().fromJson(ch, new TypeToken<ArrayList<String>>() {
			}.getType());
		Stopwatch sw = new Stopwatch();
		sw.start("LPUser.SumPermissions." + id);
		ArrayList<String> perms = new ArrayList<String>();
		HashMap<String, Boolean> map = getPermissionMap();
		for (String gp : map.keySet()) {
			Permission p = new Permission(gp, map.get(gp));
			if(!p.value) continue;
			perms.add(p.key);
//			if(p.type.equals("permission")) {
//				perms.add(p.key);
//			} else if(p.type.equals("inheritance")) {
//				Group g = PermissionRegistry.getGroup(p.key.replace("group.", ""));
//				if(g.g == null) continue;
//				for (String string : g.getFullPermissionList()) {
//					perms.add(string);
//				}
//			}
		}
		sw.stop();
		c.set(id, new Gson().toJson(perms));
		return perms;
	}
	
}
