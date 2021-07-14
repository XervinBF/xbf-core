package org.xbf.core.Plugins;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.LoggerFactory;
import org.xbf.core.Messages.Response;
import org.xbf.core.Models.XUser;

import ch.qos.logback.classic.Logger;

public class Handler {

	Logger l = (Logger) LoggerFactory.getLogger(this.getClass());
	
	boolean handleState;
	
	public void setState(boolean state) {
		handleState = state;
	}
	
	public XHandler getAnnotation() {
		return getClass().getAnnotation(XHandler.class);
	}
	
	public XUser getUserForCurrentProvider(String id, String name) {
		return XUser.getFromProvider(id, getAnnotation().providerName(), name);
	}
	
	public void start() {
		l.info("Integration running!");
	}
	
	public void stop() {
		l.info("Integration stopped!");
	}
	
	public void sendMessage(String userid, Response message) {
		throw new NotImplementedException("sendMessage not implemented for " + getAnnotation().providerName() + " provider");
	}
	
	public void sendMessageToChannel(String channelId, Response message) {
		throw new NotImplementedException("sendMessageToChannel not implemented for " + getAnnotation().providerName() + " provider");
	}
	
	public List<String> getProviderGroups(int xbfUserId) {
		throw new NotImplementedException("getProviderGroups not implemented for " + getAnnotation().providerName() + " provider");
	}
	
}
