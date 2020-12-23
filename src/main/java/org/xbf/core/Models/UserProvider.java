package org.xbf.core.Models;

import org.xbf.core.Data.SmartTable;
import org.xbf.core.Data.SmartTableObject;
import org.xbf.core.Data.Annotations.IncludeAll;
import org.xbf.core.Utils.Map.FastMap;
import org.xbf.core.Utils.String.StringUtils;

@IncludeAll
public class UserProvider extends SmartTableObject {

	public UserProvider() {
		super("UserProviders");
	}
	
	public String uid;
	public String provider;
	public String extid;
	public String name;
	
	public static SmartTable<UserProvider> getSmartTable() {
		return new SmartTable<UserProvider>("UserProviders", UserProvider.class);
	}
	
	public static synchronized void setName(String extid, String provider, String name) {
		UserProvider p = UserProvider.getSmartTable().get(new FastMap<String, String>()
				.add("extid", extid + "")
				.add("provider", provider));
		p.name = name;
		getSmartTable().set(p);
	}
	
}
