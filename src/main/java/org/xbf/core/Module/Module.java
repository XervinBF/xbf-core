package org.xbf.core.Module;

import java.util.ArrayList;
import java.util.List;

import org.xbf.core.Module.Annotations.CommandName;
import org.xbf.core.Module.Annotations.CommandPermission;
import org.xbf.core.Module.Annotations.WithRestriction;
import org.xbf.core.Module.Restrictions.Restriction;
import org.xbf.core.Permissions.PermissionRegistry;

public class Module {

	public ArrayList<Command> commands = new ArrayList<>();
	
	public String name;
	
	public Restriction[] restrictions = new Restriction[0];
	
	
	public List<Restriction> getRestrictions() {
		ArrayList<Restriction> restr = new ArrayList<Restriction>();
		for (Restriction restriction : restrictions) {
			restr.add(restriction);
		}
		
		if(getClass().isAnnotationPresent(WithRestriction.class)) {
			for (WithRestriction rest : getClass().getAnnotationsByType(WithRestriction.class)) {
				Restriction r = new Restriction(rest.restrictedTo(), rest.data());
				restr.add(r);
			}
		}
		return restr;
	}
	
	public Command registerCommand(Command command) {
		if(command.getClass().isAnnotationPresent(CommandPermission.class)) {
			command.basePermission = command.getClass().getAnnotation(CommandPermission.class).permission();
		}
		if(command.getClass().isAnnotationPresent(CommandName.class)) {
			command.command = command.getClass().getAnnotation(CommandName.class).name();
		}
		if(command.basePermission != null)
			PermissionRegistry.regPerms(command.basePermission);
		commands.add(command);
		return command;
	}
	
	public Command registerCommand(String name, LambdaCommand action) {
		return registerCommand(name, action, null);
	}
	
	public Command registerCommand(String name, LambdaCommand action, CommandOptions options) {
		Command cmd = new Command(action);
		cmd.command = name;
		if(options != null) {
			cmd.aliases = options.aliases;
			cmd.basePermission = options.basePermission;
			if(options.command != null)
				cmd.command = options.command;
			cmd.enabled = options.enabled;
			cmd.noPrefixCommand = options.noPrefixCommand;
		}
		return registerCommand(cmd);
	}
	
}
