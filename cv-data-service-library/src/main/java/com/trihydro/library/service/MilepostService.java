package com.trihydro.library.service;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.service.CvDataServiceLibrary;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;

public class MilepostService extends CvDataServiceLibrary {

	// select all mileposts
	public static List<Milepost> selectAll() {

		List<Milepost> mileposts = new ArrayList<Milepost>();
		
		try {
			// build statement SQL query
			String sqlQuery = "select * from MILEPOST_VW where MOD(milepost, 1) = 0 order by milepost asc";			
			ResultSet rs = statement.executeQuery(sqlQuery);

			try{
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
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
		}		
		  
  		return mileposts;
	}	

	public static List<Milepost> getMilepostsRoute(String route, Boolean mod) {
		
		List<Milepost> mileposts = new ArrayList<Milepost>();

		try {
			// build statement SQL query		
			String sqlString = "select * from MILEPOST_VW where route like '%" + route + "%'" ;

			if(mod)
				sqlString += " and MOD(milepost, 1) = 0";
			
			ResultSet rs = statement.executeQuery(sqlString);

			// convert result to milepost objects
			try{
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
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}		
		return mileposts;
	}	

	// select all mileposts within a range in one direction
	public static List<Milepost> selectMilepostRange(String direction, String route, Double fromMilepost, Double toMilepost) {

		List<Milepost> mileposts = new ArrayList<Milepost>();		
		
		try {
			// build SQL query
			String statementStr = "select * from MILEPOST_VW where direction = '" + direction + "' and milepost between " + Math.min(fromMilepost, toMilepost) + " and "+ Math.max(fromMilepost, toMilepost) + " and route like '%" + route + "%'";
			ResultSet rs = null;
			
			try {
				if(fromMilepost < toMilepost)
					rs = statement.executeQuery(statementStr + "order by milepost asc");
				else 
					rs = statement.executeQuery(statementStr + "order by milepost desc");					
				
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
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return mileposts;
	}
	
	// select all mileposts within a range in one direction
	public static List<Milepost> selectMilepostTestRange(String direction, String route, Double fromMilepost, Double toMilepost) {

		List<Milepost> mileposts = new ArrayList<Milepost>();		
		
		try {
			// build SQL query			
			String statementStr = "select * from MILEPOST_TEST where direction = '" + direction + "' and milepost between " + Math.min(fromMilepost, toMilepost) + " and "+ Math.max(fromMilepost, toMilepost) + " and route like '%" + route + "%'";
			ResultSet rs = null;
			   
			try {
				if(fromMilepost < toMilepost)
					rs = statement.executeQuery(statementStr + "order by milepost asc");
				else 
					rs = statement.executeQuery(statementStr + "order by milepost desc");		
				
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
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return mileposts;
	}

	// select all mileposts within a range in one direction
	public static List<Milepost> selectMilepostRangeNoDirection(String route, Double fromMilepost, Double toMilepost) {

		List<Milepost> mileposts = new ArrayList<Milepost>();		
		
		try {
			// build SQL query			
			String statementStr = "select * from MILEPOST_VW where milepost between " + Math.min(fromMilepost, toMilepost) + " and "+ Math.max(fromMilepost, toMilepost) + " and route like '%" + route + "%'";
				ResultSet rs = null;

				try{
					rs = statement.executeQuery(statementStr + "order by milepost asc");
				
					if(fromMilepost < toMilepost)
						rs = statement.executeQuery(statementStr + "order by milepost asc");
					else 
						rs = statement.executeQuery(statementStr + "order by milepost desc");	

					// convert result to milepost objects
					while (rs.next()) {   				
						Milepost milepost = new Milepost();
						milepost.setRoute(rs.getString("route"));
						milepost.setMilepost(rs.getDouble("milepost"));
						milepost.setLatitude(rs.getDouble("latitude"));
						milepost.setLongitude(rs.getDouble("longitude"));
						milepost.setElevation(rs.getDouble("elevation_ft"));	
						milepost.setBearing(rs.getDouble("bearing"));
						mileposts.add(milepost);
					}
				}
				finally {
					try { rs.close(); } catch (Exception ignore) { }
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
			return mileposts;
	}	


	// select all mileposts
	public static List<Milepost> selectAllTest() {

		List<Milepost> mileposts = new ArrayList<Milepost>();

		try {
			// build statement SQL query
				ResultSet rs = statement.executeQuery("select * from MILEPOST_TEST order by milepost asc");
				// convert result to milepost objects
				try{
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
				finally {
					try { rs.close(); } catch (Exception ignore) { }
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return mileposts;
	}	
}
