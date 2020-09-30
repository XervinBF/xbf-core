package org.xbf.core.Forms;

import java.util.ArrayList;

import org.xbf.core.Messages.Request;
import org.xbf.core.Models.XUser;

public class FormManager {

	public static ArrayList<Form> forms = new ArrayList<>();
	
	public static Form createForm(String channel, XUser usr) {
		return createForm(null, channel, usr);
	}
	
	public static Form createForm(String name, String channel, XUser usr) {
		Form f = new Form(name);
		f.channel = channel;
		f.user = usr;
		forms.add(f);
		return f;
	}
	
	public static Form getForm(String channelId, Request req) {
		for (Form form : forms) {
			if(req.source.equals("msteams") && form.user.id == req.user.id) return form;
			if((form.channel == null || form.channel.equals(channelId)) && form.user.id == req.user.id) return form;
		}
		return null;
	}

	public static void endForm(Form form) {
		int index = forms.indexOf(form);
		if(index != -1)
			forms.remove(index);
	}
	
}
