package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import com.trihydro.library.tables.TimOracleTables;

import org.apache.ibatis.jdbc.SqlBuilder;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.model.ActiveTim;

public class ActiveTimService extends CvDataServiceLibrary {

	static PreparedStatement preparedStatement = null;
	
    public static Long insertActiveTim(Long timID, Double milepostStart, Double milepostStop, String direction, Long timTypeId, String startDateTime, String endDateTime, String route, String clientId, String satRecordId) { 
		try {
			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("active_tim", TimOracleTables.getActiveTimTable());			
			preparedStatement = DbUtility.getConnection().prepareStatement(insertQueryStatement, new String[] {"active_tim_id"});
			int fieldNum = 1;
			for(String col: TimOracleTables.getActiveTimTable()) {
				if(col.equals("TIM_ID")) 
                    SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, timID);														
                else if(col.equals("MILEPOST_START"))
                    SQLNullHandler.setDoubleOrNull(preparedStatement, fieldNum, milepostStart);														
                else if(col.equals("MILEPOST_STOP"))
                    SQLNullHandler.setDoubleOrNull(preparedStatement, fieldNum, milepostStop);														               												
                else if(col.equals("DIRECTION"))
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, direction);														
				else if(col.equals("TIM_START"))																						
					SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(LocalDateTime.parse(startDateTime, DateTimeFormatter.ISO_DATE_TIME)));											
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
				else if(col.equals("SAT_RECORD_ID"))
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, satRecordId);														

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

	public static boolean updateActiveTimTimId(Long activeTimId, Long timId){
		boolean activeTimIdResult = false;
		String updateTableSQL = "UPDATE ACTIVE_TIM SET TIM_ID = ? "
		+ " WHERE ACTIVE_TIM_ID = ?";

		try {
			preparedStatement = DbUtility.getConnection().prepareStatement(updateTableSQL);
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

	public static boolean updateActiveTimEndDate(Long activeTimId, String endDateTime){
		boolean activeTimIdResult = false;
		String updateTableSQL = "UPDATE ACTIVE_TIM SET TIM_END = ? "
		+ " WHERE ACTIVE_TIM_ID = ?";

		try {
			preparedStatement = DbUtility.getConnection().prepareStatement(updateTableSQL);
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

	public static List<ActiveTim> getAllActiveTims(Double milepostStart, Double milepostStop, Long timTypeId, String direction){
		
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

		try {
			Statement statement = DbUtility.getConnection().createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where MILEPOST_START = " + milepostStart + " and MILEPOST_STOP = " + milepostStop + " and TIM_TYPE_ID = " + timTypeId + " and DIRECTION = '" + direction + "'");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));	
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

	public static List<ActiveTim> getActiveRsuTims(Double milepostStart, Double milepostStop, Long timTypeId, String direction){
		
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

		try {
			Statement statement = DbUtility.getConnection().createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where MILEPOST_START = " + milepostStart + " and MILEPOST_STOP = " + milepostStop + " and TIM_TYPE_ID = " + timTypeId + " and DIRECTION = '" + direction + "' and SAT_RECORD_ID is null");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));	
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

	public static List<ActiveTim> getActiveSatTims(Double milepostStart, Double milepostStop, Long timTypeId, String direction){
		
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

		try {
			Statement statement = DbUtility.getConnection().createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where MILEPOST_START = " + milepostStart + " and MILEPOST_STOP = " + milepostStop + " and TIM_TYPE_ID = " + timTypeId + " and DIRECTION = '" + direction + "' and SAT_RECORD_ID is not null");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));	
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

	public static ActiveTim getActiveTimByClientId(String clientId, Long timTypeId){

		ActiveTim activeTim = null;
		
		try {
			Statement statement = DbUtility.getConnection().createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where CLIENT_ID = '" + clientId + "' and TIM_TYPE_ID = " + timTypeId);
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));								
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


	public static ActiveTim getActiveTim(Long activeTimId){

		ActiveTim activeTim = null;
		
		try {
			Statement statement = DbUtility.getConnection().createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where ACTIVE_TIM_ID = '" + activeTimId + "'");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));								
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

	public static List<ActiveTim> getActiveTimsByClientId(String clientId){
		
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		
		try {
			Statement statement = DbUtility.getConnection().createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where CLIENT_ID = '" + clientId + "'");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));
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

	public static List<ActiveTim> getActiveRSUTimsByClientId(String clientId){
		
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		
		try {
			Statement statement = DbUtility.getConnection().createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where CLIENT_ID = '" + clientId + "' and SAT_RECORD_ID is null");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));
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
	
	public static List<ActiveTim> getActiveSATTimsByClientId(String clientId){
		
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		
		try {
			Statement statement = DbUtility.getConnection().createStatement();
			ResultSet rs = statement.executeQuery("select * from active_tim where CLIENT_ID = '" + clientId + "' and SAT_RECORD_ID is not null");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));
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

	public static List<ActiveTim> getActiveTimsOnRsu(String ipv4Address){
			
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		
		try {
			Statement statement = DbUtility.getConnection().createStatement();

			String selectStatement = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, SAT_RECORD_ID, MILEPOST_START, MILEPOST_STOP, TYPE from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
			selectStatement += " inner join tim_rsu on tim_rsu.rsu_id = rsu.rsu_id";
			selectStatement += " inner join tim on tim.tim_id = tim_rsu.tim_id";
			selectStatement += " inner join active_tim on active_tim.tim_id = tim.tim_id";
			selectStatement += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
			selectStatement += " where rsu_vw.ipv4_address = '" +  ipv4Address + "'";
			
			ResultSet rs = statement.executeQuery(selectStatement);

			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));
					activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
					activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
					activeTim.setTimType(rs.getString("TYPE"));
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

	public static List<ActiveTim> getActiveRCTimsOnRsu(String ipv4Address, Double fromRm, Double toRm, String direction){
			
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		
		try {
			Statement statement = DbUtility.getConnection().createStatement();

			String selectStatement = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, SAT_RECORD_ID, MILEPOST_START, MILEPOST_STOP, TYPE from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
			selectStatement += " inner join tim_rsu on tim_rsu.rsu_id = rsu.rsu_id";
			selectStatement += " inner join tim on tim.tim_id = tim_rsu.tim_id";
			selectStatement += " inner join active_tim on active_tim.tim_id = tim.tim_id";
			selectStatement += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
			selectStatement += " where rsu_vw.ipv4_address = '" +  ipv4Address + "'";
			selectStatement += " and milepost_start = " +  fromRm;
			selectStatement += " and milepost_stop = " +  toRm;
			selectStatement += " and active_tim.direction = '" +  direction + "'";		
			selectStatement += " and type = 'RC'";		
			
			ResultSet rs = statement.executeQuery(selectStatement);

			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));
					activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
					activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
					activeTim.setTimType(rs.getString("TYPE"));
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

	public static List<ActiveTim> getActiveCCTimsOnRsu(String ipv4Address, String clientId){
			
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		
		try {
			Statement statement = DbUtility.getConnection().createStatement();

			String selectStatement = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, SAT_RECORD_ID, MILEPOST_START, MILEPOST_STOP, TYPE from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
			selectStatement += " inner join tim_rsu on tim_rsu.rsu_id = rsu.rsu_id";
			selectStatement += " inner join tim on tim.tim_id = tim_rsu.tim_id";
			selectStatement += " inner join active_tim on active_tim.tim_id = tim.tim_id";
			selectStatement += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
			selectStatement += " where rsu_vw.ipv4_address = '" +  ipv4Address + "'";
			selectStatement += " and active_tim.clientId = '" +  clientId + "'";		
			selectStatement += " and type = 'CC'";		
			
			ResultSet rs = statement.executeQuery(selectStatement);

			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));
					activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
					activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
					activeTim.setTimType(rs.getString("TYPE"));
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

	public static List<ActiveTim> getActiveVSLTimsOnRsu(String ipv4Address, Double fromRm, Double toRm, String direction){
			
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		
		try {
			Statement statement = DbUtility.getConnection().createStatement();

			String selectStatement = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, SAT_RECORD_ID, MILEPOST_START, MILEPOST_STOP, TYPE from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
			selectStatement += " inner join tim_rsu on tim_rsu.rsu_id = rsu.rsu_id";
			selectStatement += " inner join tim on tim.tim_id = tim_rsu.tim_id";
			selectStatement += " inner join active_tim on active_tim.tim_id = tim.tim_id";
			selectStatement += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
			selectStatement += " where rsu_vw.ipv4_address = '" +  ipv4Address + "'";
			selectStatement += " and milepost_start = " +  fromRm;
			selectStatement += " and milepost_stop = " +  toRm;
			selectStatement += " and active_tim.direction = '" +  direction + "'";		
			selectStatement += " and type = 'VSL'";		
			
			ResultSet rs = statement.executeQuery(selectStatement);

			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					activeTim = new ActiveTim();		
					activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
					activeTim.setTimId(rs.getLong("TIM_ID"));	
					activeTim.setRecordId(rs.getString("SAT_RECORD_ID"));
					activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
					activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
					activeTim.setTimType(rs.getString("TYPE"));
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

	public static boolean deleteActiveTim(Long activeTimId){
		
		boolean deleteActiveTimResult = false;

		String deleteSQL = "DELETE FROM ACTIVE_TIM WHERE ACTIVE_TIM_ID = ?";

		try {			
		
			preparedStatement = DbUtility.getConnection().prepareStatement(deleteSQL);			
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

	public static boolean deleteExpiredActiveTims(){
		
		boolean deleteActiveTimResult = false;

		String deleteSQL = "DELETE ACTIVE_TIM where TIM_END < SYSTIMESTAMP";
	
		try {					
			preparedStatement = DbUtility.getConnection().prepareStatement(deleteSQL);			

			// execute delete SQL stetement
			deleteActiveTimResult = updateOrDelete(preparedStatement);

			System.out.println("deleteExpiredActiveTims ran");

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

