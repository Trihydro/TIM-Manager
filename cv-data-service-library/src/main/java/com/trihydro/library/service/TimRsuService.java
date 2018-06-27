package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.tables.TimOracleTables;
import com.trihydro.library.model.TimRsu;

public class TimRsuService extends CvDataServiceLibrary {	
	
    public static Long insertTimRsu(Long timId, Integer rsuId) { 

		PreparedStatement preparedStatement = null;
		Connection connection = null;

		try {
            connection = DbUtility.getConnectionPool();
            String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("TIM_RSU", TimOracleTables.getTimRsuTable());		
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"TIM_RSU_ID"});
			int fieldNum = 1;            
			
			for(String col: TimOracleTables.getTimRsuTable()) {
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

	public static List<TimRsu> getTimRsusByTimId(Long timId){
		
		Statement statement = null;
		Connection connection = null;
		ResultSet rs = null;
		List<TimRsu> timRsus = new ArrayList<TimRsu>();
		
		try  {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			// build SQL statement
			rs = statement.executeQuery("select * from TIM_RSU where tim_id = " + timId);
			
			// convert to DriverAlertType objects   			
			while (rs.next()) {   			
				TimRsu timRsu = new TimRsu();
				timRsu.setTimId(rs.getLong("TIM_ID"));
				timRsu.setRsuId(rs.getLong("RSU_ID"));							
				timRsus.add(timRsu);
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
		return timRsus;
	}

	public static List<TimRsu> selectAll(){
		
		Statement statement = null;
		Connection connection = null;
		ResultSet rs = null;
		List<TimRsu> timRsus = new ArrayList<TimRsu>();
		
		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			// select all RSUs from RSU table
   			rs = statement.executeQuery("select * from TIM_RSU");
			   
			while (rs.next()) {
				TimRsu timRsu = new TimRsu();
				timRsu.setTimId(rs.getLong("TIM_ID"));
				timRsu.setRsuId(rs.getLong("RSU_ID"));							
				timRsus.add(timRsu);
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
  		return timRsus;
	}
	
	public static TimRsu getTimRsu(Long timRsuId){
		
		Statement statement = null;
		Connection connection = null;
		ResultSet rs = null;
		TimRsu timRsu = new TimRsu();
		
		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			// build SQL statement
			rs = statement.executeQuery("select * from TIM_RSU where tim_rsu_id = " + timRsuId);
			
			// convert to DriverAlertType objects   			
			while (rs.next()) {   			
				timRsu.setTimRsuId(rs.getLong("TIM_RSU_ID"));												
				timRsu.setTimId(rs.getLong("TIM_ID"));
				timRsu.setRsuId(rs.getLong("RSU_ID"));																		
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
		return timRsu;
	}

	public static boolean deleteTimRsu(Long timRsuId){
		
		boolean deleteTimRsuResult = false;
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		ResultSet rs = null;

		String deleteSQL = "DELETE FROM TIM_RSU WHERE TIM_RSU_ID = ?";

		try {			
			connection = DbUtility.getConnectionPool();
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
		return deleteTimRsuResult;
	}
}

