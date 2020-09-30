package org.xbf.core.Exceptions;

public class AnnotationNotPresent extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5005914272643397712L;

	public String annotation;
	
	public AnnotationNotPresent(String annotation) {
		this.annotation = annotation;
	}

	@Override
	public String getMessage() {
		return "The " + annotation + " annotation was not present";
	}
	
}
