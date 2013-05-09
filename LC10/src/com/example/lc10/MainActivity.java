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
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends FragmentActivity implements SensorEventListener {
	
	public SensorManager sensorManager;
	public Sensor accelerometer;
	private boolean isRecording = false;
	private long startTime = 0;
	private long elapsedTime = 0;
	private static File workingFile;
	//private Intent sensorIntent;
	private final Handler mhandler = new Handler();
	private TextView message;
	private Spinner spinnerLocomotionActivity;
	private WakeLock wakeLock;
	private String delimiter = ",";
	private static ArrayList<DataPoint> dataPoints;
	
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
		//sensorIntent = new Intent(this, SensorService.class);
		PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LC10");
		dataPoints = new ArrayList<DataPoint>();
			
		spinnerLocomotionActivity = (Spinner) findViewById(R.id.spinner1);
		
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
					
					String locomotion = spinnerLocomotionActivity.getSelectedItem().toString();
					
					File filename = new File(path.getAbsolutePath() + "/" + formattedDate + (locomotion.length() > 0 ? "_" + locomotion : "") + ".csv");
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
					
					addColumnNamestoCsvFile();
					
					wakeLock.acquire();
					isRecording = true;
					startTime = System.currentTimeMillis();
					elapsedTime = System.currentTimeMillis();
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
				startTime = 0;
				
				wakeLock.release();				
				Post("Recording stopped. Datapoints: " + numberOfDatapoints);
				numberOfDatapoints = 0;
				
				Date stoptime = new Date();
				CleanDataOfLastRecordings(stoptime);
				SaveDataToFile();
				
				workingFile = null;
				
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
	
	private void CleanDataOfLastRecordings(Date stoptime) {
		ArrayList<DataPoint> toBeRemoved = new ArrayList<DataPoint>();
		if(!dataPoints.isEmpty()){
			for(DataPoint dp : dataPoints){
				long diffInSeconds = (stoptime.getTime() - dp.getDate().getTime()) / 1000;
				if(diffInSeconds <= 2){
					toBeRemoved.add(dp);					
				}				
			}
			
			for(DataPoint dp: toBeRemoved){
				dataPoints.remove(dp);
			}			
		}
		
	}
	
	private void addColumnNamestoCsvFile()
	{
		String[] columnNames = {"X", "Y", "Z", "Activity"};
		
		try {		

			FileWriter fw = new FileWriter(workingFile, true);
			BufferedWriter out = new BufferedWriter(fw);
			
			for(int i=0; i<columnNames.length; i++) {
				if(i == columnNames.length -1) {
					out.write(columnNames[i] + "\n");
				} else {
					out.write(columnNames[i] + delimiter);
				}
			}
			
			out.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Post(e.getMessage());
		}	
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		long delta = System.currentTimeMillis();
		if(isRecording && (delta - startTime) >= 2500){			
			if(elapsedTime != 0 && (delta - elapsedTime) >= 50){ 
				elapsedTime = delta;
				numberOfDatapoints++;
				float[] v = event.values;
				
				Date d = new Date();				
				try {
					
					dataPoints.add(new DataPoint(d, v[0], v[1], v[2]));
					
				} catch (Exception e) {				
					Post(e.getMessage());
				}			
			}
		}
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		wakeLock.release();		
	}
	
	protected void onResume(){
		super.onResume();
		wakeLock.acquire();
		 if(this != null && accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
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
    
    private void SaveDataToFile(){
			try {
				FileWriter fw = new FileWriter(workingFile, true);
				BufferedWriter out = new BufferedWriter(fw);
				
				for(DataPoint dp : dataPoints){
					out.write(dp.getX() + delimiter + dp.getY() + delimiter + dp.getZ() + delimiter + spinnerLocomotionActivity.getSelectedItem().toString() +"\n");
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
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
