package org.xbf.core.Exceptions;

public class HandlerLoadingFailed extends RuntimeException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6030871936084066271L;
	
	ReflectiveOperationException ex;
	
	public HandlerLoadingFailed(ReflectiveOperationException e) {
		ex = e;
	}
	
	@Override
	public synchronized Throwable getCause() {
		return ex;
	}

}
