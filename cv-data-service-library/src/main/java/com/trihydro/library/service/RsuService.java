package com.trihydro.library.service;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.CvDataServiceLibrary;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import org.springframework.stereotype.Component;

@Component
public class RsuService extends CvDataServiceLibrary {
	
	public static List<WydotRsu> selectAll(){
		List<WydotRsu> rsus = new ArrayList<WydotRsu>();
		try (Statement statement = DbUtility.getConnection().createStatement()) {
			// select all RSUs from RSU table
   			ResultSet rs = statement.executeQuery("select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid");
			try{   
				while (rs.next()) {
					WydotRsu rsu = new WydotRsu();
					rsu.setRsuId(rs.getInt("rsu_id"));
					rsu.setRsuTarget(rs.getString("ipv4_address"));
					rsu.setRsuUsername(rs.getString("rsu_username"));    
					rsu.setRsuPassword(rs.getString("rsu_password"));
					rsu.setLatitude(rs.getDouble("latitude"));
					rsu.setLongitude(rs.getDouble("longitude"));
					rsu.setRoute(rs.getString("route"));
					rsu.setMilepost(rs.getDouble("milepost"));
					rsus.add(rsu);
				}
			}
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return rsus;
	}

	public static List<WydotRsu> selectActiveRSUs(){
		List<WydotRsu> rsus = new ArrayList<WydotRsu>();
		try {
			// select all RSUs that are labeled as 'Existing' in the WYDOT view
   		    Statement statement = DbUtility.getConnection().createStatement();
   			ResultSet rs = statement.executeQuery("select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing'");
			try{   
				while (rs.next()) {
					WydotRsu rsu = new WydotRsu();
					//rsu.setRsuId(rs.getInt("rsu_id"));
					rsu.setRsuTarget(rs.getString("ipv4_address"));
					rsu.setRsuUsername(rs.getString("rsu_username"));    
					rsu.setRsuPassword(rs.getString("rsu_password"));
					rsu.setLatitude(rs.getDouble("latitude"));
					rsu.setLongitude(rs.getDouble("longitude"));
					rsus.add(rsu);
				}
			}
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return rsus;
	}

	public static WydotRsu getRsu(Long rsuId, Connection connection){
		WydotRsu rsu = new WydotRsu();
		try {
			// select all RSUs that are labeled as 'Existing' in the WYDOT view
   		    Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_id = " + rsuId);
			try{   
				while (rs.next()) {			    
					//rsu.setRsuId(rs.getInt("rsu_id"));
					rsu.setRsuTarget(rs.getString("ipv4_address"));
					rsu.setRsuUsername(rs.getString("rsu_username"));    
					rsu.setRsuPassword(rs.getString("rsu_password"));		
				}
			}
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return rsu;
	}

	public static List<WydotRsu> selectRsusInBuffer(String direction, Double lowerMilepost, Double higherMilepost){
		
		List<WydotRsu> rsus = new ArrayList<WydotRsu>();
		// System.out.println(env.getProperty("rsuMileageBuffer"));
		// int buffer = Integer.parseInt(env.getProperty("rsuMileageBuffer"));
		int buffer = 5;
		
		try (Statement statement = DbUtility.getConnection().createStatement()) {
			// select all RSUs that are labeled as 'Existing' in the WYDOT view   		    
			ResultSet rs = null;

			try{   
				if(direction.toLowerCase().equals("eastbound")) {		
					Double startBuffer = lowerMilepost - buffer;			
					rs = statement.executeQuery("select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing' and rsu_vw.milepost >= " + startBuffer + " and rsu_vw.milepost <= " + higherMilepost + " and rsu_vw.route like '%80%'" );
				}
				else {
					Double startBuffer = higherMilepost + buffer;			
					rs = statement.executeQuery("select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing' and rsu_vw.milepost >= " + lowerMilepost + "and rsu_vw.milepost <= " + startBuffer + " and rsu_vw.route like '%80%'");
				}

				while (rs.next()) {
					WydotRsu rsu = new WydotRsu();
					rsu.setRsuId(rs.getInt("rsu_id"));
					rsu.setRsuTarget(rs.getString("ipv4_address"));
					rsu.setRsuUsername(rs.getString("rsu_username"));    
					rsu.setRsuPassword(rs.getString("rsu_password"));
					rsu.setLatitude(rs.getDouble("latitude"));
					rsu.setLongitude(rs.getDouble("longitude"));
					rsus.add(rsu);
				}
			}
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return rsus;
	}
}