package org.xbf.core.Plugins;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.util.Loader;

public class Service {

	public String[] args = new String[0];
	public boolean once = false;
	public boolean timeSync = false;
	public Thread thread;
	public String name = this.getClass().getSimpleName().replace("Service", "").toLowerCase();
	public boolean allowMultipleInstances = false;
	public Logger l;
	public int id;
	public int errors = 0;
	public void run() throws Exception {}
	public void onStart() {}
	public void onStop() {}
	
	
}
