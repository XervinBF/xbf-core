package org.xbf.core.Exceptions;

public class HandlerLoadingFailed extends Exception {

	
	ReflectiveOperationException ex;
	
	public HandlerLoadingFailed(ReflectiveOperationException e) {
		ex = e;
	}
	
	@Override
	public synchronized Throwable getCause() {
		return ex;
	}

}
