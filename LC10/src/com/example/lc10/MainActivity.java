package com.example.lc10;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends FragmentActivity implements SensorEventListener {
	
	private static Marker marker;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private boolean isRecording = false;
	private long elapsedTime = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button startButton = (Button)findViewById(R.id.startbutton);
		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				isRecording = true;
				elapsedTime = System.currentTimeMillis();
				
			}
		});
		
		Button stopButton = (Button)findViewById(R.id.stopbutton);
		stopButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isRecording = false;
				elapsedTime = 0;
				
			}
		});
				
//		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);		
//		boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//		boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//		
//		if(isGpsEnabled && isNetworkEnabled){
//			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 1, locListener);			
//		}
		
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);	

	}
	
	protected void onResume(){
		super.onResume();
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	protected void onPause(){
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
//	private final LocationListener locListener = new LocationListener() {
//		
//		@Override
//		public void onLocationChanged(Location location) {
//			GoogleMap map = ((SupportMapFragment)  getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
//			if(marker != null)
//				marker.remove();
//			marker = map.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
//			map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16.0f));
//			
//			EditText username = (EditText)findViewById(R.id.textViewUsername);
//			
//			
//			try {
//				SendLocationData(location.getLatitude(), location.getLongitude(), username.getText().toString());
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ClientProtocolException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}		
//			
//		}

//		private void SendLocationData(double latitude, double longitude, String username) throws ClientProtocolException, IOException {
//			HttpClient client = new DefaultHttpClient();
//			HttpPost post = new HttpPost("http://192.168.1.33:8888/activitytracker");
//			
//			try{
//				List<NameValuePair> parameters = new ArrayList<NameValuePair>();
//				parameters.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
//				parameters.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));
//				parameters.add(new BasicNameValuePair("username", username));
//				post.setEntity(new UrlEncodedFormEntity(parameters));
//				
//				HttpResponse response = client.execute(post);
//				
//			}
//			catch(Exception e){
//				
//			}			
//			
//		}
//
//		@Override
//		public void onProviderDisabled(String provider) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void onProviderEnabled(String provider) {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void onStatusChanged(String provider, int status, Bundle extras) {
//			// TODO Auto-generated method stub
//			
//		}
//	};


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		
		if(isRecording){
			long delta = System.currentTimeMillis();
			if(elapsedTime != 0 && (delta - elapsedTime) > 50){						
				float[] v = event.values;
				
				Date d = new Date();
				
				try {
					
					File f = Environment.getExternalStorageDirectory();
					FileWriter fw = new FileWriter(f.getAbsolutePath() + "/out.csv");
					BufferedWriter out = new BufferedWriter(fw);
					out.write(d.toString() + "," + v[0] + "," + v[1] + "," + v[2]);
					out.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				
			}
				
		}
	}

	private void SendAccelerometerData(float[] v, String username) {
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("http://192.168.1.33:8888/activitytracker");
		
		try
		{
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair("x", String.valueOf(v[0])));
			parameters.add(new BasicNameValuePair("y", String.valueOf(v[1])));
			parameters.add(new BasicNameValuePair("z", String.valueOf(v[2])));
			parameters.add(new BasicNameValuePair("username", username));
			post.setEntity(new UrlEncodedFormEntity(parameters));
			
			HttpResponse response = client.execute(post);
			
		}
		catch(Exception e){
			
		}
		
	}

}
