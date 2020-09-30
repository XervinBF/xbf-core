package org.xbf.core.Utils.String;

import java.util.HashMap;

public class StringUtils {

	public static String replace(String str, HashMap<String, String> replaceMap) {
		for (String k : replaceMap.keySet()) {
			str = str.replace(k, replaceMap.get(k));
		}
		return str;
	}

	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	public static String randomString() {
		return randomString(32);
	}
	
	public static String randomString(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	public static boolean match(String matchAgainst, String context) {
		if (matchAgainst == null)
			return false;
		String[] chars = matchAgainst.split("");
		String[] tchars = context.split("");
		int tcharsOff = 0;
		for (int i = 0; i < chars.length; i++) {
			String s = chars[i];
			if (s.equals("*")) {
				if (i != chars.length - 1) {
					String cNext = chars[i + 1];
					for (int j = i; j < tchars.length; j++) {
						String tC = tchars[j];
						if (tC.equals(cNext) && tchars[j + 1].equals(chars[i + 2])) {
							tcharsOff = j - i - 1;
							break;
						}
					}
					continue;
				} else {
					return true;
				}
			}
			if (!s.equals(tchars[i + tcharsOff])) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean containsAny(String str, String[] arr) {
		for (String string : arr) {
			if(str.contains(string))
				return true;
		}
		return false;
	}

}
