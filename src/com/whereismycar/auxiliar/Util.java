package com.whereismycar.auxiliar;

import java.util.Calendar;

import com.google.android.maps.GeoPoint;
import com.whereismycar.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Util {

	/*
	 * Shared preferences constants
	 */
	public static final String SHARED_PREFS = "WHEREISMYCAR_PREFS";
	public static final String PREF_BT_DEVICE_ADDRESS = "PREF_BT_DEVICE_ADDRESS";
	public static final String PREF_DIALOG_SHOWN = "PREF_DIALOG_SHOWN";
	public static final String PREF_CAR_LATITUDE = "PREF_CAR_LATITUDE";
	public static final String PREF_CAR_LONGITUDE = "PREF_CAR_LONGITUDE";
	public static final String PREF_CAR_ACCURACY = "PREF_CAR_ACCURACY";
	public static final String PREF_CAR_PROVIDER = "PREF_CAR_PROVIDER";
	public static final String PREF_CAR_TIME = "PREF_CAR_TIME";
	public static final String PREF_BT_DISCONNECTION_TIME = "PREF_BT_DISCONNECTION_TIME";

	public static final float WALKING_SPEED = 1.4F; // walking speed in m/s

	/*
	 * Maximum distance we consider normal between 2 positions received on the same parking
	 */
	public static final int MAX_ALLOWED_DISTANCE = 100; //

	/*
	 * Intent for detecting BT disconnection
	 */
	public static final String INTENT_NEW_CAR_POS = "NEW_CAR_POSITION";
	public static final int PREF_POSITIONING_TIME_LIMIT = 30000;
	public static final String EXTRA_LOCATION = "EXTRA_LOCATION";
	public static final float DEFAULT_ACCURACY = 7; // in meters, used when the user taps the map

	/**
	 * Method for printing our fancy custom Toast
	 * 
	 * @param context
	 * @param string
	 *            Content of the toast
	 * @param length
	 *            One from Toast.LENGTH_LONG or Toast.LENGTH_SHORT
	 */
	public static void createToast(Context context, String string, int length) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_toast, null);
		TextView text = (TextView) layout.findViewById(R.id.text);
		text.setText(string);

		Toast toast = new Toast(context.getApplicationContext());
		toast.setGravity(Gravity.FILL_HORIZONTAL, 0, 0);
		toast.setDuration(length);
		toast.setView(layout);
		toast.show();
	}

	/**
	 * This method shows the Toast when the car icon is pressed, telling the user the parking time
	 * 
	 * @param context
	 */
	public static void showCarTimeToast(Context context) {
		String toastMsg = context.getString(R.string.car_was_here);

		SharedPreferences prefs = Util.getSharedPreferences(context);
		long timeDiff = Calendar.getInstance().getTimeInMillis() - prefs.getLong(Util.PREF_CAR_TIME, 0);

		String time = "";

		long seconds = timeDiff / 1000;
		if (seconds < 60) {
			time = seconds + " " + context.getString(R.string.seconds);
		} else {
			long minutos = timeDiff / (60 * 1000);
			if (minutos < 60) {
				time = minutos
						+ (minutos > 1 ? " " + context.getString(R.string.minutes) : " "
								+ context.getString(R.string.minute));
			} else {
				long hours = timeDiff / (60 * 60 * 1000);
				if (hours < 24) {
					time = hours
							+ (hours > 1 ? " " + context.getString(R.string.hours) : " "
									+ context.getString(R.string.hour));
				} else {
					long days = timeDiff / (24 * 60 * 60 * 1000);
					time = days
							+ (days > 1 ? " " + context.getString(R.string.days) : " "
									+ context.getString(R.string.day));
				}
			}
		}

		toastMsg = String.format(toastMsg, time);

		Util.createToast(context, toastMsg, Toast.LENGTH_SHORT);

	}
	
	/**
	 * Shows a toast in case no BT is detected
	 */
	public static void noBluetooth(Context context) {
		Util.createToast(context, context.getString(R.string.bt_not_available), Toast.LENGTH_SHORT);
	}

	/**
	 * Method that returns the average of 2 double values, giving a different weight to each one
	 * 
	 * @param a
	 * @param aWeight
	 * @param b
	 * @param bWeight
	 * @return
	 */
	public static double poundedAverage(double a, double aWeight, double b, double bWeight) {
		return (a * aWeight + b * bWeight) / (aWeight + bWeight);
	}

	/**
	 * It returns the only shared preferences file we will be using in the app.
	 * 
	 * @param context
	 * @return
	 */
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(Util.SHARED_PREFS, Context.MODE_WORLD_READABLE);
	}

	/**
	 * 
	 * Computes the bearing in degrees between two points on Earth.
	 * 
	 * @param p1 First point
	 * @param p2 Second point
	 * @return Bearing between the two points in degrees. A value of 0 means due north.
	 */

	public static double bearing(GeoPoint p1, GeoPoint p2) {

		double lat1 = p1.getLatitudeE6() / 1E6;

		double lon1 = p1.getLongitudeE6() / 1E6;

		double lat2 = p2.getLatitudeE6() / 1E6;

		double lon2 = p2.getLongitudeE6() / 1E6;

		double lat1Rad = Math.toRadians(lat1);

		double lat2Rad = Math.toRadians(lat2);

		double deltaLonRad = Math.toRadians(lon2 - lon1);

		double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);

		double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - Math.sin(lat1Rad) * Math.cos(lat2Rad)

				* Math.cos(deltaLonRad);

		return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;

	}

}
