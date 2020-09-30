package org.xbf.core.Data;

public class SmartTableException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5568522544140468380L;

	public String message;
	
	public SmartTableException(String message) {
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
}
