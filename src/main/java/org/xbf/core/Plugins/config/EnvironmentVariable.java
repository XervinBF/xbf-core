package org.xbf.core.Plugins.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(ElementType.FIELD)
/**
 * Uses the specified environment variable if found
 * @author BL19
 *
 */
public @interface EnvironmentVariable {

	public abstract String value();
	
}
