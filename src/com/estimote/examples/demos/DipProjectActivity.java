package com.estimote.examples.demos;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.BeaconManager.RangingListener;
import com.estimote.sdk.BeaconManager.ServiceReadyCallback;
import com.estimote.sdk.Region;

public class DipProjectActivity extends Activity {

	protected static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);;
	protected static final String TAG = DipProjectActivity.class.getSimpleName();
	private BeaconManager beaconManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		beaconManager = new BeaconManager(this);
		beaconManager.setRangingListener(new RangingListener() {
			
			@Override
			public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
				Log.d(TAG,beacons.toString());
				
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		beaconManager.connect(new ServiceReadyCallback() {
			
			@Override
			public void onServiceReady() {
				try {
			          beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
			        } catch (RemoteException e) {
			          Toast.makeText(DipProjectActivity.this, "Cannot start ranging, something terrible happened",
			              Toast.LENGTH_LONG).show();
			          Log.e(TAG, "Cannot start ranging", e);
			        }
			}
		});
	}
	
	@Override
	protected void onStop() {
		try {
			beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			Log.d(TAG, "Error while stopping ranging", e);
		}
		
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		beaconManager.disconnect();
		super.onDestroy();
	}
}
