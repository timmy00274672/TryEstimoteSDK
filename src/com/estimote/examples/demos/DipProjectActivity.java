package com.estimote.examples.demos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.BeaconManager.RangingListener;
import com.estimote.sdk.BeaconManager.ServiceReadyCallback;
import com.estimote.sdk.Region;

public class DipProjectActivity extends Activity {

	protected static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region(
			"rid", null, null, null);;
	protected static final String TAG = DipProjectActivity.class
			.getSimpleName();
	private BeaconManager beaconManager;
	private boolean serviceRun = false;
	private Button buttonRun;
	private List<Beacon> beaconList = new ArrayList<Beacon>();
	private TextView textViewNumber;
	private Button buttonUpload;
	private TextView textViewUploadMsg;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set the view and actionbar and initialize the object in the view
		setContentView(R.layout.diplab_project);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		buttonRun = (Button) findViewById(R.id.buttonRun);
		buttonRun.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggleService();
			}
		});

		textViewNumber = (TextView) findViewById(R.id.textViewNumber);

		beaconManager = new BeaconManager(this);
		beaconManager.setRangingListener(new RangingListener() {

			@Override
			public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
				Log.d(TAG, beacons.toString());
				beaconList.addAll(beacons);
				changeNumInTextViewNumber(beaconList.size());
			}
		});

		buttonUpload = (Button) findViewById(R.id.buttonUpload);
		buttonUpload.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				upload(beaconList);
			}
		});

		textViewUploadMsg = (TextView) findViewById(R.id.textViewUploadMsg);

	}

	private void upload(List<Beacon> beaconList) {
		// TODO use handler to do so
		this.beaconList = new ArrayList<Beacon>();
		changeNumInTextViewNumber(0);
		changeNumInTextViewUploadMsg(beaconList.size());
	}

	private void changeNumInTextViewUploadMsg(int size) {
		CharSequence msg = String.format("There are %d beacons upload", size);
		textViewUploadMsg.setText(msg);
	}

	private void changeNumInTextViewNumber(int size) {
		CharSequence msg = String.format("There are %d beacons", size);
		textViewNumber.setText(msg);
	}

	@Override
	protected void onStart() {
		super.onStart();
		beaconManager.connect(new ServiceReadyCallback() {

			@Override
			public void onServiceReady() {
				Toast.makeText(DipProjectActivity.this, "Serivce is Ready",
						Toast.LENGTH_SHORT).show();
				buttonRun.setText("START Service");
				buttonRun.setEnabled(true);
			}
		});
	}

	@Override
	protected void onStop() {
		stopRanging();

		super.onStop();
	}

	private void stopRanging() {
		try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			Log.d(TAG, "Error while stopping ranging", e);
		}
	}

	@Override
	protected void onDestroy() {
		beaconManager.disconnect();
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void toggleService() {
		if (serviceRun) {
			stopRanging();
			serviceRun = false;
			// CharSequence msg = String.format("There are %d beacons",
			// beaconList.size());
			// Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			buttonRun.setText("START Service");
		} else {
			startRanging();
			serviceRun = true;
			buttonRun.setText("STOP Service");
		}
	}

	private void startRanging() {
		try {
			beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			Toast.makeText(DipProjectActivity.this,
					"Cannot start ranging, something terrible happened",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, "Cannot start ranging", e);
		}
	}
}
