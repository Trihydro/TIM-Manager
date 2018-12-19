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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.model.ActiveTim;

public class ActiveTimService extends CvDataServiceLibrary {

	public static Long insertActiveTim(ActiveTim activeTim) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("active_tim",
					TimOracleTables.getActiveTimTable());

			// get connection
			connection = DbUtility.getConnectionPool();

			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "active_tim_id" });
			int fieldNum = 1;

			for (String col : TimOracleTables.getActiveTimTable()) {
				if (col.equals("TIM_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, activeTim.getTimId());
				else if (col.equals("MILEPOST_START"))
					SQLNullHandler.setDoubleOrNull(preparedStatement, fieldNum, activeTim.getMilepostStart());
				else if (col.equals("MILEPOST_STOP"))
					SQLNullHandler.setDoubleOrNull(preparedStatement, fieldNum, activeTim.getMilepostStop());
				else if (col.equals("DIRECTION"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getDirection());
				else if (col.equals("TIM_START"))
					SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(
							LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
				else if (col.equals("TIM_END"))
					if (activeTim.getEndDateTime() != null)
						SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(
								LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
					// SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum,
					// java.sql.Timestamp.valueOf(endDateTime));
					else
						preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
				else if (col.equals("TIM_TYPE_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, activeTim.getTimTypeId());
				else if (col.equals("ROUTE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getRoute());
				else if (col.equals("CLIENT_ID"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getClientId());
				else if (col.equals("SAT_RECORD_ID"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getSatRecordId());
				else if (col.equals("PK"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, activeTim.getPk());

				fieldNum++;
			}

			Long activeTimId = log(preparedStatement, "active tim");
			return activeTimId;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (preparedStatement != null)
					preparedStatement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return new Long(0);
	}

	// update all fields in TIM
	public static boolean updateActiveTim(ActiveTim activeTim) {

		boolean activeTimIdResult = false;
		String updateTableSQL = "UPDATE ACTIVE_TIM SET TIM_ID = ?, MILEPOST_START = ?, MILEPOST_STOP = ?, TIM_START = ?, TIM_END = ?, PK = ?"
				+ " WHERE ACTIVE_TIM_ID = ?";
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(updateTableSQL);
			SQLNullHandler.setLongOrNull(preparedStatement, 1, activeTim.getTimId());
			SQLNullHandler.setDoubleOrNull(preparedStatement, 2, activeTim.getMilepostStart());
			SQLNullHandler.setDoubleOrNull(preparedStatement, 3, activeTim.getMilepostStop());
			SQLNullHandler.setTimestampOrNull(preparedStatement, 4, java.sql.Timestamp
					.valueOf(LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));

			if (activeTim.getEndDateTime() == null)
				preparedStatement.setString(5, null);
			else
				SQLNullHandler.setTimestampOrNull(preparedStatement, 5, java.sql.Timestamp
						.valueOf(LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));

			SQLNullHandler.setIntegerOrNull(preparedStatement, 6, activeTim.getPk());
			SQLNullHandler.setLongOrNull(preparedStatement, 7, activeTim.getActiveTimId());
			activeTimIdResult = updateOrDelete(preparedStatement);

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (preparedStatement != null)
					preparedStatement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return activeTimIdResult;
	}

	public static void addItisCodesToActiveTim(ActiveTim activeTim) {

		List<Integer> itisCodes = new ArrayList<>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			rs = statement.executeQuery(
					"select * from active_tim inner join tim on tim.tim_id = active_tim.tim_id inner join data_frame on tim.tim_id = data_frame.tim_id inner join data_frame_itis_code on data_frame_itis_code.data_frame_id = data_frame.data_frame_id inner join itis_code on data_frame_itis_code.itis_code_id = itis_code.itis_code_id where active_tim_id = "
							+ activeTim.getActiveTimId());

			// convert to ActiveTim object
			while (rs.next()) {
				itisCodes.add(rs.getInt("ITIS_CODE"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		activeTim.setItisCodes(itisCodes);
	}

	public static boolean deleteExpiredActiveTims() {

		boolean deleteActiveTimResult = false;

		String deleteSQL = "DELETE FROM ACTIVE_TIM where TIM_END < SYS_EXTRACT_UTC(SYSTIMESTAMP)";
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {

			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(deleteSQL);

			// execute delete SQL stetement
			deleteActiveTimResult = updateOrDelete(preparedStatement);

			System.out.println("deleteExpiredActiveTims ran");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (preparedStatement != null)
					preparedStatement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return deleteActiveTimResult;
	}

	public static boolean deleteActiveTim(Long activeTimId) {

		boolean deleteActiveTimResult = false;

		String deleteSQL = "DELETE FROM ACTIVE_TIM WHERE ACTIVE_TIM_ID = ?";

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {

			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(deleteSQL);
			preparedStatement.setLong(1, activeTimId);

			// execute delete SQL stetement
			deleteActiveTimResult = updateOrDelete(preparedStatement);

			System.out.println("Active Tim is deleted!");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (preparedStatement != null)
					preparedStatement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return deleteActiveTimResult;
	}	

	// utility 
	public static List<Integer> getActiveTimIndicesByRsu(String rsuTarget) {

		List<Integer> indices = new ArrayList<Integer>();

		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			String selectStatement = "select rsu_index from active_tim";
			selectStatement += " inner join tim on active_tim.tim_id = tim.tim_id";
			selectStatement += " inner join tim_rsu on tim_rsu.tim_id = tim.tim_id";
			selectStatement += " inner join rsu on rsu.rsu_id = tim_rsu.rsu_id";
			selectStatement += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
			selectStatement += " where rsu_vw.ipv4_address = '" + rsuTarget + "'";

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				indices.add(rs.getInt("RSU_INDEX"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return indices;
	}

	// get Active TIMs by client ID direction
	public static List<ActiveTim> getActiveTimsByClientIdDirection(String clientId, Long timTypeId, String direction) {

		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			String query = "select * from active_tim where CLIENT_ID = '" + clientId + "' and TIM_TYPE_ID = "
					+ timTypeId;

			if (direction != null) {
				query += " and DIRECTION = '" + direction + "'";
			}

			rs = statement.executeQuery(query);

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new ActiveTim();
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTim.setDirection(rs.getString("DIRECTION"));
				activeTim.setEndDateTime(rs.getString("TIM_END"));
				activeTim.setStartDateTime(rs.getString("TIM_START"));
				activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
				activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
				activeTim.setRoute(rs.getString("ROUTE"));
				activeTim.setPk(rs.getInt("PK"));
				activeTims.add(activeTim);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return activeTims;
	}

	public static List<ActiveTim> getExpiredActiveTims() {

		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, SAT_RECORD_ID, MILEPOST_START, MILEPOST_STOP, TYPE, CLIENT_ID from active_tim";
			selectStatement += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
			selectStatement += "  WHERE TIM_END < SYS_EXTRACT_UTC(SYSTIMESTAMP)";

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new ActiveTim();
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
				activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
				activeTim.setTimType(rs.getString("TYPE"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTims.add(activeTim);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return activeTims;
	}

	// for GETs
	public static List<ActiveTim> getActivesTimByType(Long timTypeId) {

		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from active_tim where TIM_TYPE_ID = " + timTypeId);

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new ActiveTim();
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTim.setDirection(rs.getString("DIRECTION"));
				activeTim.setEndDateTime(rs.getString("TIM_END"));
				activeTim.setStartDateTime(rs.getString("TIM_START"));
				activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
				activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
				activeTim.setRoute(rs.getString("ROUTE"));
				activeTim.setPk(rs.getInt("PK"));
				activeTim.setTimTypeId(rs.getLong("TIM_TYPE_ID"));
				activeTims.add(activeTim);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return activeTims;
	}

	// for GET
	public static List<ActiveTim> getAllActiveTims() {

		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select * from active_tim";

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new ActiveTim();
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
				activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTims.add(activeTim);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return activeTims;
	}

	public static List<Integer> getIndiciesInUseForRsu(String rsuIpAddress) {
	
		List<Integer> indicies = new ArrayList<Integer>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select rsu_index from active_tim";
			selectStatement += " inner join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
			selectStatement += " inner join rsu on tim_rsu.rsu_id = rsu.rsu_id";
			selectStatement += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
			selectStatement += " where ipv4_address = '" + rsuIpAddress +"'";

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {			
				indicies.add(rs.getInt("RSU_INDEX"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return indicies;
	}



	// get Active TIMs by client ID direction
	public static ActiveTim getActiveRsuTim(String clientId, String direction, String ipv4Address) {

		ActiveTim activeTim = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			String query = "select * from active_tim";
			query += " inner join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
			query += " inner join rsu on tim_rsu.rsu_id = rsu.rsu_id";
			query += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
			query += " where ipv4_address = '" + ipv4Address + "' and client_id = '" + clientId + "' and active_tim.direction = '" + direction +"'";

			rs = statement.executeQuery(query);

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new ActiveTim();
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTim.setDirection(rs.getString("DIRECTION"));
				activeTim.setEndDateTime(rs.getString("TIM_END"));
				activeTim.setStartDateTime(rs.getString("TIM_START"));
				activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
				activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
				activeTim.setRoute(rs.getString("ROUTE"));
				activeTim.setPk(rs.getInt("PK"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return activeTim;
	}

	// get Active TIMs by client ID direction
	public static ActiveTim getActiveSatTim(String clientId, String direction) {

		ActiveTim activeTim = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			String query = "select * from active_tim";
			query += " where client_id = '" + clientId + "' and active_tim.direction = '" + direction +"' and sat_record_id is not null";

			rs = statement.executeQuery(query);

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new ActiveTim();
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTim.setDirection(rs.getString("DIRECTION"));
				activeTim.setEndDateTime(rs.getString("TIM_END"));
				activeTim.setStartDateTime(rs.getString("TIM_START"));
				activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
				activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
				activeTim.setRoute(rs.getString("ROUTE"));
				activeTim.setPk(rs.getInt("PK"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return activeTim;
	}


}