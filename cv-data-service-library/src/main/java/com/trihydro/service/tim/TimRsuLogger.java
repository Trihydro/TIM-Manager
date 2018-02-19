package com.trihydro.service.tim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.service.CvDataLoggerLibrary;
import com.trihydro.service.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.service.tables.TimOracleTables;
import com.trihydro.service.model.TimRsu;

public class TimRsuLogger extends CvDataLoggerLibrary {

	static PreparedStatement preparedStatement = null;
	
    public static Long insertTimRsu(Long timId, Integer rsuId, Connection connection) { 
		try {
            TimOracleTables timOracleTables = new TimOracleTables();
            String insertQueryStatement = timOracleTables.buildInsertQueryStatement("TIM_RSU", timOracleTables.getTimRsuTable());		
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"TIM_RSU_ID"});
            int fieldNum = 1;            
			for(String col: timOracleTables.getTimRsuTable()) {
				if(col.equals("TIM_ID")) 
                    SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, timId);														
                else if(col.equals("RSU_ID"))
                    SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, rsuId);														               								
                fieldNum++;
			}			            
            Long timRsuId = log(preparedStatement, "tim rsu");		 		            
			return timRsuId;
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

	public static List<TimRsu> getTimRsusByTimId(Long timId, Connection connection){
		
		List<TimRsu> timRsus = new ArrayList<TimRsu>();
		
		try {
			// build SQL statement
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery("select * from TIM_RSU where tim_id = " + timId);
				try {
					// convert to DriverAlertType objects   			
					while (rs.next()) {   			
						TimRsu timRsu = new TimRsu();
						timRsu.setTimId(rs.getLong("TIM_ID"));
						timRsu.setRsuId(rs.getLong("RSU_ID"));							
						timRsus.add(timRsu);
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
		return timRsus;
	}

	public static TimRsu getTimRsu(Long timRsuId, Connection connection){
		
		TimRsu timRsu = new TimRsu();
		
		try {
			// build SQL statement
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery("select * from TIM_RSU where tim_rsu_id = " + timRsuId);
				try {
					// convert to DriverAlertType objects   			
					while (rs.next()) {   			
						timRsu.setTimRsuId(rs.getLong("TIM_RSU_ID"));												
						timRsu.setTimId(rs.getLong("TIM_ID"));
						timRsu.setRsuId(rs.getLong("RSU_ID"));																		
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
		return timRsu;
	}

	public static boolean deleteTimRsu(Long timRsuId, Connection connection){
		
		boolean deleteTimRsuResult = false;

		String deleteSQL = "DELETE FROM TIM_RSU WHERE TIM_RSU_ID = ?";

		try {			
		
			preparedStatement = connection.prepareStatement(deleteSQL);			
			preparedStatement.setLong(1, timRsuId);

			// execute delete SQL stetement
			deleteTimRsuResult = updateOrDelete(preparedStatement);

			System.out.println("TIM RSU is deleted!");

		}catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				preparedStatement.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}					
		}
		return deleteTimRsuResult;
	}
}

