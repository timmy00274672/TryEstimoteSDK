package com.estimote.examples.demos;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
	private Button runButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//set the view and actionbar and initialize the object in the view
		setContentView(R.layout.diplab_project);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		runButton = (Button)findViewById(R.id.runButton);
		runButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleService();
			}
		});
		
		beaconManager = new BeaconManager(this);
		beaconManager.setRangingListener(new RangingListener() {

			@Override
			public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
				Log.d(TAG, beacons.toString());

			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		beaconManager.connect(new ServiceReadyCallback() {

			@Override
			public void onServiceReady() {
				Toast.makeText(DipProjectActivity.this, "Serivce is Ready", Toast.LENGTH_SHORT).show();
				runButton.setText("START Service");
				runButton.setEnabled(true);
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
		if(serviceRun){
			stopRanging();
			serviceRun = false;
			runButton.setText("START Service");
		}else{
			startRanging();
			serviceRun = true;
			runButton.setText("STOP Service");
		}
	}

	private void startRanging() {
		try {
			beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			Toast.makeText(
					DipProjectActivity.this,
					"Cannot start ranging, something terrible happened",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, "Cannot start ranging", e);
		}
	}
}
