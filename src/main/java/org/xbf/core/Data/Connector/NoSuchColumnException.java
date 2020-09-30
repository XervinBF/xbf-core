package org.xbf.core.Data.Connector;

public class NoSuchColumnException extends RuntimeException {

	String message;
	
	public NoSuchColumnException(String message) {
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
	
}
