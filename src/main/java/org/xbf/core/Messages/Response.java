package org.xbf.core.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.xbf.core.DBConfig;
import org.xbf.core.Utils.Language.Dict;

public class Response {

	public List<RichResponse> responses;
	public ResponseDestination destination;
	Dict dict;
	public String text;
	public boolean removeMessage;
	public boolean editMessage;
	public long messageToEdit;
	public long channelIdOfEditMessage = -1;
	public long responseId = new Random().nextLong();
	
	public Response(Request req) {
		this(null, ResponseDestination.SAME_CHANNEL, req.user.getDict());
	}
	
	public Response(Dict dict) {
		this(null, ResponseDestination.SAME_CHANNEL, dict);
	}
	
	public Response(ResponseDestination destination, Request req) {
		this(null, destination, req.user.getDict());
	}
	
	public Response(ResponseDestination destination, Dict dict) {
		this(null, destination, dict);
	}
	
	public Response(List<RichResponse> responses, ResponseDestination destination, Request req) {
		this(responses, destination, req.user.getDict());
	}
	
	public Response(List<RichResponse> responses, ResponseDestination destination, Dict dict) {
		this.responses = new ArrayList<RichResponse>();
		if(responses != null) {
			for (RichResponse richResponse : responses) {
				this.responses.add(richResponse);
			}
		}
		this.destination = destination;
		
		this.dict = dict;
		if(this.dict == null) {
			this.dict = new Dict(DBConfig.getDefaultLang());
		}
	}
	
	/**
	 * Adds a {@link org.xbf.core.Messages.RichResponse RichResponse} to the current response, the language is applied when this is added
	 * @param response A {@link org.xbf.core.Messages.RichResponse RichResponse}
	 * @return The current instance of {@link org.xbf.core.Messages.Response Response}
	 */
	public Response addRichResponse(RichResponse response) {
		RichResponse r = new RichResponse(dict.partTransform(response.title));
		for (Pair<String, String> k : response.fields) {
			r.addField(dict.partTransform(k.getKey()), dict.partTransform(k.getValue()));
		}
		
		for (Pair<String, String> k : response.commands) {
			r.addCommand(dict.partTransform(k.getKey()), dict.partTransform(k.getValue()));
		}
		if(response.footer != null)
			r.footer = dict.partTransform(response.footer);
		if(response.description != null)
			r.description = dict.partTransform(response.description);
		r.color = response.color;
		responses.add(r);
		return this;
	}
	
	public Response setText(String text) {
		this.text = text;
		return this;
	}
	
}
