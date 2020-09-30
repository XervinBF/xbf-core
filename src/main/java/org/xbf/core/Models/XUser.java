package org.xbf.core.Models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.xbf.core.DBConfig;
import org.xbf.core.XBF;
import org.xbf.core.Cache.ObjectCache;
import org.xbf.core.Data.SmartTable;
import org.xbf.core.Data.SmartTableObject;
import org.xbf.core.Messages.Response;
import org.xbf.core.Models.Config.UserConfig;
import org.xbf.core.Permissions.PermissionRegistry;
import org.xbf.core.Utils.Language.Dict;
import org.xbf.core.Utils.Map.FastMap;
import org.xbf.core.Utils.String.StringMatcher;
import org.xbf.core.Utils.String.StringUtils;
import org.xbf.core.Utils.Timings.Stopwatch;

import ch.qos.logback.classic.Logger;


public class XUser extends SmartTableObject {

	public static ObjectCache c = new ObjectCache("USER.IMPERSONATE", (60000 * 30));
	ObjectCache configCache;

	public static XUser getFromProvider(String id, String provider) {
		UserProvider prov = UserProvider.getSmartTable().get(new FastMap<String, String>()
				.add("extid", id + "")
				.add("provider", provider));		
		if(prov == null) {
			return null;
		}
				
		return new XUser(Integer
				.parseInt(prov.uid));
	}
	
	public static XUser getFromProvider(String id, String provider, String name) {
		UserProvider prov = UserProvider.getSmartTable().get(new FastMap<String, String>()
				.add("extid", id + "")
				.add("provider", provider));		
		if(prov == null) {
			
			return XUser.createNewUserAndAddProvider(id + "", "Discord", StringUtils.replace(name, safeMap));
		}
		
		UserProvider.setName(id, provider, StringUtils.replace(name, safeMap));
		
		return new XUser(Integer
				.parseInt(prov.uid));
	}
	
	public static HashMap<String, String> safeMap = new FastMap<String, String>()
			.add("ö", "o")
			.add("å", "a")
			.add("ä", "a")
			.add("!", "")
			.add("-", "")
			.add(".", "")
			.getMap();
	
	public static HashMap<String, String> namePriorityPermissions = new HashMap<String, String>();
	public static List<String> namePriority = new ArrayList<>();
	
	
	public String getName(String permission) {
		String highestPriority = null;
		int pnum = Integer.MAX_VALUE;
		for (UserProvider p : UserProvider.getSmartTable().getMultiple(new FastMap<String, String>()
				.add("uid", id + ""))) {
			if(p.name == null) continue;
			int priority = namePriority.indexOf(p.provider);
			if(priority < pnum) {
				String perm = namePriorityPermissions.get(p.provider);
				if(perm == null || perm.trim() == "") {
					highestPriority = p.name.name;
					pnum = priority;
				} else if(StringUtils.match(permission, "userdata.name." + p.provider)) {
					highestPriority = p.name.name;
					pnum = priority;
				}
			}
		}
		return highestPriority;
	}
	
	public boolean hasPermission(String permission) {
		Stopwatch s = Stopwatch.startnew("USR." + id + ".PERM." + permission);
		HashMap<String, Boolean> map = PermissionRegistry.getUser(id).getPermissionMap();
		HashMap<String, Boolean> matching = new HashMap<>();
		for (String string : map.keySet()) {
			if(StringUtils.match(string, permission))
				matching.put(string, map.get(string));
		}
		if(matching.size() == 0) {
			s.stop();
			return false;
		}
		boolean res = matching.get(StringMatcher.mostMatching(permission, matching.keySet().toArray(new String[0])));
		s.stop();
		return res;
	}
	//             Permission          Configuration
	static HashMap<String, String> configPermissions = new FastMap<String, String>()
			.add("framework.timings", "framework.timings")
			.getMap();
	
	public ArrayList<String> getAvailibleConfiguration() {
		ArrayList<String> conf = new ArrayList<>();
		conf.add("framework.prefix");
		conf.add("dict");

		for (String s : configPermissions.keySet()) {
			if(hasPermission(s) && !conf.contains(configPermissions.get(s)))
				conf.add(configPermissions.get(s));
		}
		Collections.sort(conf);
		return conf;
	}
	
