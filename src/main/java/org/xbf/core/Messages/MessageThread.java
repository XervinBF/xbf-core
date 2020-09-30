package org.xbf.core.Messages;

import java.util.ArrayList;

import org.xbf.core.Data.SmartTable;
import org.xbf.core.Data.SmartTableObject;
import org.xbf.core.Data.Annotations.IncludeAll;
import org.xbf.core.Utils.Map.FastMap;

@IncludeAll
/**
 * A Message Subscription
 * @author BL19
 *
 */
public class MessageThread extends SmartTableObject {

	public MessageThread() {
		super("MessageThreads");
	}
	
	public String name;
	public int id;
	public int uid;
	
	public static SmartTable<MessageThread> getSmartTable() {
		return new SmartTable<MessageThread>("MessageThreads", MessageThread.class);
	}
	
	public static ArrayList<Integer> getRecipients(String thread) {
		ArrayList<Integer> lng = new ArrayList<Integer>();
		for (MessageThread t : getSmartTable().getMultiple(new FastMap<String, String>()
				.add("name", thread))) {
			lng.add(t.uid);
		}
		return lng;
	}
	
}
