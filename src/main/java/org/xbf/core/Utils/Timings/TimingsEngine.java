package org.xbf.core.Utils.Timings;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.xbf.core.Messages.Pair;

public class TimingsEngine {

	
	public static ArrayList<Consumer<Pair<String, Long>>> timingsListeners = new ArrayList<>();
	
	
	public static void RecordTimings(String name, long executionTime) {
		for (Consumer<Pair<String, Long>> listener : timingsListeners) {
			listener.accept(new Pair<String, Long>(name, executionTime));
		}
	}
	
}
