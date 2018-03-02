package com.trihydro.library.service.milepost;

import com.trihydro.library.model.Milepost;
import com.trihydro.library.service.CvDataLoggerLibrary;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class MilepostService extends CvDataLoggerLibrary {

	// select all mileposts
	public static List<Milepost> selectAll(Connection connection) {

		List<Milepost> mileposts = new ArrayList<Milepost>();

		try {
			// build statement SQL query
   		    Statement statement = connection.createStatement();
   			ResultSet rs = statement.executeQuery("select * from MILEPOST_VW where MOD(milepost, 1) = 0 order by milepost asc");
   			// convert result to milepost objects
   			while (rs.next()) {   				
			    Milepost milepost = new Milepost();
			    //milepost.setMilepostId(rs.getInt("milepost_id"));
	    	    milepost.setRoute(rs.getString("route"));
	    	    milepost.setMilepost(rs.getDouble("milepost"));
			    milepost.setDirection(rs.getString("direction"));	
			    milepost.setLatitude(rs.getDouble("latitude"));
			    milepost.setLongitude(rs.getDouble("longitude"));
			    milepost.setElevation(rs.getDouble("elevation_ft"));	
			    milepost.setBearing(rs.getDouble("bearing"));
			    mileposts.add(milepost);
   			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return mileposts;
	}	

	public static List<Milepost> getMilepostsRoute(String route, Connection connection) {
		
				List<Milepost> mileposts = new ArrayList<Milepost>();
		
				try {
					// build statement SQL query
					   Statement statement = connection.createStatement();
					   ResultSet rs = statement.executeQuery("select * from MILEPOST_VW where route like '%Prairie%' OR route like '%Field%'" );
					   // convert result to milepost objects
					   while (rs.next()) {   				
						Milepost milepost = new Milepost();
						//milepost.setMilepostId(rs.getInt("milepost_id"));
						milepost.setRoute(rs.getString("route"));
						milepost.setMilepost(rs.getDouble("milepost"));
						milepost.setDirection(rs.getString("direction"));	
						milepost.setLatitude(rs.getDouble("latitude"));
						milepost.setLongitude(rs.getDouble("longitude"));
						milepost.setElevation(rs.getDouble("elevation_ft"));	
						milepost.setBearing(rs.getDouble("bearing"));
						mileposts.add(milepost);
					   }
				  } 
				  catch (SQLException e) {
					   e.printStackTrace();
				  }
				  return mileposts;
			}	

	// select all mileposts within a range in one direction
	public static List<Milepost> selectMilepostRange(String direction, String route, Double lowerMilepost, Double higherMilepost, Connection connection) {

		List<Milepost> mileposts = new ArrayList<Milepost>();		
		
		try {
			// build SQL query
			Statement statement = connection.createStatement();
			String statementStr = "select * from MILEPOST_VW where direction = '" + direction + "' and milepost >= " + lowerMilepost + " and milepost <= "+ higherMilepost + " and route like '%" + route + "%'";
   		    ResultSet rs;
   		    if(direction.toLowerCase().equals("eastbound"))
				rs = statement.executeQuery(statementStr + "order by milepost asc");
			else if(direction.toLowerCase().equals("westbound"))	
				rs = statement.executeQuery(statementStr + "order by milepost desc");			
			else
				return mileposts;
   			// convert result to milepost objects
   			while (rs.next()) {   				
			    Milepost milepost = new Milepost();
	    	    milepost.setRoute(rs.getString("route"));
	    	    milepost.setMilepost(rs.getDouble("milepost"));
			    milepost.setDirection(rs.getString("direction"));	
			    milepost.setLatitude(rs.getDouble("latitude"));
			    milepost.setLongitude(rs.getDouble("longitude"));
			    milepost.setElevation(rs.getDouble("elevation_ft"));	
			    milepost.setBearing(rs.getDouble("bearing"));
			    mileposts.add(milepost);
   			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return mileposts;
	}
	
	// select all mileposts within a range in one direction
	public static List<Milepost> selectMilepostTestRange(String direction, String route, Double lowerMilepost, Double higherMilepost, Connection connection) {

		List<Milepost> mileposts = new ArrayList<Milepost>();		
		
		try {
			// build SQL query
			Statement statement = connection.createStatement();
			String statementStr = "select * from MILEPOST_TEST where direction = '" + direction + "' and milepost >= " + lowerMilepost + " and milepost <= "+ higherMilepost + " and route like '%" + route + "%'";
   		    ResultSet rs;
   		    if(direction.toLowerCase().equals("southbound"))
				rs = statement.executeQuery(statementStr + "order by milepost asc");			
			else
				return mileposts;
   			// convert result to milepost objects
   			while (rs.next()) {   				
			    Milepost milepost = new Milepost();
	    	    milepost.setRoute(rs.getString("route"));
	    	    milepost.setMilepost(rs.getDouble("milepost"));
			    milepost.setDirection(rs.getString("direction"));	
			    milepost.setLatitude(rs.getDouble("latitude"));
			    milepost.setLongitude(rs.getDouble("longitude"));
			    milepost.setElevation(rs.getDouble("elevation_ft"));	
			    milepost.setBearing(rs.getDouble("bearing"));
			    mileposts.add(milepost);
   			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return mileposts;
	}

	// select all mileposts within a range in one direction
	public static List<Milepost> selectMilepostRangeNoDirection(String route, Double lowerMilepost, Double higherMilepost, Connection connection) {

		List<Milepost> mileposts = new ArrayList<Milepost>();		
		
		try {
			// build SQL query
			Statement statement = connection.createStatement();
			String statementStr = "select * from MILEPOST_VW where milepost >= " + lowerMilepost + " and milepost <= "+ higherMilepost + " and route like '%" + route + "%'";
				ResultSet rs;
				rs = statement.executeQuery(statementStr + "order by milepost asc");
			
				// convert result to milepost objects
				while (rs.next()) {   				
				Milepost milepost = new Milepost();
				milepost.setRoute(rs.getString("route"));
				milepost.setMilepost(rs.getDouble("milepost"));
				milepost.setDirection(rs.getString("direction"));	
				milepost.setLatitude(rs.getDouble("latitude"));
				milepost.setLongitude(rs.getDouble("longitude"));
				milepost.setElevation(rs.getDouble("elevation_ft"));	
				milepost.setBearing(rs.getDouble("bearing"));
				mileposts.add(milepost);
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
			return mileposts;
	}	


	// select all mileposts
	public static List<Milepost> selectAllTest(Connection connection) {

		List<Milepost> mileposts = new ArrayList<Milepost>();

		try {
			// build statement SQL query
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery("select * from MILEPOST_TEST order by milepost asc");
				// convert result to milepost objects
				while (rs.next()) {   				
				Milepost milepost = new Milepost();
				//milepost.setMilepostId(rs.getInt("milepost_id"));
				milepost.setRoute(rs.getString("route"));
				milepost.setMilepost(rs.getDouble("milepost"));
				milepost.setDirection(rs.getString("direction"));	
				milepost.setLatitude(rs.getDouble("latitude"));
				milepost.setLongitude(rs.getDouble("longitude"));
				milepost.setElevation(rs.getDouble("elevation_ft"));	
				milepost.setBearing(rs.getDouble("bearing"));
				mileposts.add(milepost);
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return mileposts;
	}	
}
