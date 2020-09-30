package org.xbf.core.Module.Restrictions;

public class Restriction {

	public RestrictTo r;
	public String data;
	
	
	public Restriction(RestrictTo r, String data) {
		this.r = r;
		this.data = data;
	}
	
	public Restriction(RestrictTo r) {
		this(r, null);
	}
	
	
	
}




