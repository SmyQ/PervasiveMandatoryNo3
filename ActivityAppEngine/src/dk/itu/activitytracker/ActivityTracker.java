package dk.itu.activitytracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ActivityTracker extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String test = req.getParameter("name");
		resp.setContentType("text/plain");
		resp.getWriter().println("Test test test" + req.getParameter("what"));
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String data = req.getParameter("activity");
		
		Gson gson = new Gson();		
		JsonParser parser = new JsonParser();
		JsonObject object = parser.parse(data).getAsJsonObject();
		
		ArrayList<DataPoint> dataPoints = gson.fromJson(object.get("activity"), new TypeToken<ArrayList<DataPoint>>(){}.getType());
		
		
		
		
	}
	
	
	

	public class DataPoint {
		public float x;
		public float y;
		public float z;
		public Date d;
			
		public DataPoint(Date d, float x, float y, float z){
			this.x = x;
			this.y = y;
			this.z = z;
			this.d = d;
		}
		
		public float getX(){
			return x;
		}
		
		public float getY(){
			return y;
		}
		
		public float getZ(){
			return z;
		}
		
		public Date getDate(){
			return d;
		}
	}

	
	
//	String lat = req.getParameter("latitude");
//	double latitude = Double.parseDouble(lat);
//	double longitude = Double.parseDouble(req.getParameter("longitude"));		
//	String user = req.getParameter("username");
//	
//	Key userKey = KeyFactory.createKey("User", user);
//	Date date = new Date();
//	Entity userEntity = new Entity("LocationData", userKey);
//	userEntity.setProperty("date", date);
//	userEntity.setProperty("username", user);
//	userEntity.setProperty("latitude", latitude);
//	userEntity.setProperty("longitude", longitude);
//	
//	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
//	datastore.put(userEntity);
//	
//	
//	
//	Key retrievalKey = KeyFactory.createKey("User", user);
//	Query query = new Query("LocationData", retrievalKey);
//	List<Entity> entities = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));

}
