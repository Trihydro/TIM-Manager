package com.trihydro.library.service;

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

    static PreparedStatement preparedStatement = null;
    
	public static List<DriverAlertItisCode> selectAll() {
		
		List<DriverAlertItisCode> driverAlertItisCodes = new ArrayList<DriverAlertItisCode>();
		
		try (Statement statement = DbUtility.getConnection().createStatement()) {
			// build SQL statement			
				ResultSet rs = statement.executeQuery("select * from DRIVER_ALERT_ITIS_CODE");
				try {
					// convert to DriverAlertItisCode objects   			
					while (rs.next()) {   			
						DriverAlertItisCode driverAlertItisCode = new DriverAlertItisCode();
						driverAlertItisCode.setDriverAlertId(rs.getInt("DRIVER_ALERT_ID"));
						driverAlertItisCode.setItisCodeId(rs.getInt("ITIS_CODE_ID"));									   
						driverAlertItisCodes.add(driverAlertItisCode);
					}
				}
				finally {
					try {
						rs.close();
					}
					catch (Exception e) {
						e.printStackTrace();
					}					
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return driverAlertItisCodes;
    }
    
    public static Long insertDriverAlertItisCode(Long driverAlertId, Integer itisCodeId) { 
		try {
			TimOracleTables timOracleTables = new TimOracleTables();
			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("driver_alert_itis_code", timOracleTables.getDriverAlertItisCodeTable());			
			preparedStatement = DbUtility.getConnection().prepareStatement(insertQueryStatement, new String[] {"driver_alert_itis_code_id"});
			int fieldNum = 1;
			for(String col: timOracleTables.getDriverAlertItisCodeTable()) {
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
				preparedStatement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new Long(0);
    }
	 
}

