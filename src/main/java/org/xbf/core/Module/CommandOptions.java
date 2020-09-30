package org.xbf.core.Module;

import java.util.ArrayList;

import org.xbf.core.Module.Restrictions.Restriction;

public class CommandOptions {

	public String command;
	public ArrayList<String> aliases = new ArrayList<>();
	
	public String noPrefixCommand;

	public String help;
			
	public String basePermission = null;
	
	public boolean enabled = true;
	
	public ArrayList<Restriction> restrictions = new ArrayList<>();
	
	public CommandOptions setCommand(String command) {
		this.command = command;
		return this;
	}
	
	public CommandOptions addAlias(String alias) {
		this.aliases.add(alias);
		return this;
	}
	
	public CommandOptions setNoPrefix(String noPrefixCommand) {
		this.noPrefixCommand = noPrefixCommand;
		return this;
	}
	
	public CommandOptions setHelp(String help) {
		this.help = help;
		return this;
	}
	
	public CommandOptions setPermission(String permission) {
		this.basePermission = permission;
		return this;
	}
	
	public CommandOptions setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}
	
	public CommandOptions addRestriction(Restriction restr) {
		restrictions.add(restr);
		return this;
	}
	
}
