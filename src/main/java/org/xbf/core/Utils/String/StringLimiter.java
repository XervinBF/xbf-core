package org.xbf.core.Utils.String;

import java.util.ArrayList;

public class StringLimiter {

	public static String[] splitAt(String string, int at) {
		String[] characters = string.split("");
//		int splits = (int) (Math.floor(string.length() / at) + (string.length() % at != 0 ? 1 : 0));
		ArrayList<String> list = new ArrayList<String>();
		String current = "";
		for (int i = 0; i < characters.length; i++) {
			if(current.length() > at - 200) {
				if(current.length() >= at) {
					list.add(current);
					current = "";
				} else if(characters[i] == "\n" || (characters[i - 1] == "\\" && characters[i] == "n") || current.endsWith("\n")) {
					list.add(current);
					current = "";
				}
			}
			current += characters[i];
		}
		list.add(current);
		return list.toArray(new String[0]);
	}
	
	public static String limit(String string, int at) {
		if(string.length() < at) return string;
		String[] characters = string.split("");
		String res = "";
		for (int i = 0; i < at - 3; i++) {
			res += characters[i];
		}
		res += "...";
		return res;
	}
	
}
