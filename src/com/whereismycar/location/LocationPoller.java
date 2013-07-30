package com.whereismycar.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * BroadcastReceiver to be launched by AlarmManager. Simply passes the work over to LocationPollerService, who arranges
 * to make sure the WakeLock stuff is done properly.
 */
public class LocationPoller extends BroadcastReceiver {
	public static final String EXTRA_ERROR = "com.whereismycar.EXTRA_ERROR";
	public static final String EXTRA_INTENT = "com.whereismycar.EXTRA_INTENT";
	public static final String EXTRA_LOCATION = "com.whereismycar.EXTRA_LOCATION";
	public static final String EXTRA_PROVIDER = "com.whereismycar.EXTRA_PROVIDER";
	public static final String EXTRA_LASTKNOWN = "com.whereismycar.EXTRA_LASTKNOWN";

	/**
	 * Standard entry point for a BroadcastReceiver. Delegates the event to LocationPollerService for processing.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("LocationPoller", "Location request received");
		LocationPollerService.requestLocation(context, intent);
	}
}
