package org.xbf.core.Plugins;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

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
	
	
}
