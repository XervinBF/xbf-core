package org.xbf.core.Utils.Language;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.xbf.core.Plugins.PluginLoader;
import org.xbf.core.Plugins.XervinJavaPlugin;

import ch.qos.logback.classic.Logger;

public class DictionaryLoader {

	static final Logger logger = (Logger) LoggerFactory.getLogger(DictionaryLoader.class);
	
	public static List<String> load(String dict) {
		List<String> str = new ArrayList<>();
		// For development when not built (default dictionaries)
		try {
			InputStream stream1 = DictionaryLoader.class.getClassLoader().getResourceAsStream(dict + ".lang");
			if(stream1 == null)
				stream1 = DictionaryLoader.class.getClassLoader().getResourceAsStream("resources/" + dict + ".lang");
			String theString = IOUtils.toString(stream1, "UTF-8"); 
			str.add(theString);
		} catch (Exception ex) {
			logger.error("Reading failed", ex);
		}
		
		for (XervinJavaPlugin plc : PluginLoader.getPlugins().values()) {
			if(plc == null) continue;
			String search = "resources/lang/" + plc.getPluginInfo().name() + "/" + dict + ".lang";
			logger.trace("Looking for language file in [" + plc.getPluginInfo().name() + "] @ [" + search + "]");
			URL url = plc.getClass().getClassLoader().getResource(search);
			logger.trace("File: " + url);
			if(url == null) continue;
			logger.trace("File found!");
			try {
				InputStream stream = url.openStream();
				String theString = IOUtils.toString(stream, "UTF-8"); 
				str.add(theString);
			} catch (IOException ex) {
				logger.error("Reading failed", ex);
			}
		}
		return str;
	}
	
}
