package org.xbf.core.Module;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.LoggerFactory;
import org.xbf.core.Messages.Request;
import org.xbf.core.Messages.Response;
import org.xbf.core.Messages.RichResponse;
import org.xbf.core.Module.Annotations.CommandAlias;
import org.xbf.core.Module.Annotations.CommandName;
import org.xbf.core.Module.Annotations.CommandPermission;
import org.xbf.core.Module.Annotations.WithRestriction;
import org.xbf.core.Module.Restrictions.Restriction;

import ch.qos.logback.classic.Logger;

public class Command {
	
	public Command() {
		this.command = this.getClass().getSimpleName().replace("Command", "").toLowerCase();
		if(getClass().isAnnotationPresent(CommandName.class)) {
			this.command = getClass().getAnnotation(CommandName.class).name();
			
		}
		if(getClass().isAnnotationPresent(CommandAlias.class)) {
			for (CommandAlias alias : getClass().getAnnotationsByType(CommandAlias.class)) {
				this.aliases.add(alias.name());
			}
		}
		
		if(getClass().isAnnotationPresent(CommandPermission.class)) {
			basePermission = getClass().getAnnotation(CommandPermission.class).permission();
		}
	}
	
	public Command(LambdaCommand lambda) {
		this.lambdaCommand = lambda;
	}

	public Logger l = (Logger) LoggerFactory.getLogger(this.getClass());
	
	public String command;
	public ArrayList<String> aliases = new ArrayList<>();
	
	public String noPrefixCommand;

	public String help;
			
	public String basePermission = null;
	
	public boolean enabled = true;
	
	LambdaCommand lambdaCommand;
	
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
		
	public Response Handle(String[] args, Request req) {
		if(lambdaCommand != null)
			return lambdaCommand.apply(args, req);
		l.warn("Command handler not specified");
		return new Response(req);
	}
	
	public Response UnknownArgument(String argument, Request req) {
		return new Response(req).addRichResponse(new RichResponse("[generic.arg.unknown] '" + argument + "'").setColor(Color.RED));
	}
	
	public Response TooFewArguments(String argument, int required, Request req) {
		return new Response(req).addRichResponse(new RichResponse("'" + argument + "' requires '" + required + "' arguments").setColor(Color.RED));
	}
	
	public Response NoPermission(Request req) {
		return new Response(req).addRichResponse(new RichResponse("[framework.perms.title]").setDescription("[framework.perms.desc]").setColor(Color.RED));
	}
	
}
