package org.xbf.core.Messages;

import java.util.ArrayList;

import org.xbf.core.Models.XUser;
import org.xbf.core.Utils.Timings.Stopwatch;

/**
 * A request for the {@link org.xbf.core.CommandProcessor CommandProcessor}
 * @see org.xbf.core.Plugins.Handler
 * @author BL19
 *
 */
public class Request {

	/**
	 * The XBF User
	 */
	public XUser user;
	/**
	 * The message
	 */
	public String message;
	/**
	 * The channel that the message was sent in
	 */
	public String channel;
	/**
	 * The server the request was sent from,
	 * if no server then empty string.
	 */
	public String server = "";
	/**
	 * The Message Provider that issued the request
	 */
	public String source;
	/**
	 * Timings recorder
	 */
	public Stopwatch time = new Stopwatch(this);
	/**
	 * Original message id
	 */
	public String origid;
	/**
	 * The provider name
	 */
	public String providerName;
	/**
	 * Provider specific data
	 */
	public RequestSourceData data;
	
	/**
	 * The timings
	 */
	public ArrayList<Pair<String, Long>> timings = new ArrayList<Pair<String, Long>>();
	
	/**
	 * Logs timings
	 * @param name The name of the timing
	 * @param time The time it took in Milliseconds
	 */
	public void logTimings(String name, long time) {
		timings.add(new Pair<String, Long>(name, time));
	}
	
}
