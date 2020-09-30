package org.xbf.core.Utils.Timings;

import org.xbf.core.Messages.Request;

public class Stopwatch {

	long start;
	long end;
	long time;
	boolean running;
	String name;
	Request req;
	
	public Stopwatch() {
		// TODO Auto-generated constructor stub
	}
	
	public Stopwatch(Request req) {
		this.req = req;
	}
	
	public static Stopwatch startnew(String name) {
		return new Stopwatch().start(name);
	}
	
	public Stopwatch start(String name) {
		if(running) stop();
		this.name = name;
		start = System.currentTimeMillis();
		running = true;
		return this;
	}
	
	public Stopwatch stop() {
		if(!running) return this;
		running = false;
		end = System.currentTimeMillis();
		time = end - start;
//		new Logger(this.getClass()).debug("Timings - " + name + " ran for " + time + "ms");
		TimingsEngine.RecordTimings(name, time);
		if(req != null) {
			req.logTimings(name, time);
		}
		return this;
	}
	
	public long getTime() {
		if(running)
			return System.currentTimeMillis() - start;
		else
			return time;
	}
	
}
