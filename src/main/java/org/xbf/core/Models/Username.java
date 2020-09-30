package org.xbf.core.Models;

import org.xbf.core.Data.SmartTableObject;
import org.xbf.core.Data.Annotations.IncludeAll;

@IncludeAll
public class Username extends SmartTableObject{

	public Username() {
		super("UserNames");
	}
	
	public String name;
	public String provider;
	
}
