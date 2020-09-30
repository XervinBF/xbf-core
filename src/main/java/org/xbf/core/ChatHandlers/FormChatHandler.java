package org.xbf.core.ChatHandlers;

import org.xbf.core.Forms.Form;
import org.xbf.core.Forms.FormManager;
import org.xbf.core.Messages.Request;
import org.xbf.core.Messages.Response;
import org.xbf.core.Messages.RichResponse;

public class FormChatHandler extends ChatHandler {

	@Override
	public ChatHandlerResult HandleMessage(Request req) {
		Form f = FormManager.getForm(req.channel, req);
		if(f == null) return new ChatHandlerResult();
		RichResponse r = f.handleAnswer(req.message);
		if(r == null) return new ChatHandlerResult();
		return new ChatHandlerResult(new Response(req).addRichResponse(r));
	}
	
}
