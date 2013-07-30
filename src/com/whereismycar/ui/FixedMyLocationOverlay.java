package com.whereismycar.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Projection;
import com.whereismycar.R;
import com.whereismycar.auxiliar.Util;

/**
 * Fixes bug with some phone's location overlay class (ie Droid X). Essentially, it attempts to use the default
 * MyLocationOverlay class, but if it fails, we override the drawMyLocation method to provide an icon and accuracy
 * circle to mimic showing user's location. Right now the icon is a static image. If you want to have it animate, modify
 * the drawMyLocation method.
 */
public class FixedMyLocationOverlay extends MyLocationOverlay {

	private static final String TAG = "MyLocationOverlay";

	private static final int POSITION_RADIUS = 10;

	private Paint accuracyPaint;
	private Paint positionPaint;

	private Point center;
	private Point left;
	private int width;
	private int height;

	Bitmap myCompassPointer;

	Drawable drawable;

	private Context mContext;

	// We also use the car position here to know if we are poiting to the car
	private GeoPoint carPosition;

	private GeoPoint myLocation;

	public FixedMyLocationOverlay(Context context, MapView mapView) {
		super(context, mapView);
		mContext = context;
	}

	public GeoPoint getCarPosition() {
		return carPosition;
	}

	public void setCarPosition(GeoPoint carPosition) {
		this.carPosition = carPosition;
	}

