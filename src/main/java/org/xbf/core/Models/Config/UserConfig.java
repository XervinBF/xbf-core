package org.xbf.core.Models.Config;

import org.xbf.core.Data.SmartTable;
import org.xbf.core.Data.SmartTableObject;
import org.xbf.core.Data.Annotations.IncludeAll;

@IncludeAll
public class UserConfig extends SmartTableObject {

	public UserConfig() {
		super("UserConfig");
	}

	public int userId;
	public String configKey;
	public String configValue;
	
	public static SmartTable<UserConfig> getSmartTable() {
		return new SmartTable<UserConfig>("UserConfig", UserConfig.class);
	}
	
}
