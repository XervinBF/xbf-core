package org.xbf.core.Models.Config;

import org.xbf.core.Data.SmartTable;
import org.xbf.core.Data.SmartTableObject;
import org.xbf.core.Data.Annotations.IncludeAll;


@IncludeAll
public class XervinConfig extends SmartTableObject {

	public XervinConfig() {
		super("XConfig");
	}

	public String configKey;
	public String configValue;
	
	public static SmartTable<XervinConfig> getSmartTable() {
		return new SmartTable<XervinConfig>("XConfig", XervinConfig.class);
	}
	
}
