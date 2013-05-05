package com.example.lc10;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements SensorEventListener {
	
	public SensorManager sensorManager;
	public Sensor accelerometer;
	private boolean isRecording = false;
	private long elapsedTime = 0;
	private File workingFile;
	private Intent sensorIntent;
	private final Handler mhandler = new Handler();
	private TextView message;
	private WakeLock wakeLock;
	
	private static int numberOfDatapoints = 0;
	
	
	public BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
				return;
			}
			
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					unregisterListener();
					registerListener();						
				}				
			};
			
			new Handler().postDelayed(runnable, 500);
		}
	};
	
	private void registerListener(){
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	private void unregisterListener(){
		sensorManager.unregisterListener(this);
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		message = (TextView)findViewById(R.id.textView1);
		sensorIntent = new Intent(this, SensorService.class);
		PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LC10");	
			
		
		Button startButton = (Button)findViewById(R.id.startbutton);
		startButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				Post("Recording started.");
								
				try
				{				
					DateFormat df = new SimpleDateFormat("dd_MM_yy");
					String formattedDate = df.format(new Date());				
					
					File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AcRecogData");
					if(!path.exists())
						path.mkdir();
					
					File filename = new File(path.getAbsolutePath() + "/" + formattedDate + ".csv");
					if(filename.exists()){
						File[] files = path.listFiles();
						int highestIndex = 0;
						for(File f : files){
							if(f.getAbsolutePath().contains(formattedDate) && f.getAbsolutePath().contains("-")){
								String[] splits = f.getName().split("-");
								String index = splits[1].split("\\.")[0];						
								
								if(Integer.parseInt(index) > highestIndex)
									highestIndex = Integer.parseInt(index);								
							}
						}
						
						filename = new File(filename.getAbsolutePath().replace(".csv", "-" + String.valueOf(highestIndex + 1) + ".csv"));
					}
					
					workingFile = new File(filename.getAbsolutePath());				
					
					wakeLock.acquire();
					isRecording = true;
					elapsedTime = System.currentTimeMillis();
					
//					sensorIntent.putExtra("filename", filename.getAbsolutePath());
//					startService(sensorIntent);
				}
				catch(Exception e){
					Post(e.getMessage());
				}
			}
		});
		
		Button stopButton = (Button)findViewById(R.id.stopbutton);
		stopButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				isRecording = false;
				elapsedTime = 0;
				workingFile = null;
				wakeLock.release();
				Post("Recording stopped. Datapoints: " + numberOfDatapoints);
				numberOfDatapoints = 0;
				//stopService(sensorIntent);
				
			}
		});
		
		
		Button uploadButton = (Button)findViewById(R.id.uploadbutton);
		uploadButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost("http://192.168.1.33:8888/activitytracker");
				// TODO: Upload .csv file

				
			}
		});
		
		registerReceiver(receiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
		
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);

	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {		
		if(isRecording){
			long delta = System.currentTimeMillis();
			if(elapsedTime != 0 && (delta - elapsedTime) >= 50){ 
				elapsedTime = delta;
				numberOfDatapoints++;
				float[] v = event.values;
				
				Date d = new Date();
				
				try {		

					FileWriter fw = new FileWriter(workingFile, true);
					BufferedWriter out = new BufferedWriter(fw);
					out.write(d.toString() + ";" + v[0] + ";" + v[1] + ";" + v[2] + "\n");
					out.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Post(e.getMessage());
				}			
			}
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		wakeLock.release();
		stopService(sensorIntent);
	}
	
	protected void onResume(){
		super.onResume();
		wakeLock.acquire();
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	protected void onPause(){
		super.onPause();
		wakeLock.release();
		sensorManager.unregisterListener(this);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	


	
    public void Post(final String msg){
  		mhandler.post(new Runnable(){

			@Override
			public void run() {
				message.setText(msg);
				
			}
  			
  		});
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
