package org.xbf.core.Permissions.DataModels;

public class Permission {

	public String type;
	public String key;
	public boolean value;
	
	public Permission(String key, boolean value) {
		type = "permission";
		if(key.startsWith("group."))
			type = "inheritance";
		this.key = key;
		this.value = value;
	}
	
}
