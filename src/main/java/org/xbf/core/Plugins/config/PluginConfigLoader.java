package org.xbf.core.Plugins.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;

import org.xbf.core.XBF;
import org.yaml.snakeyaml.Yaml;

import com.google.common.io.Files;

public class PluginConfigLoader<T extends PluginConfig> {

	public T loadConfig(String pluginName, Class<T> baseClass) {
		return loadConfig(pluginName, "config", baseClass);
	}
	
	public T loadConfig(String pluginName, String fileName, Class<T> baseClass) {
		T cfg = null;
		
		Yaml yml = new Yaml(XBF.getYamlOptions());
		File f = new File(XBF.getConfig().pluginDirectory + "/config/" + pluginName);
		if(!f.exists())
			f.mkdirs();
		f = new File(f, fileName + ".yml");
		if(!f.exists()) {
			try {
				f.createNewFile();
				cfg = baseClass.newInstance();
				Files.write(yml.dump(cfg).getBytes(), f);
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
			cfg = yml.loadAs(fis, baseClass);
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for (Field field : baseClass.getFields()) {
			if(field.isAnnotationPresent(EnvironmentVariable.class)) {
				if(System.getenv(field.getAnnotation(EnvironmentVariable.class).value()) != null) {
					try {
						cfg.getClass().getField(field.getName()).set(cfg, System.getenv(field.getAnnotation(EnvironmentVariable.class).value()));
					} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
							| SecurityException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return (T) cfg;
	}
	
}
