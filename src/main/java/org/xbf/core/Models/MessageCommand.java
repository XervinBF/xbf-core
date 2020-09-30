package org.xbf.core.Models;

import org.xbf.core.Data.SmartTable;
import org.xbf.core.Data.SmartTableObject;
import org.xbf.core.Data.Annotations.IncludeAll;

@IncludeAll
public class MessageCommand extends SmartTableObject{

	public MessageCommand() {
		super("MessageCommands");
	}
	
	public long messageId;
	public String reactionEmote;
	public String command;
	public String source;
	
	public static SmartTable<MessageCommand> getSmartTable() {
		return new SmartTable<MessageCommand>("MessageCommands", MessageCommand.class);
	}
	
}
