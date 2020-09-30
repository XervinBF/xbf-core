package org.xbf.core.ChatHandlers;

import java.util.function.Function;

import org.xbf.core.Messages.Request;

public class ChatHandler {

	public ChatHandler() {
		
	}
	
	Function<Request, ChatHandlerResult> action;
	
	public ChatHandler(Function<Request, ChatHandlerResult> action) {
		this.action = action;
	}

	public ChatHandlerResult HandleMessage(Request req) {
		if(action != null)
			return action.apply(req);
		return new ChatHandlerResult();
	}
	
	public ChatHandlerResult BuildResult(boolean keepMessage) {
		return new ChatHandlerResult(keepMessage);
	}
	
}
