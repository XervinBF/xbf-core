package org.xbf.core.Models;

import org.xbf.core.Data.SmartTable;
import org.xbf.core.Data.SmartTableObject;
import org.xbf.core.Data.Annotations.IncludeAll;

@IncludeAll
public class RunningService extends SmartTableObject{

	public RunningService() {
		super("XServices");
	}
	
	public String serviceName;
	public int serviceId;
	public String args;
	
	public static SmartTable<RunningService> getSmartTable() {
		return new SmartTable<RunningService>("XServices", RunningService.class);
	}

}
