package org.xbf.core.Config;

import java.util.HashMap;

public class XConfiguration {

	public XDBConfig defaultDatabase;
	public HashMap<String, XDBConfig> databases = new HashMap<>();
	public String BOT_NAME = "Xervin";
	public String pluginDirectory = "plugins";
	
	public XConfiguration() {
		if(this.databases == null)
			this.databases = new HashMap<>();
	}
	
	public XConfiguration(boolean newConfig) {
		this();
		if(newConfig) {
			defaultDatabase = new XDBConfig();
			this.databases = new HashMap<>();
		}
	}
	
}
