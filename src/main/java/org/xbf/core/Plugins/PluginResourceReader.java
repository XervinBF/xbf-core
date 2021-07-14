package org.xbf.core.Plugins;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

import org.xbf.core.XBF;

public class PluginResourceReader {

	public static URI getURIToResourceInFile(File file, String resourcePath) {
		try {
			return new URI(file.getAbsolutePath() + "!" + resourcePath);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static File[] getFilesFoldersFromAllPlugins(String resourceFolder) {
		ArrayList<File> files = new ArrayList<File>();
		for (File file : new File(XBF.getConfig().pluginDirectory).listFiles()) {
			URI uri = getURIToResourceInFile(file, resourceFolder);
			File resourceFolderRef = new File(uri);
			files.addAll(Arrays.asList(resourceFolderRef.listFiles()));
		}
		return files.toArray(new File[0]);
	}
	
	public static File getFirstFileInAnyPlugin(String resourcePath) {
		for (File file : new File(XBF.getConfig().pluginDirectory).listFiles()) {
			URI uri = getURIToResourceInFile(file, resourcePath);
			File resourceFile = new File(uri);
			if(resourceFile.exists()) return resourceFile;
		}
		return null;
	}
	
}
