package dk.itu.activitytracker;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.*;

import com.google.appengine.api.datastore.*;


public class ActivityTracker extends HttpServlet {
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String test = req.getParameter("name");
		resp.setContentType("text/plain");
		resp.getWriter().println("Test test test" + req.getParameter("what"));
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String lat = req.getParameter("latitude");
		double latitude = Double.parseDouble(lat);
		double longitude = Double.parseDouble(req.getParameter("longitude"));		
		String user = req.getParameter("username");
		
		Key userKey = KeyFactory.createKey("User", user);
		Date date = new Date();
		Entity userEntity = new Entity("LocationData", userKey);
		userEntity.setProperty("date", date);
		userEntity.setProperty("username", user);
		userEntity.setProperty("latitude", latitude);
		userEntity.setProperty("longitude", longitude);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		datastore.put(userEntity);
		
		
		
		Key retrievalKey = KeyFactory.createKey("User", user);
		Query query = new Query("LocationData", retrievalKey);
		List<Entity> entities = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
		
		
		
	}

}
