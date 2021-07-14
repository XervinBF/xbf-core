package org.xbf.core.Plugins;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.xbf.core.Plugins.config.PluginConfig;
import org.xbf.core.Plugins.config.PluginConfigLoader;

public class XervinJavaPlugin {
	
	public Logger logger = (Logger) LoggerFactory.getLogger(getClass());
	
	public Logger getLogger() {
		return logger;
	}
	
	public void onEnable() {}
	
	public void onDisable() {}
	
	/**
	 * This is where command and module registration is supposed to happen
	 */
	public void register() {}
	
	public boolean registerPermission(String perm) {
		return false;
	}
	
	public XPlugin getPluginInfo() {
		return getClass().getAnnotation(XPlugin.class);
	}
	
	public <T extends PluginConfig> T getConfig(Class<T> baseClass)  {
		return new PluginConfigLoader<T>().loadConfig(getPluginInfo().name(), baseClass);
	}
	
	
}
