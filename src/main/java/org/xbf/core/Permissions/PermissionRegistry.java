package org.xbf.core.Permissions;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.xbf.core.XBF;
import org.xbf.core.Models.Permissions.PGroup;
import org.xbf.core.Models.Permissions.PUser;
import org.xbf.core.Models.Permissions.PUserPermission;
import org.xbf.core.Permissions.DataModels.Group;
import org.xbf.core.Permissions.DataModels.PermissionUser;
import org.xbf.core.Utils.Map.FastMap;

import ch.qos.logback.classic.Logger;

public class PermissionRegistry {

	static Logger log = (Logger) LoggerFactory.getLogger(PermissionRegistry.class);
	
	public static Group getGroup(String name) {
		return new Group(PGroup.getSmartTable().get(new FastMap<String, String>().add("gId", name)));
	}

	public static List<Group> getGroups(ArrayList<String> groups2) {
		ArrayList<Group> gr = new ArrayList<>();
		for (String string : groups2) {
			gr.add(getGroup(string));
		}
		return gr;
	}

	public static PermissionUser getUser(int id) {
		PUser u = PUser.getSmartTable().get(new FastMap<String, String>().add("uid", id + ""));
		if (u == null) {
			u = new PUser();
			u.permissions = new ArrayList<PUserPermission>();
			PUserPermission pup = new PUserPermission();
			pup.keyIndex = "group.default";
			if(id == XBF.getConfig().ownerUserId)
				pup.keyIndex = "group.admin";
			pup.value = true;
			u.permissions.add(pup);
			u.uid = id + "";
			u = PUser.getSmartTable().set(u);
			PUser.getSmartTable().c.clear();
		}
		return new PermissionUser(u);
	}

	static List<String> permissions = new ArrayList<>();

	public static List<String> getPermissions() {
		return permissions;
	}


	public static void registerPermission(String permission) {
		if (!permissions.contains(permission))
			permissions.add(permission);
	}

	public static ArrayList<String> pendingPerms = new ArrayList<>();

	/**
	 * module.PACKAGE.CLASSNAME.INPUT
	 * @param strings
	 */
	public static void regPerms(String... strings) {
		for (String string : strings) {
			registerPermission(string.toLowerCase());
		}
	}


}
