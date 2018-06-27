package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.DriverAlertItisCode;
import com.trihydro.library.tables.TimOracleTables;

public class DriverAlertItisCodeService extends CvDataServiceLibrary {
    
	public static List<DriverAlertItisCode> selectAll() {
		
		List<DriverAlertItisCode> driverAlertItisCodes = new ArrayList<DriverAlertItisCode>();		
		Statement statement = null;
		Connection connection = null;	
		ResultSet rs = null;
		
		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			// build SQL statement			
			rs = statement.executeQuery("select * from DRIVER_ALERT_ITIS_CODE");
			
			// convert to DriverAlertItisCode objects   			
			while (rs.next()) {   			
				DriverAlertItisCode driverAlertItisCode = new DriverAlertItisCode();
				driverAlertItisCode.setDriverAlertId(rs.getInt("DRIVER_ALERT_ID"));
				driverAlertItisCode.setItisCodeId(rs.getInt("ITIS_CODE_ID"));									   
				driverAlertItisCodes.add(driverAlertItisCode);
			}			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {			
			try {
				// close prepared statement
				if(statement != null)
					statement.close();
				// return connection back to pool
				if(connection != null)
					connection.close();
				// close result set
				if(rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return driverAlertItisCodes;
    }
    
    public static Long insertDriverAlertItisCode(Long driverAlertId, Integer itisCodeId) { 

		PreparedStatement preparedStatement = null;
		Connection connection = null;

		try {

			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("driver_alert_itis_code", TimOracleTables.getDriverAlertItisCodeTable());			
			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"driver_alert_itis_code_id"});
			int fieldNum = 1;

			for(String col: TimOracleTables.getDriverAlertItisCodeTable()) {
				if(col.equals("ITIS_CODE_ID")) 
                    SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, itisCodeId);	
                else if(col.equals("DRIVER_ALERT_ID")) 
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, driverAlertId);													
				fieldNum++;
			}			

			Long driverAlertItisCodeId = log(preparedStatement, "driverAlertItisCode");		 		
			return driverAlertItisCodeId;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {			
			try {
				// close prepared statement
				if(preparedStatement != null)
					preparedStatement.close();
				// return connection back to pool
				if(connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return new Long(0);
    }
	 
}

