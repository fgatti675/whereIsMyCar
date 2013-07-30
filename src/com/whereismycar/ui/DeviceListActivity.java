package com.whereismycar.ui;

/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.Set;

import com.whereismycar.R;
import com.whereismycar.auxiliar.Util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This Activity appears as a dialog. It lists any paired devices and devices detected in the area after discovery. When
 * a device is chosen by the user, the MAC address of the device is sent back to the parent Activity in the result
 * Intent.
 */
public class DeviceListActivity extends Activity {
	// Debugging
	private static final String TAG = "DeviceListActivity";

	// Return Intent extra
	public static final String EXTRA_DEVICE_ADDRESS = "device_address";
	public static final String EXTRA_DEVICE_NAME = "device_name";

	// Member fields
	private BluetoothAdapter mBtAdapter;
	private DeviceListAdapter mPairedDevicesArrayAdapter;
	private DeviceListAdapter mNewDevicesArrayAdapter;

	private String selectedDeviceAddress = "";

	private TextView title;

	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		final SharedPreferences prefs = Util.getSharedPreferences(this);
		selectedDeviceAddress = prefs.getString(Util.PREF_BT_DEVICE_ADDRESS, "");

		setContentView(R.layout.device_list);

		title = (TextView) findViewById(R.id.title_dialog);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setVisibility(View.INVISIBLE);
		title.setText(R.string.select_device);

		// Set result CANCELED in case the user backs out
		setResult(Activity.RESULT_CANCELED);

		// Initialize the button to perform device discovery
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});

		Button removeButton = (Button) findViewById(R.id.button_remove_link);
		removeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				prefs.edit().remove(Util.PREF_BT_DEVICE_ADDRESS).commit();
				finish();
			}
		});

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new DeviceListAdapter();
		mNewDevicesArrayAdapter = new DeviceListAdapter();

		// Find and set up the ListView for paired devices
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);

		// Find and set up the ListView for newly discovered devices
		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		// Get the local Bluetooth adapter
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			mPairedDevicesArrayAdapter.setDevices(pairedDevices);
			mPairedDevicesArrayAdapter.notifyDataSetChanged();
		} else {
			String noDevices = getResources().getText(R.string.none_paired).toString();
			// mPairedDevicesArrayAdapter.add(noDevices);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Make sure we're not doing discovery anymore
		if (mBtAdapter != null) {
			mBtAdapter.cancelDiscovery();
		}

		// Unregister broadcast listeners
		this.unregisterReceiver(mReceiver);
	}

	/**
	 * Start device discover with the BluetoothAdapter
	 */
	private void doDiscovery() {
		Log.d(TAG, "doDiscovery()");

		// Indicate scanning in the title
		progressBar.setVisibility(View.VISIBLE);
		title.setText(R.string.scanning);

		// Turn on sub-title for new devices
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		// If we're already discovering, stop it
		if (mBtAdapter.isDiscovering()) {
			mBtAdapter.cancelDiscovery();
		}

		// Request discover from BluetoothAdapter
		mBtAdapter.startDiscovery();
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			// Cancel discovery because it's costly and we're about to connect
			mBtAdapter.cancelDiscovery();

			// Get the device MAC address, which is the last 17 chars in the
			// View
			String info = ((TextView) v.findViewById(R.id.name)).getText().toString();
			String address = info.substring(info.length() - 17);

			Log.d("List", "Clicked: " + info + " " + address);

			SharedPreferences prefs = Util.getSharedPreferences(DeviceListActivity.this);
			prefs.edit().putString(Util.PREF_BT_DEVICE_ADDRESS, address).commit();

			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			intent.putExtra(EXTRA_DEVICE_NAME, info.substring(0, info.length() - 18));

			// Set result and finish this Activity
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mNewDevicesArrayAdapter.add(device);
					mNewDevicesArrayAdapter.notifyDataSetChanged();
				}
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

				progressBar.setVisibility(View.INVISIBLE);
				title.setText(R.string.select_device);
				if (mNewDevicesArrayAdapter.getCount() == 0) {
					// String noDevices = getResources().getText(
					// R.string.none_found).toString();
					// mNewDevicesArrayAdapter.add(noDevices);
				}
			}
		}
	};

	class DeviceListAdapter extends BaseAdapter {

		private Context mContext;
		ArrayList<BluetoothDevice> mDevices;

		public DeviceListAdapter() {
			mDevices = new ArrayList<BluetoothDevice>();
		}

		public void setDevices(Set<BluetoothDevice> devices) {
			mDevices = new ArrayList<BluetoothDevice>(devices);
		}

		public void add(BluetoothDevice device) {
			mDevices.add(device);
		}

		public int getCount() {
			return mDevices.size();
		}

		public Object getItem(int position) {

			return mDevices.get(position);
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(final int position, View convertView, ViewGroup parent) {

			View view;

			// Code for recliclying views
			if (convertView == null) {
				LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflator.inflate(R.layout.device_name, null);
			} else {
				view = convertView;
			}

			BluetoothDevice device = mDevices.get(position);

			// We set the visibility of the check image
			ImageView img = (ImageView) view.findViewById(R.id.imageView1);
			if (selectedDeviceAddress.equals(device.getAddress())) {
				img.setVisibility(View.VISIBLE);
			} else {
				img.setVisibility(View.INVISIBLE);
			}

			// We set the name
			TextView t = (TextView) view.findViewById(R.id.name);

			t.setText(device.getName() + "\n" + device.getAddress());

			return view;
		}

	}

}
