package org.xbf.core.Utils.Time;

import java.util.Date;

public class TimeSync {

	public static void timeSyncToMinute() {
		try {
			long secondsToWait = 60 - new Date().getSeconds();
			Thread.sleep(secondsToWait * 1000);
		} catch (Exception e) {
		}
	}

}
