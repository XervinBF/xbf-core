package org.xbf.core;

import java.util.Optional;

import org.xbf.core.Messages.Request;
import org.xbf.core.Module.Command;
import org.xbf.core.Module.Module;
import org.xbf.core.Module.Restrictions.RestrictTo;
import org.xbf.core.Module.Restrictions.Restriction;

public class PermissionsManager {

	
	
	
	public static boolean hasPermission(Command c, Request r) {
		if(c.basePermission != null) {
			return r.user.hasPermission(c.basePermission);
		}
		Optional<Module> opm = XBF.getModuleForCommand(c.command);
		if(!opm.isPresent()) return false;
		Module m = opm.get();

		if(m.getRestrictions() != null && m.getRestrictions().size() != 0) {
			boolean any = false;
			for (Restriction rest : m.getRestrictions()) {
				if(restrictionCheck(rest, r))
					any = true;
			}
			if(!any)
				return false;
		}
		
		if(c.getRestrictions() != null && c.getRestrictions().size() != 0) {
			boolean any = false;
			for (Restriction rest : c.getRestrictions()) {
				if(restrictionCheck(rest, r))
					any = true;
			}
			if(!any)
				return false;
		}
		
		return true;
	}
	
	public static boolean restrictionCheck(Restriction r, Request req) {
		if(r.r == RestrictTo.PRIVATE) {
			if(!req.server.equals("")) { // Is channel private
				return false;
			}
		}
		else if(r.r == RestrictTo.CHANNEL) {
			if(!req.channel.equals(r.data))
				return false;
		} else if(r.r == RestrictTo.SOURCE) {
			if(!req.source.equalsIgnoreCase(r.data))
				return false;
		} else if(r.r == RestrictTo.IMPERSONATED) {
			if(!req.user.isImpersonated) return false;
		}
		return true;
	}

	
}
