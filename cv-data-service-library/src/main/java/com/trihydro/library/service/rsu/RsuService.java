package com.trihydro.library.service.rsu;

import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.helpers.SQLNullHandler;
import com.trihydro.library.service.CvDataLoggerLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import org.springframework.stereotype.Component;

@Component
public class RsuService extends CvDataLoggerLibrary {

	// @Autowired
	// public Environment env;

	// @Autowired
    // static DataSource dataSource;

	public static List<WydotRsu> selectAll(Connection connection){
		List<WydotRsu> rsus = new ArrayList<WydotRsu>();
		try {
			// select all RSUs from RSU table
   		    Statement statement = connection.createStatement();
   			ResultSet rs = statement.executeQuery("select * from rsu_vw");
   			while (rs.next()) {
			    WydotRsu rsu = new WydotRsu();
			    // rsu.setRsuId(rs.getInt("rsu_id"));
			    // rsu.setRsuTarget(rs.getString("url"));
			    // rsu.setRsuUsername(rs.getString("rsu_username"));    
			    // rsu.setRsuPassword(rs.getString("rsu_password"));
			    // rsu.setSnmpUsername(rs.getString("snmp_username"));
			    // rsu.setSnmpPassword(rs.getString("snmp_password"));
			    rsu.setLatitude(rs.getDouble("latitude"));
			    rsu.setLongitude(rs.getDouble("longitude"));
			    rsus.add(rsu);
   			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return rsus;
	}

	public static List<WydotRsu> selectActiveRSUs(Connection connection){
		List<WydotRsu> rsus = new ArrayList<WydotRsu>();
		try {
			// select all RSUs that are labeled as 'Existing' in the WYDOT view
   		    Statement statement = connection.createStatement();
   			ResultSet rs = statement.executeQuery("select rsu.*, rsu_vw.latitude, rsu_vw.longitude from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing'");
   			while (rs.next()) {
			    WydotRsu rsu = new WydotRsu();
			    //rsu.setRsuId(rs.getInt("rsu_id"));
			    rsu.setRsuTarget(rs.getString("url"));
			    rsu.setRsuUsername(rs.getString("rsu_username"));    
			    rsu.setRsuPassword(rs.getString("rsu_password"));
			    rsu.setLatitude(rs.getDouble("latitude"));
			    rsu.setLongitude(rs.getDouble("longitude"));
			    rsus.add(rsu);
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
   			ResultSet rs = statement.executeQuery("select rsu.* from rsu where rsu_id = " + rsuId);
   			while (rs.next()) {			    
			    //rsu.setRsuId(rs.getInt("rsu_id"));
			    rsu.setRsuTarget(rs.getString("url"));
			    rsu.setRsuUsername(rs.getString("rsu_username"));    
			    rsu.setRsuPassword(rs.getString("rsu_password"));		
   			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return rsu;
	}

	public static List<WydotRsu> selectRsusInBuffer(String direction, Double lowerMilepost, Double higherMilepost, Connection connection){
		
		List<WydotRsu> rsus = new ArrayList<WydotRsu>();
		// System.out.println(env.getProperty("rsuMileageBuffer"));
		// int buffer = Integer.parseInt(env.getProperty("rsuMileageBuffer"));
		int buffer = 5;
		
		try {
			// select all RSUs that are labeled as 'Existing' in the WYDOT view
   		    Statement statement = connection.createStatement();

   			ResultSet rs;
			if(direction.toLowerCase().equals("eastbound")) {		
				Double startBuffer = lowerMilepost - buffer;			
				rs = statement.executeQuery("select rsu.*, rsu_vw.latitude, rsu_vw.longitude from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing' and rsu_vw.milepost >= " + startBuffer + " and rsu_vw.milepost <= " + higherMilepost + " and rsu_vw.route like '%80%'" );
			}
			else {
				Double startBuffer = higherMilepost + buffer;			
				rs = statement.executeQuery("select rsu.*, rsu_vw.latitude, rsu_vw.longitude from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing' and rsu_vw.milepost >= " + lowerMilepost + "and rsu_vw.milepost <= " + startBuffer + " and rsu_vw.route like '%80%'");
			}

   			while (rs.next()) {
			    WydotRsu rsu = new WydotRsu();
			    rsu.setRsuId(rs.getInt("rsu_id"));
			    rsu.setRsuTarget(rs.getString("url"));
			    rsu.setRsuUsername(rs.getString("rsu_username"));    
			    rsu.setRsuPassword(rs.getString("rsu_password"));
			    rsu.setLatitude(rs.getDouble("latitude"));
			    rsu.setLongitude(rs.getDouble("longitude"));
			    rsus.add(rsu);
   			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return rsus;
	}

	public static void addRSU(WydotRsu rsu, Connection connection) {
		try {
			
			PreparedStatement preparedStatement = connection.prepareStatement("insert into rsu(url, rsu_username, rsu_password, snmp_username, snmp_password) values (?,?,?,?,?)");
	  
			SQLNullHandler.setStringOrNull(preparedStatement, 1, rsu.getRsuTarget());
			SQLNullHandler.setStringOrNull(preparedStatement, 2, rsu.getRsuUsername());
			SQLNullHandler.setStringOrNull(preparedStatement, 3, rsu.getRsuPassword());
					
			preparedStatement.executeUpdate();

	  } catch (SQLException e) {
	   e.printStackTrace();
	  }
    }
}