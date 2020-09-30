package org.xbf.core.Utils.String;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StringMatcher {

	public static double match(String a, String b) {
		char[] ca = a.toCharArray();
		char[] cb = b.toCharArray();
		int matching = 0;
		int total = Math.min(ca.length, cb.length);
		char[] min = ca;
		char[] max = cb;
		if(ca.length > cb.length) {
			min = cb;
			max = ca;
		}
		
		for (int i = 0; i < min.length; i++) {
			if(min[i] == max[i] || min[i] + ' ' == max[i] || min[i] == max[i] + ' ')
				matching++;
		}
		return (double) matching / (double) total;
	}
	
	public static String mostMatching(String a, String[] b) {
		HashMap<String, Double> matches = new HashMap<>();
		for (String string : b) {
			matches.put(string, match(a, string));
		}
		Map<String, Double> sorted = matches
		        .entrySet()
		        .stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .collect(
		            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                LinkedHashMap::new));
//		for (String d : sorted.keySet()) {
//			System.out.println(d + ": " + sorted.get(d));
//		}
		
		return sorted.keySet().toArray(new String[0])[0];
	}
	
	public static String mostMatchingCommand(String a, String[] b) {
		HashMap<String, Double> matches = new HashMap<>();
		for (String string : b) {
			matches.put(string, match(a, string));
		}
		Map<String, Double> sorted = matches
		        .entrySet()
		        .stream()
		        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
		        .collect(
		            Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
		                LinkedHashMap::new));
//		for (String d : sorted.keySet()) {
//			System.out.println(d + ": " + sorted.get(d));
//		}
		double topValue = sorted.values().toArray(new Double[0])[0];
		if(a.length() < 2 && topValue != 1.0)
			return "";
		if(topValue < 0.33)
			return "";
		return sorted.keySet().toArray(new String[0])[0];
	}
	
	
	
	
//	public static void main(String[] args) {
//		String[] m1 = new String[] {
//				"help",
//				"test",
//				"wow",
//				"hello",
//				"testing"
//		};
//		System.out.println(mostMatching("he", m1));
//	}
	
}
