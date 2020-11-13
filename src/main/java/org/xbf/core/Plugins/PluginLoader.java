package org.xbf.core.Plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.xbf.core.XBF;
import org.xbf.core.XVI;
import org.xbf.core.XVI.Version;
import org.xbf.core.Exceptions.PluginLoadingFailed;
import org.xbf.core.Plugins.xbfbuiltin.XBFBuiltinPlugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import ch.qos.logback.classic.Logger;

public class PluginLoader {

	static HashMap<String, Class<? extends XervinJavaPlugin>> pluginClasses = new HashMap<>();
	static HashMap<String, String> pluginVersions = new HashMap<>();

	static HashMap<String, XervinJavaPlugin> plugins = new HashMap<>();
	
	static ClassLoader pluginClassLoader = PluginLoader.class.getClassLoader();

	static final Logger logger = (Logger) LoggerFactory.getLogger(PluginLoader.class);

	static void registerXBFAsAPlugin() {
		pluginClasses.put("xbf", XBFBuiltinPlugin.class);
		pluginVersions.put("xbf", XVI.version.version);
		logger.debug("Registered 'xbf' as a plugin with version '" + XVI.version.version + "'");
	}

	public static void loadPlugins() throws PluginLoadingFailed {
		logger.info("Loading external plugins");
		File dir = new File(XBF.getConfig().pluginDirectory);
		if (!dir.exists())
			dir.mkdir();
		registerXBFAsAPlugin();
		for (File f : dir.listFiles()) {
			try {
				loadPlugin(f);
			} catch (MalformedURLException e) {
				logger.warn("Plugin url error", e);
				e.printStackTrace();
			}
		}
		if (!runDependsCheck()) {
			logger.error("Plugin depend check failed. Aborting plugin loading!");
			throw new PluginLoadingFailed();
		}
		logger.info("Loading plugins");
		createInstances();
		logger.info("Registering");
		plugins.forEach((name, pl) -> {
			if(pl != null)
				pl.register();
		});
		logger.info("Starting plugins!");
		startPlugins();
		logger.info("Plugins loaded!");
	}

	private static void createInstances() {
		for (String plName : pluginClasses.keySet()) {
			if(plName.equals("xbf")) continue; // XBF does not have a plugin class
			try {
				plugins.put(plName, pluginClasses.get(plName).newInstance());
			} catch (Exception e) {
				logger.error("Failed to create plugin instance for " + plName, e);
			}
		}
	}

	public static void loadPlugin(File file) throws MalformedURLException {
		logger.info("Loading plugin from " + file.getName());
		long t1 = System.currentTimeMillis();
		if (!file.exists())
			return;
		URL loadPath = file.toURI().toURL();
		URL[] classUrl = new URL[] { loadPath };
		long t2 = System.currentTimeMillis();
		ClassLoader cl = new URLClassLoader(classUrl, pluginClassLoader);
		loadPluginsFromClassLoader(cl);
	}

	static void loadPluginsFromClassLoader(ClassLoader loader) {
		
		
		List<Class<?>> classes = new ArrayList<>();
		
		try {
			Enumeration<URL> urls = loader.getResources("resources/plugin.yml");
			while(urls.hasMoreElements()) {
				URL url = urls.nextElement();
				Yaml yml = new Yaml(new Constructor(PluginInformationFile.class));
				InputStream stream = url.openStream();
				PluginInformationFile pif = yml.load(stream);
				stream.close();
				classes.add(loader.loadClass(pif.mainClass));
			}
		} catch (IOException e) {
			logger.error("Failed to read plugin file", e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			logger.error("Plugin class not found!", e);
		}

		for (Class<?> class1 : classes) {
			if(!class1.isAnnotationPresent(XPlugin.class)) {
				logger.warn("Annotation XPlugin not present on class " + class1.getName());
				continue;
			}
			XPlugin plg = class1.getAnnotation(XPlugin.class);
			if (pluginClasses.containsKey(plg.name())) {
				logger.warn("Plugin '" + plg.name() + "' was already found once. Skipping.");
				continue;
			}


			if (class1.isAnnotationPresent(PluginVersion.class)) {
				String version = class1.getAnnotation(PluginVersion.class).currentVersion();
				pluginVersions.put(plg.name(), version);
				logger.info("Plugin '" + plg.name() + "' version '" + version + "' registered");
			} else {
				logger.warn("Plugin '" + plg.name() + "' registered without version");
			}

			pluginClasses.put(plg.name(), (Class<? extends XervinJavaPlugin>) class1);
		}
	}

	static boolean runDependsCheck() {
		boolean mayContinueLoading = true;
		for (Class<? extends XervinJavaPlugin> cls : pluginClasses.values()) {
			if(cls == null) continue;
			if (cls.isAnnotationPresent(DependsOn.class)) {
				XPlugin plg = cls.getAnnotation(XPlugin.class);
				for (DependsOn depend : cls.getAnnotationsByType(DependsOn.class)) {
					if (!pluginClasses.containsKey(depend.pluginName())) {
						logger.error("Plugin '" + plg.name() + "' depends on '" + depend.pluginName()
								+ "' which is not found");
						mayContinueLoading = false;
					} else {
						// Version check
						String pluginVersion = pluginVersions.get(depend.pluginName());
						if (pluginVersion == null) {
							logger.warn("Plugin '" + depend.pluginName() + "' does not have a version");
						} else if (depend.minimumVersion() == null) {
							logger.warn(plg.name() + " has a depends on '" + depend.pluginName()
									+ "' but with no minimum version");
						} else {
							Version currentVersion = null;
							try {
								currentVersion = new Version(pluginVersion, null, null, null);
							} catch (Exception ex) {
								logger.error("The plugin version of '" + depend.pluginName() + "' seems to be invalid",
										ex);
							}
							Version minimumVersion = null;
							try {
								minimumVersion = new Version(depend.minimumVersion(), null, null, null);
							} catch (Exception ex) {
								logger.error("The plugin '" + plg.name() + "' specified an invalid version of depend '"
										+ depend.pluginName() + "'", ex);
							}
							if (currentVersion != null && minimumVersion != null) {
								if (currentVersion.compareTo(minimumVersion) == -1) {
									logger.error("Plugin '" + plg.name() + "' depends on '" + depend.pluginName()
											+ "' version '" + minimumVersion.version + "' but the version found was '"
											+ currentVersion.version + "'");
									mayContinueLoading = false;
								}
							} else {
								logger.warn("One or more versions in the validation (min: " + depend.minimumVersion()
										+ ", current: " + pluginVersion + ") was invalid, coninuing anyway.");
							}
						}
					}
				}
			}
		}
		return mayContinueLoading;
	}

	static void startPlugins() {
		for (XervinJavaPlugin pl : plugins.values()) {
			if(pl == null) continue;
			pl.onEnable();
		}
	}
	
	public static HashMap<String, XervinJavaPlugin> getPlugins() {
		return plugins;
	}

}
