package com.trihydro.library.service.tim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.library.service.CvDataLoggerLibrary;
import com.trihydro.library.service.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import com.trihydro.library.service.tables.TimOracleTables;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.model.ActiveTim;

public class ActiveTimLogger extends CvDataLoggerLibrary {

	static PreparedStatement preparedStatement = null;
	
    public static Long insertActiveTim(Long timID, Double milepostStart, Double milepostStop, String direction, Long timTypeId, String startDateTime, String endDateTime, String route, String clientId, Connection connection) { 
		try {
			TimOracleTables timOracleTables = new TimOracleTables();
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("active_tim", timOracleTables.getActiveTimTable());			
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"active_tim_id"});
			int fieldNum = 1;
			for(String col: timOracleTables.getActiveTimTable()) {
				if(col.equals("TIM_ID")) 
                    SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, timID);														
                else if(col.equals("MILEPOST_START"))
                    SQLNullHandler.setDoubleOrNull(preparedStatement, fieldNum, milepostStart);														
                else if(col.equals("MILEPOST_STOP"))
                    SQLNullHandler.setDoubleOrNull(preparedStatement, fieldNum, milepostStop);														               												
                else if(col.equals("DIRECTION"))
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, direction);														
				else if(col.equals("TIM_START")){				
					SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(LocalDateTime.parse(startDateTime, DateTimeFormatter.ISO_DATE_TIME)));							
				}
				else if(col.equals("TIM_END"))
					if (endDateTime != null)
						SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(LocalDateTime.parse(endDateTime, DateTimeFormatter.ISO_DATE_TIME)));							
						//SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(endDateTime));							
					else
						preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
                else if(col.equals("TIM_TYPE_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, timTypeId);	
				else if(col.equals("ROUTE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, route);	
				else if(col.equals("CLIENT_ID"))
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, clientId);														

                fieldNum++;
			}			
			
			Long activeTimId = log(preparedStatement, "active tim");		 		
			return activeTimId;
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

	public static boolean updateActiveTimTimId(Long activeTimId, Long timId, Connection connection){
		boolean activeTimIdResult = false;
		String updateTableSQL = "UPDATE ACTIVE_TIM SET TIM_ID = ? "
		+ " WHERE ACTIVE_TIM_ID = ?";

		try {
			preparedStatement = connection.prepareStatement(updateTableSQL);
			SQLNullHandler.setLongOrNull(preparedStatement, 1, timId);	
			SQLNullHandler.setLongOrNull(preparedStatement, 2, activeTimId);	
			
			activeTimIdResult = updateOrDelete(preparedStatement);							
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

		return activeTimIdResult;

	}

	public static boolean updateActiveTimEndDate(Long activeTimId, String endDateTime, Connection connection){
		boolean activeTimIdResult = false;
		String updateTableSQL = "UPDATE ACTIVE_TIM SET TIM_END = ? "
		+ " WHERE ACTIVE_TIM_ID = ?";

		try {
			preparedStatement = connection.prepareStatement(updateTableSQL);
			SQLNullHandler.setTimestampOrNull(preparedStatement, 1, java.sql.Timestamp.valueOf(LocalDateTime.parse(endDateTime, DateTimeFormatter.ISO_DATE_TIME)));										
			SQLNullHandler.setLongOrNull(preparedStatement, 2, activeTimId);	
			
			activeTimIdResult = updateOrDelete(preparedStatement);							
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

		return activeTimIdResult;

	}

	public static List<ActiveTim> getActiveTims(Double milepostStart, Double milepostStop, Long timTypeId, String direction, Connection connection){
		
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where MILEPOST_START = " + milepostStart + " and MILEPOST_STOP = " + milepostStop + " and TIM_TYPE_ID = " + timTypeId + " and DIRECTION = '" + direction + "'");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTims.add(activeTim);				
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

		return activeTims;
	}

	public static ActiveTim getActiveTim(Long activeTimId, Connection connection){

		ActiveTim activeTim = null;
		
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where ACTIVE_TIM_ID = '" + activeTimId + "'");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));									
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

		return activeTim;
	}

	public static List<ActiveTim> getActiveTimsByClientId(String clientId, Connection connection){
		
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		
		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where CLIENT_ID = '" + clientId + "'");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTims.add(activeTim);												
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

		return activeTims;
	}		

	public static boolean deleteActiveTim(Long activeTimId, Connection connection){
		
		boolean deleteActiveTimResult = false;

		String deleteSQL = "DELETE FROM ACTIVE_TIM WHERE ACTIVE_TIM_ID = ?";

		try {			
		
			preparedStatement = connection.prepareStatement(deleteSQL);			
			preparedStatement.setLong(1, activeTimId);

			// execute delete SQL stetement
			deleteActiveTimResult = updateOrDelete(preparedStatement);

			System.out.println("Active Tim is deleted!");

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
		return deleteActiveTimResult;
	}
}

