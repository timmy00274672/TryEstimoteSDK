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
import android.widget.EditText;
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
	private EditText editTextPosition;

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
				upload(beaconList, getPositionInput());
			}
		});

		textViewUploadMsg = (TextView) findViewById(R.id.textViewUploadMsg);
		editTextPosition = (EditText) findViewById(R.id.editTextPosition);
	}

	private int getPositionInput() {
		return Integer.parseInt(editTextPosition.getText().toString());
	}

	private void upload(List<Beacon> beaconList, int position) {
		// TODO use handler to do so
		{
			final String translateToXmlString = translateToXmlString(beaconList,
					position);
			Toast.makeText(this, translateToXmlString, Toast.LENGTH_LONG)
					.show();
			String response = "";
			Log.d(TAG, translateToXmlString);
			HandlerThread mHandlerThread = new HandlerThread("upload");
			mHandlerThread.start();
			Handler handler = new Handler(mHandlerThread.getLooper());
			handler.post(new Runnable() {
				
				@Override
				public void run() {
					try {
						HttpURLConnection httpUrlConnection = (HttpURLConnection) new URL(
								"http://140.116.179.11/BT_project/BT_offline.php")
								.openConnection();
						httpUrlConnection.setConnectTimeout(20000);
						httpUrlConnection.setDoOutput(true);
						httpUrlConnection.setRequestMethod("POST");
						httpUrlConnection
								.setRequestProperty("Connection", "Keep-Alive");
						httpUrlConnection.connect();

						OutputStream os = httpUrlConnection.getOutputStream();

						InputStream fis = new ByteArrayInputStream(
								translateToXmlString.getBytes("UTF-8"));

						byte[] temp = new byte[1024 * 4]; // the common size of Internet
						// transmission
						int count;

						while ((count = fis.read(temp)) != -1) { // if the xmlFile is
																	// read over,
																	// return-1
							os.write(temp, 0, count);
						}
						os.close();

						BufferedReader in = new BufferedReader(new InputStreamReader(
								httpUrlConnection.getInputStream()));
						String buf = null;

						while ((buf = in.readLine()) != null) {
							Log.e(TAG,buf);
//							response = response + buf + "\n";
						}
						
						in.close();
						fis.close();

					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			
			
		}

		this.beaconList = new ArrayList<Beacon>();
		changeNumInTextViewNumber(0);
		changeNumInTextViewUploadMsg(beaconList.size(), position);
	}

	private static String translateToXmlString(List<Beacon> beaconList,
			int position) {

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();

		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "data");

			{
				// <RP_Id>1</RP_Id>
				serializer.startTag("", "RP_Id");
				serializer.text(position + "");
				serializer.endTag("", "RP_Id");

				/*
				 * <Bluetooth> <BeaconRecord>
				 * <Mac_Address>DF:81:E2:C5:A2:BA</Mac_Address> <RSS>-79</RSS>
				 * <Scan_Id>1</Scan_Id> </BeaconRecord> <BeaconRecord>
				 * <Mac_Address>F6:CC:5E:1A:67:43</Mac_Address> <RSS>-83</RSS>
				 * <Scan_Id>1</Scan_Id> </BeaconRecord> </Bluetooth>
				 */
				serializer.startTag("", "Bluetooth");
				{
					for (Beacon beacon : beaconList) {
						serializer.startTag("", "BeaconRecord");
						{
							serializer.startTag("", "Mac_Address");
							serializer.text(beacon.getMacAddress());
							serializer.endTag("", "Mac_Address");

							serializer.startTag("", "Scan_Id");
							serializer.text(1 + "");
							serializer.endTag("", "Scan_Id");
							
							serializer.startTag("", "RSS");
							serializer.text(beacon.getRssi() + "");
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

	private void changeNumInTextViewUploadMsg(int size, int position) {
		CharSequence msg = String.format(
				"There are %d beacons upload at position %d", size, position);
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
