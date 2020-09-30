package org.xbf.core.Plugins.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;

import org.xbf.core.XBF;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.Files;

public class PluginConfigLoader<T extends PluginConfig> {

	public T loadConfig(String pluginName, Class<T> baseClass) {
		return loadConfig(pluginName, "config", baseClass);
	}

	public T loadConfig(String pluginName, String fileName, Class<T> baseClass) {
		T cfg = null;
		ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
		yaml.findAndRegisterModules();

		File f = new File(XBF.getConfig().pluginDirectory + "/config/" + pluginName);
		if (!f.exists())
			f.mkdirs();
		f = new File(f, fileName + ".yml");
		if (!f.exists()) {
			yaml.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			try {
				f.createNewFile();
				cfg = baseClass.newInstance();
				yaml.writeValue(f, cfg);
			} catch (InstantiationException | IllegalAccessException | IOException e) {
				e.printStackTrace();
			}
		} else {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			try {
				cfg = yaml.readValue(fis, baseClass);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (Field field : baseClass.getFields()) {
			if (field.isAnnotationPresent(EnvironmentVariable.class)) {
				if (System.getenv(field.getAnnotation(EnvironmentVariable.class).value()) != null) {
					try {
						cfg.getClass().getField(field.getName()).set(cfg,
								System.getenv(field.getAnnotation(EnvironmentVariable.class).value()));
					} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
							| SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return (T) cfg;
	}

	public void saveConfig(Object config, String pluginName) {
		saveConfig(config, pluginName, "config");
	}
	
	public void saveConfig(Object config, String pluginName, String fileName) {
		ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
		yaml.findAndRegisterModules();
		yaml.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		File f = new File(XBF.getConfig().pluginDirectory + "/config/" + pluginName);
		if (!f.exists())
			f.mkdirs();
		f = new File(f, fileName + ".yml");
		try {
			f.createNewFile();
			yaml.writeValue(f, config);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