	public static synchronized XUser createNewUserAndAddProvider(String extId, String provider, String name) {
		name = StringUtils.replace(name, safeMap);
		UserProvider prov = UserProvider.getSmartTable().get(new FastMap<String, String>()
				.add("extid", extId + "")
				.add("provider", provider));
		if(prov != null) return getFromProvider(extId, provider);
		int id = XUser.getSmartTable().getNextId();
		createUser(id, name);
		UserProvider p = new UserProvider();
		p.extid = extId;
		p.provider = provider;
		p.uid = id + "";
		UserProvider.getSmartTable().set(p);
		UserProvider.getSmartTable().c.clear();
		return getFromProvider(extId, provider);
	}

	static Logger l = (Logger) LoggerFactory.getLogger(XUser.class);
	
	public boolean isImpersonated;
	public long realUserId;

	public XUser() {
		super("Users");
	}

	public XUser(int internalId) {
		super("Users");
		String s = (String) c.get("" + internalId);
		id = internalId;
		if(s != null) {
			id = Integer.parseInt(s);
			isImpersonated = true;
			realUserId = internalId;
		}
		configCache = new ObjectCache("USER.CONFIG." + id, 60000 * 10);
	}
	
	@Override
	public void objectLoaded() {
		configCache = new ObjectCache("USER.CONFIG." + id, 60000 * 10);
	}

	public void sendMessage(Response res) {
		XBF.getHandlers().forEach(handler -> {
			for (String handlerUserId : getProviderIds(handler.getAnnotation().providerName())) {
				handler.sendMessage(handlerUserId, res);
			}
		});
	}

	public String getProviderId(String provider) {
		UserProvider p = UserProvider.getSmartTable().get(new FastMap<String, String>()
				.add("uid", id + "")
				.add("provider", provider));
		if(p == null) return null;
		return p.extid;
	}
	
	public List<String> getProviderIds(String provider) {
		ArrayList<String> st = new ArrayList<>();
		for (UserProvider p : UserProvider.getSmartTable().getMultiple(new FastMap<String, String>()
				.add("uid", id + "")
				.add("provider", provider))) {
			st.add(p.extid);
		}
		return st;
	}
	
	public List<UserProvider> getProviders() {
		return UserProvider.getSmartTable().getMultiple(new FastMap<String, String>().add("uid", id + ""));
	}


	public void setConfig(String key, String value) {
		UserConfig cfg = UserConfig.getSmartTable().get(
					new FastMap<String, String>()
					.add("userid", id + "")
					.add("configkey", key)
				);
		if(cfg == null) cfg = new UserConfig();
		cfg.configKey = key;
		cfg.configValue = value;
		cfg.userId = id;
		UserConfig.getSmartTable().set(cfg);
	}

	public String getConfigRaw(String key) {
		UserConfig cfg = UserConfig.getSmartTable().get(
				new FastMap<String, String>()
				.add("userid", id + "")
				.add("configkey", key)
			);
		if(cfg == null) return null;
		return cfg.configValue;
	}
	
	public String getConfig(String key) {
		return getConfig(key, DBConfig.getConfig(key));
	}
	
	public String getConfig(String key, String defaultResponse) {
		if(configCache.get(key) != null) return (String) configCache.get(key);
		String res = getConfigRaw(key);
		configCache.set(key, res);
		if(res == null) res = defaultResponse;
		return res;
	}

	public HashMap<String, String> getConfigMap() {
		HashMap<String, String> map = new HashMap<>();
		List<UserConfig> cfgs = UserConfig.getSmartTable().getMultiple(
				new FastMap<String, String>()
				.add("userid", id + "")
			);
		for (UserConfig cfg : cfgs) {
			map.put(cfg.configKey, cfg.configValue.trim());
		}
		return map;
	}

	public boolean hasConfig(String key) {
		if(configCache.get(key) != null) return true;
		return UserConfig.getSmartTable().hasWithQuery(
				new FastMap<String, String>()
				.add("configkey", key)
				.add("userid", id + ""));
	}

	public Dict getDict() {
		String dc = getConfig("dict");
		if (dc == null)
			return new Dict(DBConfig.getDefaultLang());
		return new Dict(dc);
	}

	public static SmartTable<XUser> getSmartTable() {
		return new SmartTable<XUser>("Users", XUser.class);
	}
	
	private static void createUser(int uid, String username) {
		l.info("Registering user " + username);
		XUser usr = new XUser();
		usr.id = uid;
		getSmartTable().set(usr);
		PermissionRegistry.getUser(uid);
	}

	public static boolean userExists(int id) {
		return getSmartTable().hasWithQuery(new FastMap<String, String>().add("id", id + ""));
	}

	public static void removeAccount(String uid) {
		l.info("Deleting id " + uid);
		l.debug("Account removing is currently not supported");
	}

}
