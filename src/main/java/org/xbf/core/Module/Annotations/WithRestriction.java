package org.xbf.core.Module.Annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.xbf.core.Module.Restrictions.RestrictTo;

@Retention(RUNTIME)
@Target(TYPE)
public @interface WithRestriction {

	RestrictTo restrictedTo();
	
	String data();
	
}
