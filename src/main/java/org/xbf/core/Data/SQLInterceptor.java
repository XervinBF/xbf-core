package org.xbf.core.Data;

public class SQLInterceptor {

	public void interceptStatement(String sql) {}
	
	public StackTraceElement parentCaller() {
		int index = 5;
		StackTraceElement st = Thread.currentThread().getStackTrace()[index];
		String className = getSimpleClassName(st.getClassName());
		while(className.equalsIgnoreCase("SmartTable")) {
			st = Thread.currentThread().getStackTrace()[++index];
			className = getSimpleClassName(st.getClassName());
		}
		return st;
	}
	
	public String getSimpleClassName(String fullName) {
		String[] splt = fullName.split("\\.");
		if(splt.length == 0) return "";
		return splt[splt.length - 1];
	}
	
}
