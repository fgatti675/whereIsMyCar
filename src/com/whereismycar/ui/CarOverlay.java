package com.whereismycar.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.whereismycar.R;
import com.whereismycar.auxiliar.Util;

/**
 * This class represents the car icon we will overlay over the map. It is composed of the car icon and a circle
 * representing the accuracy
 */
public class CarOverlay extends Overlay {

	// Context of the calling activity
	Context mContext;

	// Location of the car
	Location location;

	// coordinates of the icon, useful to check for tapping
	int left;
	int top;
	int right;
	int bottom;

	public CarOverlay(Context context) {
		mContext = context;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	/**
	 * This method draws the car overlay over the map
	 */
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		super.draw(canvas, mapView, shadow);

		Paint accuracyPaint;
		Point leftBorder;
		Point carPts;

		Drawable carDrawable;

		int height;

		// ICON

		// translate the GeoPoint to screen pixels
		carPts = new Point();
		GeoPoint gp = new GeoPoint((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6));

		Projection projection = mapView.getProjection();
		projection.toPixels(gp, carPts);

		// add the marker
		carDrawable = mapView.getContext().getResources().getDrawable(R.drawable.car_p);
		height = carDrawable.getIntrinsicHeight();
		leftBorder = new Point();

		// drawable borders
		// left = carPts.x - width / 2;
		left = carPts.x - 15; // TODO: hard coded, better use a constant probably
		top = carPts.y - height;
		// right = carPts.x + width / 2;
		right = carPts.x + 30;
		bottom = carPts.y;

		carDrawable.setBounds(left, top, right, bottom);

		// ACCURACY CIRCLE

		accuracyPaint = new Paint();
		accuracyPaint.setAntiAlias(true);
		accuracyPaint.setStrokeWidth(2.0f);

		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		float accuracy = location.getAccuracy();

		float[] result = new float[1];

		Location.distanceBetween(latitude, longitude, latitude, longitude + 1, result);
		float longitudeLineDistance = result[0];

		GeoPoint leftGeo = new GeoPoint((int) (latitude * 1e6),
				(int) ((longitude - accuracy / longitudeLineDistance) * 1e6));
		projection.toPixels(leftGeo, leftBorder);
		int radius = carPts.x - leftBorder.x;

		accuracyPaint.setColor(0xff883e25); // TODO: remove all hard-coded colors
		accuracyPaint.setStyle(Style.STROKE);
		canvas.drawCircle(carPts.x, carPts.y, radius, accuracyPaint);

		accuracyPaint.setColor(0x18883e25); // TODO: remove all hard-coded colors
		accuracyPaint.setStyle(Style.FILL);
		canvas.drawCircle(carPts.x, carPts.y, radius, accuracyPaint);

		// draw the actual car icon
		carDrawable.draw(canvas);

		return true;
	}

	/**
	 * onTap we show a Toast telling the user the last time parked
	 */
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		Log.d("CarOverlay", "onTap: " + p.toString());

		// we need to check if the user has tapped over the icon
		Projection projection = mapView.getProjection();

		Point tapPoint = new Point();
		projection.toPixels(p, tapPoint);

		// we check if the user tapped over
		if (tapPoint.x > left && tapPoint.x < right && tapPoint.y > top && tapPoint.y < bottom) {

			Util.showCarTimeToast(mContext);
		}

		// we return false so the event is not consumed and tap is propagated to other views
		// TODO: set to true?
		return false;
	}

}
