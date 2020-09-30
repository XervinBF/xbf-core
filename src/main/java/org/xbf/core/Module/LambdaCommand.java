package org.xbf.core.Module;

import org.xbf.core.Messages.Request;
import org.xbf.core.Messages.Response;

@FunctionalInterface
public interface LambdaCommand {
	public Response apply(String[] args, Request req);
}
