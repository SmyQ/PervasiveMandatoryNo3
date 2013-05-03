package dk.itu.activitytracker;

import java.util.List;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;





public class TestClass {

	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		String latitude = URLEncoder.encode("30.0", "UTF-8");
		String longitude = URLEncoder.encode("50.0", "UTF-8");
		String username = URLEncoder.encode("MMR", "UTF-8");
		
		try{
			URL url = new URL("http://10.0.2.2:8888/activitytracker");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");

            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write("latitude=" + latitude);
            writer.write("longitude=" + longitude);
            writer.write("username=" + username);
            writer.close();
    
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // OK
            } else {
                // Server returned HTTP error code.
            }

		}
		catch(Exception e){
			System.out.println("Error: " + e.getMessage());
		}

	}

}
