package org.xbf.core;

import java.awt.Color;

import org.slf4j.LoggerFactory;
import org.xbf.core.ChatHandlers.ChatHandler;
import org.xbf.core.ChatHandlers.ChatHandlerResult;
import org.xbf.core.Messages.Pair;
import org.xbf.core.Messages.Request;
import org.xbf.core.Messages.Response;
import org.xbf.core.Messages.ResponseDestination;
import org.xbf.core.Messages.RichResponse;
import org.xbf.core.Module.Command;
import org.xbf.core.Module.Module;
import org.xbf.core.Permissions.PermissionRegistry;
import org.xbf.core.Plugins.Handler;
import org.xbf.core.Utils.Arrays.ArrayUtils;

import ch.qos.logback.classic.Logger;

public class CommandProcessor {

	public static boolean loading = true;

	public static double execTime = 0;

	static Logger l = (Logger) LoggerFactory.getLogger(CommandProcessor.class);
	

	public static void runAsyncRequest(Request req) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				runRequest(req);

			}
		}, getGenericRequestThreadName(req));
		t.start();
	}

	public static String getGenericRequestThreadName(Request req) {
		if (req == null)
			return "FW.Async.NullRequest";

		if (req.user == null)
			return "FW.Async." + req.source + "." + req.message;

		return "FW.Async." + req.source + "." + req.message + "." + req.user.id;
	}

	public static Response runRequest(Request req) {
		try {
		long startTime = System.currentTimeMillis();
		String prefix = req.user.getConfig("framework.prefix", DBConfig.getPrefix());
		if(!req.message.startsWith(prefix)) {
			req.time.start("Framework.ChatHandlers");
			for (ChatHandler chatHandler : XBF.getChatHandlers()) {
				try {
				ChatHandlerResult res = chatHandler.HandleMessage(req);
				if(res == null) continue;
				if(res.hasResponse()) {
					return res.response;
				}
				} catch (Exception ex) {
					l.error("Exception from chathandler '" + chatHandler.getClass().getSimpleName() + "'");
					ex.printStackTrace();
				}
			}
		}
		
		req.time.start("Framework.Precompute");
		Response res = handle(req);
		req.time.stop();
		if (res == null)
			return null;
		if(req.user.hasPermission("framework.timings") && req.user.getConfig("framework.timings", "false").equals("true")) {
			RichResponse rr = new RichResponse("Timings");
			for (Pair<String, Long> k : req.timings) {
				rr.addField(k.getKey(), k.getValue() + " ms");
			}
			rr.footer = XBF.getConfig().BOT_NAME + " Timings";
			res.addRichResponse(rr);
		}
		for (RichResponse rre : res.responses) {
			for (Pair<String, String> p : rre.commands) {
				p.setValue(prefix + p.getValue());
			}
		}
		
		execTime = execTime * 0.8 + (System.currentTimeMillis() - startTime) * 0.2;
		
		return res;
		} catch(Exception ex) {
			l.error("Execution error", ex);
			return null;
		}
	}

	private static Response handle(Request req) {
		if (req.user == null)
			return null;
		l.info(req.providerName + "@" + req.source + ": " + req.message);

		String[] args = req.message.split(" ");
		String prefix = req.user.getConfig("framework.prefix", DBConfig.getPrefix());
		if (!req.message.startsWith(prefix)) {
			boolean noPrefixCommand = false;
			for (Module m : XBF.getModules()) {
				for (Command cmd : m.commands) {
					if(cmd.noPrefixCommand != null && req.message.startsWith(cmd.noPrefixCommand)) {
						String[] newArgs = new String[args.length + 1];
						for (int i = 0; i < args.length; i++) {
							newArgs[i + 1] = args[i];
						}
						newArgs[1] = newArgs[1].substring(cmd.noPrefixCommand.length());
						newArgs[0] = cmd.noPrefixCommand;
						args = newArgs;
						prefix = "";
						noPrefixCommand = true;
					}
				}
			}
			if(!noPrefixCommand)
				return null;
		}

		String command = args[0].substring(prefix.length());

		boolean noPermissions = false;
		
		for (Module m : XBF.getModules()) {
			for (Command cmd : m.commands) {
				if (cmd.command.equals(command) || (cmd.noPrefixCommand != null && cmd.noPrefixCommand.equals(command))) {
					if(PermissionsManager.hasPermission(cmd, req)) {
						try {
							String[] arguments = (String[]) ArrayUtils.removeUntil(args, 1);
							req.time.start("Framework.ExecuteCommand");
							Response r = cmd.Handle(arguments, req);
							req.time.stop();
								return r;
						} catch (Exception ex) {
							ex.printStackTrace();
							return new Response(req).addRichResponse(new RichResponse("[framework.error.title]")
									.setDescription(String.format(req.user.getDict().getString("framework.error.desc"),
											ex.getClass().getSimpleName()))
									.addField("[framework.error.message]", ex.getMessage()).setColor(Color.RED));
						}
					} else {
						noPermissions = true;
					}
				}
			}
		}
		
		if(noPermissions)
			return new Response(null, ResponseDestination.SAME_CHANNEL, req)
					.addRichResponse(
							new RichResponse("[framework.perms.title]")
							.setDescription("[framework.perms.desc]")
							.setColor(Color.RED)
							);
		
		// 
		System.out.println("Empty response");
		return null;

	}
	


	public static Command getCommand(String command) {
		for (Module m : XBF.getModules()) {
			for (Command cmd : m.commands) {
				if (cmd.command.equalsIgnoreCase(command))
					return cmd;
			}
		}
		return null;
	}

	public static Module getModule(String command) {
		for (Module m : XBF.getModules()) {
			for (Command cmd : m.commands) {
				if (cmd.command.equalsIgnoreCase(command))
					return m;
			}
		}
		return null;
	}

	public static void startHandlers() {
		for (Handler handler : XBF.getHandlers()) {
			handler.start();
		}

	}

	public static void stopHandlers() {
		for (Handler handler : XBF.getHandlers()) {
			handler.stop();
		}
	}

	
	public static void loadPermissions() {
		PermissionRegistry.regPerms("framework.timings");
		PermissionRegistry.regPerms("experimental.*", "experimental.wit");
	}
	
}
