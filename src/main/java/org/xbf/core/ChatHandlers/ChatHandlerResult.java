package org.xbf.core.ChatHandlers;

import org.xbf.core.DBConfig;
import org.xbf.core.Messages.Response;
import org.xbf.core.Utils.Language.Dict;

public class ChatHandlerResult {

	public ChatHandlerResult() {

	}

	public ChatHandlerResult(boolean keepOriginal) {
		this(null, keepOriginal);
	}

	public ChatHandlerResult(Response res) {
		this(res, true);
	}

	public ChatHandlerResult(Response res, boolean keepOriginal) {
		if (res == null) {
			res = new Response(new Dict(DBConfig.getDefaultLang()));
		}
		response = res;
		response.removeMessage = !keepOriginal;
	}

	public Response response;

	public boolean hasResponse() {
		if(response == null) {
			return false;
		}
		if(response.text != null) {
			System.out.println("Has Text");
			return true;
		}
		if(response.responses != null && response.responses.size() != 0) {
			System.out.println("Responses: " + response.responses.size());
			return true;
		}
		if(response.removeMessage) {
			return true;
		}
		return false;
	}

}
