package org.xbf.core.Utils.Language;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.LoggerFactory;
import org.xbf.core.DBConfig;

import ch.qos.logback.classic.Logger;

public class Dict {

	public static boolean forceDefault;

	Properties p;

	Logger logger;

	String lang;

//    Translate translate = TranslateOptions.getDefaultInstance().getService();

	public Dict(String string) {
		p = new Properties();
		loadDict(string);

	}

	private void loadDict(String str) {
		lang = str;
		logger = (Logger) LoggerFactory.getLogger("Dict." + str);
		if (forceDefault)
			str = DBConfig.getDefaultLang();

		p = new Properties();
		for (String s : DictionaryLoader.load(str)) {
			p = new Properties(p);
			try {
				p.load(new StringReader(s));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileInputStream is = new FileInputStream("locals/" + str + ".lang");
			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
			p.load(isr);
		} catch (Exception e) {
			// Google Translate
//			String dflt = Config.getDefaultLang();
//			if(str.equals(dflt))
//				return;
//			loadDict(dflt);
		}
		lang = str;
	}
	
	public String partTransform(String string) {
		if(string == null) return null;
		if(!string.contains("[")) return string;
		String[] a = string.split(" ");
		List<String> r = new ArrayList<String>();
		String built = "";
		String temp = string;
		while(true) {
			if(!temp.contains("[") || !temp.contains("]")) break;
			int index = temp.indexOf("[");
			String before = temp.substring(0, index);
			String after = temp.substring(index + 1);
			int endIndex = after.indexOf("]");
			String data = after.substring(0, endIndex);
			after = after.substring(endIndex + 1);
			temp = after;
			String str = getString(data);
			if(str != null) {
				before += str;
			} else {
				before += "[" + data + "]";
			}
			built += before;
		}
		built += temp;
		return built;
//		for (String b : a) {
//			if(b.startsWith("[") && b.endsWith("]")) {
//				
//				String c = b.replace("[", "").replace("]", "");
//				String str = getString(c);
//				if(str == null) {
//					r.add(b);
//					continue;
//				}
//				
//				r.add(str);
//				
//			} else {
//				r.add(b);
//			}
//		}
//		return String.join(" ", r);
	}

	public String getStringa(String str) {
		if (p.getProperty("parent") != null)
			str = new Dict(p.getProperty("parent")).getString(str);
		if (p.getProperty("generator") != null)
			return generator(str);
		String s = p.getProperty(str);
		if (s == null && !lang.equals(DBConfig.getDefaultLang()))
			return getDefault(str);
		return s;
	}

	private String getDefault(String str) {
//		if (Config.getConfig("dict.auto").equals("true"))
//			return translateReplace(str);
		return new Dict(DBConfig.getDefaultLang()).getString(str);
	}

//	private String translate(String str) {
//		Translation tr = translate.translate(new Dict("en").getString(str), TranslateOption.sourceLanguage("en"), TranslateOption.targetLanguage(lang));
//		
//		return tr.getTranslatedText();
//	}

//	private String translateReplace(String str) {
//		String orig = new Dict("en").getString(str);
//		if(lang.equalsIgnoreCase("en")) return orig;
//		String translate = null;
//		try {
//			String[] trans = GTranslate.translate("en", lang, orig);
//			if(trans.length == 0) translate = null;
//			if(trans.length != 0)
//				translate = trans[0];
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		if (p == null)
//			p = new Properties();
//		if (translate == null)
//			return orig;
//		p.setProperty(str, translate);
//		saveTranslations();
//		return translate;
//	}

	private void saveTranslations() {
		try {
			FileOutputStream fos = new FileOutputStream("locals/" + lang + ".lang");
			OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
			p.store(writer, "Auto-Saved");
			writer.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getString(String str) {
		String r = getStringa(str);
		if (DBConfig.DEV)
			logger.debug(str + ": " + r);
		return r;
	}

	private String generator(String str) {
		String generator = p.getProperty("generator");
		return null;
	}

}
