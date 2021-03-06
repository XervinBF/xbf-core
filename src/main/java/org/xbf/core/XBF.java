package org.xbf.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.xbf.core.ChatHandlers.ChatHandler;
import org.xbf.core.ChatHandlers.ChatHandlerResult;
import org.xbf.core.ChatHandlers.FormChatHandler;
import org.xbf.core.Config.XCacheDurations;
import org.xbf.core.Config.XConfiguration;
import org.xbf.core.Config.XDBConfig;
import org.xbf.core.Data.DBConnector;
import org.xbf.core.Data.NXDBConnector;
import org.xbf.core.Data.Connector.DbType;
import org.xbf.core.Data.Connector.IDBProvider;
import org.xbf.core.Exceptions.AnnotationNotPresent;
import org.xbf.core.Exceptions.HandlerLoadingFailed;
import org.xbf.core.Exceptions.PluginLoadingFailed;
import org.xbf.core.Install.XBFInstaller;
import org.xbf.core.Messages.Request;
import org.xbf.core.Messages.Response;
import org.xbf.core.Models.XUser;
import org.xbf.core.Models.Permissions.Permission;
import org.xbf.core.Module.Command;
import org.xbf.core.Module.Module;
import org.xbf.core.Plugins.Handler;
import org.xbf.core.Plugins.PluginLoader;
import org.xbf.core.Plugins.PluginPermissionFile;
import org.xbf.core.Plugins.PluginResourceReader;
import org.xbf.core.Plugins.Service;
import org.xbf.core.Plugins.XHandler;
import org.xbf.core.Plugins.XService;
import org.xbf.core.Plugins.XervinJavaPlugin;
import org.xbf.core.Plugins.PluginPermissionFile.PluginDefinedPermission;
import org.xbf.core.Utils.Language.DictionaryLoader;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.Files;
import com.google.gson.Gson;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class XBF {

	public static final String BOT_FRAMEWORK_NAME = "XBF";
	static XConfiguration config;
	static final Logger logger = (Logger) LoggerFactory.getLogger(XBF.class);
	
	public static void main(String[] args) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		root.setLevel(Level.INFO);
		
		
		
		logger.info("Loading " + BOT_FRAMEWORK_NAME + " v" + XVI.version.version);
		
		logger.info("Loading config");
		reloadConfig();
		if(config == null) {
			logger.error("Config loading failed, exiting.");
			System.exit(1);
		}
		logger.info("Starting bot as '" + config.BOT_NAME + "'");
		
		registerChatHandler(new FormChatHandler());
		try {
			PluginLoader.loadPlugins();
		} catch (PluginLoadingFailed e) {
			logger.info("Halting..");
			return;
		}
		
		logger.info("Starting handlers");
		
		handlers.forEach(h -> {
			h.start();
		});
		
		PluginLoader.startPlugins();
		
		if(args.length != 0) {
			if(args[0].equalsIgnoreCase("install")) {
				logger.info("Running XBF installer");
				XBFInstaller.runInstaller();
				logger.info("Installer completed!");
				logger.info("Restart XBF without the install argument to complete setup");
				return;
			}
		}
		
		
		
		logger.info("Starting services");
		ServiceController.XStartedTrigger();
		
		logger.info(BOT_FRAMEWORK_NAME + " has been started");
		
		logger.info("Started bot as name '" + config.BOT_NAME + "'");
		
		saveConfig();
		
				
	}
	
	public static XConfiguration getConfig() {
		if(config == null) {
			reloadConfig();
		}
		if(config.cacheDurations == null)
			config.cacheDurations = new XCacheDurations();
		if(config.defaultDatabase == null)
			config.defaultDatabase = new XDBConfig();
		return config;
	}
	
	public static void reloadConfig() {
		ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
		yaml.findAndRegisterModules();
		File configFile = new File("config.yml");
		if(!configFile.exists()) {
			config = new XConfiguration(true);
			saveConfig();
		} else {
			InputStream configFileInputStream = null;
			try {
				configFileInputStream = new FileInputStream(configFile);
			} catch (FileNotFoundException e1) {
				logger.error("Did the config get removed during loading?");
				e1.printStackTrace();
				return;
			}
			try {
				config = yaml.readValue(configFileInputStream, XConfiguration.class);
			} catch (IOException e1) {
				logger.error("Failed to load config", e1);
				e1.printStackTrace();
			}
			try {
				configFileInputStream.close();
			} catch (IOException e) {
				logger.error("Failed to load config from " + configFile.getName(), e);
				e.printStackTrace();
			}
		}
	}
	
	static DumperOptions getDumperOptions() {
		DumperOptions dumperOptions = new DumperOptions();
		dumperOptions.setPrettyFlow(true);
		dumperOptions.setDefaultFlowStyle(FlowStyle.AUTO);
//		dumperOptions.setCanonical(true);
		dumperOptions.setAllowUnicode(true);
		dumperOptions.setTimeZone(TimeZone.getDefault());
//		dumperOptions.setVersion(Version.V1_0);
		return dumperOptions;
	}
	
	public static void saveConfig() {
		logger.debug("Saving config to 'config.yml'");
		File configFile = new File("config.yml");
		ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
		yaml.findAndRegisterModules();
		try {
			yaml.writeValue(configFile, config);
		} catch (IOException e) {
			logger.error("Failed saving of new config", e);
			e.printStackTrace();
		}
		logger.debug("Config saved to 'config.yml'");
	}
	
	static ArrayList<Module> modules = new ArrayList<>();
	static ArrayList<Handler> handlers = new ArrayList<Handler>();
	static ArrayList<ChatHandler> chatHandlers = new ArrayList<ChatHandler>();
	static HashMap<String, Class<?>> databaseProviders = new HashMap<String, Class<?>>();
	
	public static Module registerModule(String name) {
		Module mod = new Module();
		mod.name = name;
		mod.commands = new ArrayList<>();
		modules.add(mod);
		return mod;
	}



	public static ArrayList<Module> getModules() {
		return modules;
	}
	
	public static Optional<Module> getModule(String name) {
		return modules.stream().filter(m -> m.name.equalsIgnoreCase(name)).findFirst();
	}
	
	public static Optional<Module> getModuleForCommand(Command cmd) {
		return modules.stream().filter(m -> m.commands.stream().anyMatch(c -> c == cmd)).findFirst();
	}
	
	public static Optional<Module> getModuleForCommand(String cmd) {
		return modules.stream().filter(m -> m.commands.stream().anyMatch(c -> c.command.equalsIgnoreCase(cmd))).findFirst();
	}
	
	public static List<Module> getModulesWithCommand(String cmd) {
		return modules.stream().filter(m -> m.commands.stream().anyMatch(c -> c.command.equalsIgnoreCase(cmd))).collect(Collectors.toList());
	}

	public static ArrayList<Handler> getHandlers() {
		return handlers;
	}
	
	public static Handler registerHandler(Class<? extends Handler> handlerClass) throws AnnotationNotPresent, HandlerLoadingFailed {
		if(!handlerClass.isAnnotationPresent(XHandler.class)) {
			logger.warn("XHandler annotation is not present for HandlerClass '" + handlerClass.getName() + "'");
			logger.warn("Handler not enabled");
			throw new AnnotationNotPresent("XHandler");
		}
		Handler handler;
		try {
			handler = handlerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new HandlerLoadingFailed(e);
		}
		handlers.add(handler);
		return handler;
	}
	
	public static void sendMessageToChannel(String provider, String channelId, Response msg) {
		for (Handler h : handlers) {
			if(h.getAnnotation().providerName().equalsIgnoreCase(provider)) {
				h.sendMessageToChannel(channelId, msg);
			}
		}
	}
	
	public static ArrayList<ChatHandler> getChatHandlers() {
		return chatHandlers;
	}
	
	public static ChatHandler registerChatHandler(ChatHandler chatHandler) {
		chatHandlers.add(chatHandler);
		return chatHandler;
	}
	
	public static ChatHandler registerChatHandler(Function<Request, ChatHandlerResult> action) {
		return registerChatHandler(new ChatHandler(action));
	}
	
	public static IDBProvider registerDatabaseProvider(Class<?> dbProviderClass) throws AnnotationNotPresent {
		if(!dbProviderClass.isAnnotationPresent(DbType.class)) {
			logger.warn("DbType annotation is not present for IDBProvider '" + dbProviderClass.getName() + "'");
			logger.warn("Handler not enabled");
			throw new AnnotationNotPresent("DbType");
		}
		IDBProvider provider = null;
		try {
			provider = (IDBProvider) dbProviderClass.newInstance();
		} catch (InstantiationException e) {
			logger.error("Failed to register database provider", e);
		} catch (IllegalAccessException e) {
			logger.error("Failed to register database provider", e);
		}
		if(provider == null) return null;
		String dbtype = dbProviderClass.getAnnotation(DbType.class).name();
		logger.info("Registered IDBProvider with dbtype: " + dbtype);
		databaseProviders.put(dbtype, dbProviderClass);
		return provider;
	}
	
	
	public static IDBProvider getDatabaseProvider(String dbType) {
		if(!databaseProviders.containsKey(dbType)) {
			logger.error("Database provider '" + dbType + "' is not registered");
			logger.warn("Please check your configuration for spelling errors. The application will most likely throw a lot of errors now.");
			return null;
		}
		Class<?> dbProviderClass = (Class<?>) databaseProviders.get(dbType);
		IDBProvider provider = null;
		try {
			provider = (IDBProvider) dbProviderClass.newInstance();
		} catch (InstantiationException e) {
			logger.error("Failed to register database provider", e);
		} catch (IllegalAccessException e) {
			logger.error("Failed to register database provider", e);
		}
		return provider; 
	}

	public static void registerService(Class<? extends Service> serviceClass) throws AnnotationNotPresent {
		if(!serviceClass.isAnnotationPresent(XService.class)) {
			logger.error("XService annotation not present on service class");
			throw new AnnotationNotPresent("XService");
		}
		ServiceController.services.add(serviceClass);
	}
	
	public static DumperOptions getYamlOptions() {
		return getDumperOptions();
	}
	
	public static XUser getOwnerUser() {
		if(XUser.userExists(config.ownerUserId)) {
			return new XUser(config.ownerUserId);
		}
		return null;
	}
	
	public static boolean tryRegisterPermission(String permission) {
		try {
			InputStream stream1 = DictionaryLoader.class.getClassLoader().getResourceAsStream("permissions/xbf.yml");
			if(stream1 == null)
				stream1 = DictionaryLoader.class.getClassLoader().getResourceAsStream("resources/permissions/xbf.yml");
			String theString = IOUtils.toString(stream1, "UTF-8");
			ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
			yaml.findAndRegisterModules();
			PluginPermissionFile permissionFile = yaml.readValue(theString, PluginPermissionFile.class);
			if(permissionFile.permissions.containsKey(permission)) {
				PluginDefinedPermission pluginDefinedPermission = permissionFile.permissions.get(permission);
				if(pluginDefinedPermission.editPermission != null) {
					Permission.registerPermissionIfNotFound(permission, pluginDefinedPermission.name, pluginDefinedPermission.description, pluginDefinedPermission.editPermission);
				} else {
					Permission.registerPermissionIfNotFound(permission, pluginDefinedPermission.name, pluginDefinedPermission.description);
				}
				return true;
			}
		} catch (Exception e) {
			logger.error("Permission file reading failed: xbf.yml", e);
		}
		
		try {
			for (File file : PluginResourceReader.getFilesFoldersFromAllPlugins("/resources/permissions")) {
				try {
					InputStream stream = new FileInputStream(file);
					String theString = IOUtils.toString(stream, "UTF-8"); 
					ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
					yaml.findAndRegisterModules();
					PluginPermissionFile permissionFile = yaml.readValue(theString, PluginPermissionFile.class);
					if(permissionFile.permissions.containsKey(permission)) {
						PluginDefinedPermission pluginDefinedPermission = permissionFile.permissions.get(permission);
						if(pluginDefinedPermission.editPermission != null) {
							Permission.registerPermissionIfNotFound(permission, pluginDefinedPermission.name, pluginDefinedPermission.description, pluginDefinedPermission.editPermission);
						} else {
							Permission.registerPermissionIfNotFound(permission, pluginDefinedPermission.name, pluginDefinedPermission.description);
						}
						return true;
					}
				} catch (Exception ex) {
					logger.error("Permission file reading failed: " + file.getName(), ex);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to locate permission files", e);
		}
		
		try {
			for (String pluginName : PluginLoader.getPlugins().keySet()) {
				try {
					InputStream stream1 = DictionaryLoader.class.getClassLoader().getResourceAsStream("permissions/" + pluginName + ".yml");
					if(stream1 == null)
						stream1 = DictionaryLoader.class.getClassLoader().getResourceAsStream("resources/permissions/" + pluginName + ".yml");
					String theString = IOUtils.toString(stream1, "UTF-8");
					ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
					yaml.findAndRegisterModules();
					PluginPermissionFile permissionFile = yaml.readValue(theString, PluginPermissionFile.class);
					if(permissionFile.permissions.containsKey(permission)) {
						PluginDefinedPermission pluginDefinedPermission = permissionFile.permissions.get(permission);
						if(pluginDefinedPermission.editPermission != null) {
							Permission.registerPermissionIfNotFound(permission, pluginDefinedPermission.name, pluginDefinedPermission.description, pluginDefinedPermission.editPermission);
						} else {
							Permission.registerPermissionIfNotFound(permission, pluginDefinedPermission.name, pluginDefinedPermission.description);
						}
						return true;
					}
				} catch (Exception ex) {
//					logger.error("Permission file reading failed: " + pluginName + ".yml", ex);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to locate permission files", e);
		}
		
		
		
		if(permission.startsWith("plugin.")) {
			String plugin = permission.split("\\.")[1];
			if(PluginLoader.getPlugin(plugin).registerPermission(permission))
				return true;
		}
		for (XervinJavaPlugin plugin : PluginLoader.getPlugins().values()) {
			if(plugin.registerPermission(permission))
				return true;
		}
		if(XBF.getConfig().createPermissionsAutomaticallyIfNotCreatedByPlugin) {
			Permission.registerPermissionIfNotFound(permission, "Unnamed - " + permission, "No plugin registered the permission by request so the permission was registered automatically. To change this behaviour set createPermissionsAutomaticallyIfNotCreatedByPlugin to false in config.yml.");
			return true;
		}
		return false;
	}
	
	
}
