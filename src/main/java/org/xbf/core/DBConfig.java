package org.xbf.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.xbf.core.Cache.ObjectCache;
import org.xbf.core.Models.Config.XervinConfig;
import org.xbf.core.Utils.Map.FastMap;

public class DBConfig {

	public static boolean DEV = false;
	
	
	static ObjectCache configCache = new ObjectCache("XConfig", 60000 * 60 * 6); // 6 hours


	public static String[] userConfig;
	
	public static String[] userConfigDesc;
	
	public static HashMap<String, String> userConfigDescriptors = new HashMap<>();
	
	public static void reloadSetVariables() {
		userConfig = new String[] {
				"framework.prefix",
				"framework.commandcompletion",
				"dict",
		};
		
		userConfigDesc = new String[] {
				"Prefix for Xervin commands",
				"Xervin will guess what your command are",
				getDictOptions(),
		};
		userConfigDescriptors.put("framework.prefix", "Prefix f�r Einstein Kommandon");
		userConfigDescriptors.put("framework.commandcompletion", "Einstein kommer att gissa ditt kommando (felstavningar)");
		userConfigDescriptors.put("dict", getDictOptions());

		userConfigDescriptors.put("framework.timings", "Display Timings after commands (true/false)");
	}
	
	private static String getDictOptions() {
		String str = "";
		for (String string : new File("locals").list()) {
			str += string.replace(".lang", "") + ", ";
		}
		return str.substring(0, str.length() - 2);
	}
	
	// Config Connection
	public static void setConfig(String key, String value) {
		XervinConfig cfg = XervinConfig.getSmartTable().get(
					new FastMap<String, String>()
					.add("configkey", key)
				);
		if(cfg == null) cfg = new XervinConfig();
		cfg.configKey = key;
		cfg.configValue = value;
		XervinConfig.getSmartTable().set(cfg);
	}

	public static String getConfig(String key) {
		XervinConfig cfg = XervinConfig.getSmartTable().get(
				new FastMap<String, String>()
				.add("configkey", key)
			);
		if(cfg == null) return null;
		return cfg.configValue.trim();
	}

	public static HashMap<String, String> getConfigMap() {
		HashMap<String, String> map = new HashMap<>();
		List<XervinConfig> cfgs = XervinConfig.getSmartTable().getAll();
		for (XervinConfig cfg : cfgs) {
			map.put(cfg.configKey, cfg.configValue.trim());
		}
		return map;
	}

	public static boolean hasConfig(String key) {
		if(configCache.get(key) != null) return true;
		return XervinConfig.getSmartTable().hasWithQuery(
				new FastMap<String, String>()
				.add("configkey", key));
	}
	
	
	public static String getDefaultLang() {
		return getConfig("dict");
	}
	
	public static String getPrefix() {
		return getConfig("framework.prefix");
	}
	
}
