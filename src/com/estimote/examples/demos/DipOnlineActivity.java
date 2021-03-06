package com.estimote.examples.demos;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Xml;
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

public class DipOnlineActivity extends Activity {

	protected static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region(
			"rid", null, null, null);;
	protected static final String TAG = DipOnlineActivity.class.getSimpleName();
	private BeaconManager beaconManager;
	private Button buttonRun;
	private List<MyBeacon> beaconList = new ArrayList<MyBeacon>();
	private TextView textViewUploadMsg;
	private String resultString;

	@SuppressLint("HandlerLeak")
	private Handler messageHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			textViewUploadMsg.setText(resultString);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set the view and actionbar and initialize the object in the view
		setContentView(R.layout.diplab_online);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		buttonRun = (Button) findViewById(R.id.buttonRunOnline);
		buttonRun.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startRanging();
			}
		});

		textViewUploadMsg = (TextView) findViewById(R.id.textViewUploadMsg);

		beaconManager = new BeaconManager(this);
		beaconManager.setRangingListener(new RangingListener() {
			
			int scanNum = 1;

			@Override
			public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {
				Log.d(TAG, beacons.toString());
				for (Beacon beacon : beacons) {
					beaconList.add(new MyBeacon(beacon, scanNum));
					scanNum++;
				}
//				stopRanging();
				if(scanNum >= 3){
					upload(beaconList);
					scanNum = 1;
				}
			}
		});

	}

	private void upload(List<MyBeacon> beaconList2) {
		final String translateToXmlString = translateToXmlString(beaconList2);
		Log.d(TAG, translateToXmlString);
		HandlerThread mHandlerThread = new HandlerThread("upload");
		mHandlerThread.start();
		Handler handler = new Handler(mHandlerThread.getLooper());
		handler.post(new Runnable() {

			@Override
			public void run() {
				try {

					String response = "";

					HttpURLConnection httpUrlConnection = (HttpURLConnection) new URL(
							getString(R.string.string_online_url))
							.openConnection();
					httpUrlConnection.setConnectTimeout(20000);
					httpUrlConnection.setDoOutput(true);
					httpUrlConnection.setRequestMethod("POST");
					httpUrlConnection.setRequestProperty("Connection",
							"Keep-Alive");
					httpUrlConnection.connect();

					OutputStream os = httpUrlConnection.getOutputStream();

					InputStream fis = new ByteArrayInputStream(
							translateToXmlString.getBytes("UTF-8"));

					byte[] temp = new byte[1024 * 4]; // the common size of
														// Internet
					// transmission
					int count;

					while ((count = fis.read(temp)) != -1) { // if the xmlFile
																// is
																// read over,
																// return-1
						os.write(temp, 0, count);
					}
					os.close();

					BufferedReader in = new BufferedReader(
							new InputStreamReader(httpUrlConnection
									.getInputStream()));
					String buf = null;

					while ((buf = in.readLine()) != null) {
						Log.e(TAG, buf);
						response = response + buf + "\n";
					}

					in.close();
					fis.close();

					resultString = response;
					messageHandler.sendEmptyMessage(0);

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		this.beaconList = new ArrayList<MyBeacon>();
		textViewUploadMsg.append("\n" + translateToXmlString);
	}

	public static String translateToXmlString(List<MyBeacon> beaconList2) {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();

		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "data");

			{

				/*
				 * <Bluetooth> <BeaconRecord>
				 * <Mac_Address>DF:81:E2:C5:A2:BA</Mac_Address> <RSS>-79</RSS>
				 * <Scan_Id>1</Scan_Id> </BeaconRecord> <BeaconRecord>
				 * <Mac_Address>F6:CC:5E:1A:67:43</Mac_Address> <RSS>-83</RSS>
				 * <Scan_Id>1</Scan_Id> </BeaconRecord> </Bluetooth>
				 */
				serializer.startTag("", "Bluetooth");
				{
					for (MyBeacon beacon : beaconList2) {
						serializer.startTag("", "BeaconRecord");
						{
							serializer.startTag("", "Mac_Address");
							serializer.text(beacon.beacon.getMacAddress());
							serializer.endTag("", "Mac_Address");

							serializer.startTag("", "Scan_Id");
							serializer.text(beacon.scan + "");
							serializer.endTag("", "Scan_Id");

							serializer.startTag("", "RSS");
							serializer.text(beacon.beacon.getRssi() + "");
							serializer.endTag("", "RSS");
						}
						serializer.endTag("", "BeaconRecord");
					}

				}
				serializer.endTag("", "Bluetooth");

			}

			serializer.endTag("", "data");
			serializer.endDocument();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return writer.toString();
	}

	@Override
	protected void onStart() {
		super.onStart();
		beaconManager.connect(new ServiceReadyCallback() {

			@Override
			public void onServiceReady() {
				Toast.makeText(DipOnlineActivity.this, "Serivce is Ready",
						Toast.LENGTH_SHORT).show();
				buttonRun.setText(getString(R.string.string_start_service));
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

	private void startRanging() {
		try {
			beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
		} catch (RemoteException e) {
			Toast.makeText(DipOnlineActivity.this,
					"Cannot start ranging, something terrible happened",
					Toast.LENGTH_LONG).show();
			Log.e(TAG, "Cannot start ranging", e);
		}
	}
}
