package org.xbf.core.Install;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import org.slf4j.LoggerFactory;
import org.xbf.core.Install.Steps.PermissionsSetup;

import ch.qos.logback.classic.Logger;

public class XBFInstaller {
	
	static final Logger logger = (Logger) LoggerFactory.getLogger(XBFInstaller.class);

	static ArrayList<SetupStage> getStages() {
		ArrayList<SetupStage> stages = new ArrayList<>();
		stages.add(new PermissionsSetup());
		return stages;
	}
	
	public static boolean runInstaller() {
		ArrayList<SetupStage> stages = getStages();
		HashMap<Integer, SetupStage> stagesWithStagePlacement = new HashMap<>();
		for (SetupStage setupStage : stages) {
			stagesWithStagePlacement.put(setupStage.getClass().getAnnotation(InstallerStage.class).stageId(), setupStage);
		}
		TreeMap<Integer, SetupStage> stagesToRun = new TreeMap<>(stagesWithStagePlacement);
		for (SetupStage setupStage : stagesToRun.values()) {
			String name = setupStage.getClass().getAnnotation(InstallerStage.class).stageName();
			logger.info("Running installer stage " + name);
			if(!setupStage.executeStep()) { 
				logger.error("Installer stage " + name + " failed");
				return false;
			}
		}
		return true;
	}
	
}