	@Override
	protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {

		this.myLocation = myLocation;

		// If we are using the compass
		if (this.isCompassEnabled()) {
			
			// In the first run we initialise the compass pointer
			if (myCompassPointer == null) {

				myCompassPointer = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.maps_indicator);

				drawable = mapView.getContext().getResources()
						.getDrawable(R.drawable.ic_maps_indicator_current_position_bw);

				accuracyPaint = new Paint();
				accuracyPaint.setAntiAlias(true);
				accuracyPaint.setStrokeWidth(2.0f);

				positionPaint = new Paint();
				positionPaint.setAntiAlias(true);
				positionPaint.setStrokeWidth(1.0f);

				width = myCompassPointer.getWidth();
				height = myCompassPointer.getHeight();
				center = new Point();
				left = new Point();
			}

			// Translate coordinates
			Projection projection = mapView.getProjection();
			double latitude = lastFix.getLatitude();
			double longitude = lastFix.getLongitude();
			float accuracy = lastFix.getAccuracy();

			float[] result = new float[1];

			Location.distanceBetween(latitude, longitude, latitude, longitude + 1, result);
			float longitudeLineDistance = result[0];

			// We get the left point of the circle, and translate it
			GeoPoint leftGeo = new GeoPoint((int) (latitude * 1e6), (int) ((longitude - accuracy
					/ longitudeLineDistance) * 1e6));
			projection.toPixels(leftGeo, left);
			projection.toPixels(myLocation, center);
			int radius = center.x - left.x;

			drawable.setBounds(center.x - width / 2, center.y - height / 2, center.x + width / 2, center.y + height / 2);
			drawable.draw(canvas);

			accuracyPaint.setColor(0xff0ba28f);
			accuracyPaint.setStyle(Style.STROKE);
			canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

			accuracyPaint.setColor(0x180ba28f);
			accuracyPaint.setStyle(Style.FILL);
			canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

			positionPaint.setColor(0xdd3e3232);
			positionPaint.setStyle(Style.STROKE);
			canvas.drawCircle(center.x, center.y, POSITION_RADIUS, positionPaint);


		// If we are not using the compass
		} else {
			if (drawable == null) {

				accuracyPaint = new Paint();
				accuracyPaint.setAntiAlias(true);
				accuracyPaint.setStrokeWidth(2.0f);

				positionPaint = new Paint();
				positionPaint.setAntiAlias(true);
				positionPaint.setStrokeWidth(2.0f);

				drawable = mapView.getContext().getResources()
						.getDrawable(R.drawable.ic_maps_indicator_current_position_bw);
				width = drawable.getIntrinsicWidth();
				height = drawable.getIntrinsicHeight();
				center = new Point();
				left = new Point();
			}

			Projection projection = mapView.getProjection();
			double latitude = lastFix.getLatitude();
			double longitude = lastFix.getLongitude();
			float accuracy = lastFix.getAccuracy();

			float[] result = new float[1];

			Location.distanceBetween(latitude, longitude, latitude, longitude + 1, result);
			float longitudeLineDistance = result[0];

			GeoPoint leftGeo = new GeoPoint((int) (latitude * 1e6), (int) ((longitude - accuracy
					/ longitudeLineDistance) * 1e6));
			projection.toPixels(leftGeo, left);
			projection.toPixels(myLocation, center);
			int radius = center.x - left.x;

			accuracyPaint.setColor(0xff0ba28f);
			accuracyPaint.setStyle(Style.STROKE);
			canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

			accuracyPaint.setColor(0x180ba28f);
			accuracyPaint.setStyle(Style.FILL);
			canvas.drawCircle(center.x, center.y, radius, accuracyPaint);

			positionPaint.setColor(0xdd3e3232);
			positionPaint.setStyle(Style.STROKE);
			canvas.drawCircle(center.x, center.y, POSITION_RADIUS, positionPaint);

			positionPaint.setColor(0xbbd2d2d2);
			positionPaint.setStyle(Style.FILL);
			canvas.drawCircle(center.x, center.y, POSITION_RADIUS, positionPaint);

			drawable.setBounds(center.x - width / 2, center.y - height / 2, center.x + width / 2, center.y + height / 2);
			drawable.draw(canvas);
		}

	}

	@Override
	protected void drawCompass(Canvas canvas, float bearing) {
		if (this.isCompassEnabled()) { // Probably a redundant check
			if (myCompassPointer != null) {
				// we turn the bearing into an int, so it gets less shaky
				int bearingInt = (int) bearing;
				rotateCompass(canvas, bearingInt);
			}
		}
		
		// We don't call super so we don't get the compass on the top

	}

	/**
	 * Private method for rotating the compass. We turn the color of the indicator from grey to green as long
	 * as we are looking to the car.
	 * 
	 * @param canvas
	 * @param bearingInt
	 */
	private void rotateCompass(Canvas canvas, int bearingInt) {

		// we rotate the cursor
		Matrix rotationMatrix = new Matrix();
		rotationMatrix.preTranslate(center.x - width / 2, center.y - height / 2);
		rotationMatrix.preRotate(bearingInt, myCompassPointer.getWidth() / 2.0f, myCompassPointer.getHeight() / 2.0f);

		if (carPosition != null) {
			double bearingToCar;
			
			// this is the bearing we should have to be pointing at the car
			int carBearing = (int) Util.bearing(myLocation, carPosition);
			// this is the difference in degrees from where we are looking to the car
			bearingToCar = Math.min(
					Math.min(
							Math.abs(carBearing - bearingInt),
							(360 - carBearing + bearingInt)),
					(360 - bearingInt + carBearing));

			// we set the indicator color based on the result
			Log.v(TAG, "Compass move: " + bearingInt + " " + bearingToCar);
			if (bearingToCar < 20) {
				positionPaint.setColor(0xbb00ffd8);
			} else if (bearingToCar < 40) {
				positionPaint.setColor(0xbb79ead9);
			} else if (bearingToCar < 60) {
				positionPaint.setColor(0xbb96dbd0);
			} else if (bearingToCar < 80) {
				positionPaint.setColor(0xbbb4d1cd);
			} else {
				positionPaint.setColor(0xbbd2d2d2);
			}
		} else {
			positionPaint.setColor(0xddd2d2d2);
		}

		positionPaint.setStyle(Style.FILL);
		canvas.drawCircle(center.x, center.y, POSITION_RADIUS, positionPaint);

		// draw the rotated pointer
		canvas.drawBitmap(myCompassPointer, rotationMatrix, null);

	}
}
