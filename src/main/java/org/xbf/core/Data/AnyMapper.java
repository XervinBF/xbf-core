package org.xbf.core.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.xbf.core.Models.XUser;
import org.xbf.core.Module.Command;

import com.google.gson.Gson;

public class AnyMapper {

	public static Object mapObject(Object obj, Class<?> cls) {
//		System.out.println("Mapping: " + obj + ", to " + cls.getSimpleName());
		if(obj != null && cls.isAssignableFrom(obj.getClass())) {
			return obj;
		}
		if(Integer.class.isAssignableFrom(cls)) {
			if(obj == null) return -1;
			if(obj instanceof BigDecimal)
				return ((BigDecimal) obj).intValue();
			if(obj instanceof BigInteger)
				return ((BigInteger) obj).intValue();
			if(obj instanceof Integer)
				return (int) obj;
			try {
				return Integer.parseInt((obj + "").trim());
			} catch (Exception ex) {
				return (int) obj;
			}
		}
		if(Long.class.isAssignableFrom(cls)) {
			if(obj == null) return -1;
			if(obj instanceof BigDecimal)
				return ((BigDecimal) obj).longValue();
			if(obj instanceof BigInteger)
				return ((BigInteger) obj).longValue();
			if(obj instanceof Long)
				return (int) obj;
			try {
				return Long.parseLong((obj + "").trim());
			} catch (Exception ex) {
				return (long) obj;
			}
		}
		if(Double.class.isAssignableFrom(cls)) {
			if(obj == null) return -1;
			if(obj instanceof BigDecimal)
				return ((BigDecimal) obj).doubleValue();
			if(obj instanceof BigInteger)
				return ((BigInteger) obj).doubleValue();
			if(obj instanceof Double)
				return (int) obj;
			try {
				return Double.parseDouble((obj + "").trim());
			} catch (Exception ex) {
				return (double) obj;
			}
		}
		if(Boolean.class.isAssignableFrom(cls)) {
			if(obj == null) return false;
			if(obj instanceof Boolean)
				return obj;
			if(isNumber(obj + "")) {
				if(!(obj instanceof String)) {
					obj = mapObject(obj, String.class);
				}
				obj = mapObject(obj, Integer.class);
				obj = (int) obj == 1;
			}
			try {
				return Boolean.parseBoolean((obj + "").trim());
			} catch (Exception ex) {				
				return obj;
			}
		}
		
		if(String.class.isAssignableFrom(cls)) {
			return obj + "";
		}
		return new Gson().fromJson(new Gson().toJson(obj), cls);
	}

	public static void main(String[] args) {
		System.out.println(mapObject("Well idono", String.class));
		System.out.println(mapObject("415", Long.class));
		System.out.println(mapObject(new BigInteger("857719273981639278792873992"), Long.class));
		System.out.println(mapObject("1", Boolean.class));
		System.out.println(mapObject("0", Boolean.class));
		System.out.println(mapObject(1, Boolean.class));
		System.out.println(mapObject(0, Boolean.class));
		System.out.println(mapObject("true", Boolean.class));
		System.out.println(mapObject("false", Boolean.class));
		System.out.println(mapObject(true, Boolean.class));
		System.out.println(mapObject(false, Boolean.class));
		
		System.out.println(mapObject(new DBConnector(), DBConnector.class));
		System.out.println(mapObject(new DBConnector(), NXDBConnector.class)); // Uses Gson re-mapping
	}
	
	static boolean isNumber(String str) {
		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
	
	
}
