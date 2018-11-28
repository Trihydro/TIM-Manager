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

	public static boolean updateActiveTimTimId(Long activeTimId, Long timId) {

		boolean activeTimIdResult = false;
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		String updateTableSQL = "UPDATE ACTIVE_TIM SET TIM_ID = ? " + " WHERE ACTIVE_TIM_ID = ?";

		try {
			// get connection
			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(updateTableSQL);
			SQLNullHandler.setLongOrNull(preparedStatement, 1, timId);
			SQLNullHandler.setLongOrNull(preparedStatement, 2, activeTimId);

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

	public static boolean updateActiveTimEndDate(Long activeTimId, String endDateTime) {

		boolean activeTimIdResult = false;
		String updateTableSQL = "UPDATE ACTIVE_TIM SET TIM_END = ? " + " WHERE ACTIVE_TIM_ID = ?";
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(updateTableSQL);
			SQLNullHandler.setTimestampOrNull(preparedStatement, 1,
					java.sql.Timestamp.valueOf(LocalDateTime.parse(endDateTime, DateTimeFormatter.ISO_DATE_TIME)));
			SQLNullHandler.setLongOrNull(preparedStatement, 2, activeTimId);
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

	// get active sat tims by client id
	public static List<ActiveTim> getActiveSatTimsByClientIdDirection(String clientId, Long timTypeId,
			String direction) {
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			rs = statement
					.executeQuery("select * from active_tim where CLIENT_ID = '" + clientId + "' and TIM_TYPE_ID = "
							+ timTypeId + " and DIRECTION = '" + direction + "' and SAT_RECORD_ID is not null");

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new ActiveTim();
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
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

	// get active rsu tims by client id
	public static List<ActiveTim> getActiveTimsOnRsuByClientId(String ipv4Address, String clientId, Long timTypeId,
			String direction) {

		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			String selectStatement = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, SAT_RECORD_ID, MILEPOST_START, MILEPOST_STOP, TYPE from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
			selectStatement += " inner join tim_rsu on tim_rsu.rsu_id = rsu.rsu_id";
			selectStatement += " inner join tim on tim.tim_id = tim_rsu.tim_id";
			selectStatement += " inner join active_tim on active_tim.tim_id = tim.tim_id";
			selectStatement += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
			selectStatement += " where rsu_vw.ipv4_address = '" + ipv4Address + "'";
			selectStatement += " and active_tim.client_Id = '" + clientId + "'";
			selectStatement += " and active_tim.direction = '" + direction + "'";
			selectStatement += " and type = '" + timTypeId + "'";

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

	public static List<ActiveTim> getActiveTimsByClientId(String clientId, Long timTypeId, String direction) {

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

	public static List<ActiveTim> getRsusWithActiveTim(String clientId, String direction, String timType) {

		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			String selectStatement = "select ipv4_address, active_tim_id, active_tim.tim_id, active_tim.direction from active_tim";
			selectStatement += " inner join tim on active_tim.tim_id = tim.tim_id";
			selectStatement += " inner join tim_rsu on tim_rsu.tim_id = tim.tim_id";
			selectStatement += " inner join rsu on rsu.rsu_id = tim_rsu.rsu_id";
			selectStatement += " inner join rsu_vw on rsu_vw.deviceid = rsu.deviceid";
			selectStatement += " inner join tim_type on active_tim.tim_type_id = tim_type.tim_type_id";
			selectStatement += " where active_tim.direction = '" + direction + "' and client_id = '" + clientId
					+ "' and type = '" + timType + "'";

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new ActiveTim();
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setDirection(rs.getString("DIRECTION"));
				activeTim.setRsuTarget(rs.getString("IPV4_ADDRESS"));
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

	public static List<Integer> getTakenRsuIndexs(String ipv4Address) {

		List<Integer> indexes = new ArrayList<Integer>();

		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		String selectStatement = "select rsu_index from active_tim";
		selectStatement += " inner join tim on tim.tim_id = active_tim.TIM_ID";
		selectStatement += " inner join tim_rsu on tim.tim_id = tim_rsu.tim_id";
		selectStatement += " inner join rsu on rsu.rsu_id = tim_rsu.rsu_id";
		selectStatement += " inner join rsu_vw on rsu_vw.DEVICEID = rsu.DEVICEID";
		selectStatement += " where ipv4_address = '" + ipv4Address + "'";

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			rs = statement.executeQuery(selectStatement);
			// convert to ActiveTim object
			while (rs.next()) {
				indexes.add(rs.getInt("RSU_INDEX"));
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
		return indexes;
	}

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

}

// maybe revise
// public static List<ActiveTim> getAllActiveTimsBySegment(Double milepostStart,
// Double milepostStop, Long timTypeId,
// String direction) {

// ActiveTim activeTim = null;
// List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
// Connection connection = null;
// Statement statement = null;
// ResultSet rs = null;

// try {
// connection = DbUtility.getConnectionPool();
// statement = connection.createStatement();
// rs = statement.executeQuery("select * from active_tim where MILEPOST_START =
// " + milepostStart
// + " and MILEPOST_STOP = " + milepostStop + " and TIM_TYPE_ID = " + timTypeId
// + " and DIRECTION = '"
// + direction + "'");
// try {
// // convert to ActiveTim object
// while (rs.next()) {
// activeTim = new ActiveTim();
// activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
// activeTim.setTimId(rs.getLong("TIM_ID"));
// activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
// activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
// activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
// activeTim.setRoute(rs.getString("ROUTE"));
// activeTim.setDirection(rs.getString("DIRECTION"));
// activeTims.add(activeTim);
// }
// } finally {
// try {
// rs.close();
// } catch (Exception e) {
// e.printStackTrace();
// }
// }
// } catch (SQLException e) {
// e.printStackTrace();
// } finally {
// try {
// // close prepared statement
// if (statement != null)
// statement.close();
// // return connection back to pool
// if (connection != null)
// connection.close();
// // close result set
// if (rs != null)
// rs.close();
// } catch (SQLException e) {
// e.printStackTrace();
// }
// }

// return activeTims;
// }

// new call?
// public static List<ActiveTim> getActiveRsuTims(Double milepostStart, Double
// milepostStop, Long timTypeId,
// String direction) {

// ActiveTim activeTim = null;
// List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
// Connection connection = null;
// Statement statement = null;
// ResultSet rs = null;

// try {
// connection = DbUtility.getConnectionPool();
// statement = connection.createStatement();
// rs = statement.executeQuery("select * from active_tim where MILEPOST_START =
// " + milepostStart
// + " and MILEPOST_STOP = " + milepostStop + " and TIM_TYPE_ID = " + timTypeId
// + " and DIRECTION = '"
// + direction + "' and SAT_RECORD_ID is null");

// // convert to ActiveTim object
// while (rs.next()) {
// activeTim = new ActiveTim();
// activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
// activeTim.setTimId(rs.getLong("TIM_ID"));
// activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
// activeTims.add(activeTim);
// }
// } catch (SQLException e) {
// e.printStackTrace();
// } finally {
// try {
// // close prepared statement
// if (statement != null)
// statement.close();
// // return connection back to pool
// if (connection != null)
// connection.close();
// // close result set
// if (rs != null)
// rs.close();
// } catch (SQLException e) {
// e.printStackTrace();
// }
// }

// return activeTims;
// }

// get active sat tims by segment
// public static List<ActiveTim> getActiveSatTimsBySegmentDirection(Double
// milepostStart, Double milepostStop,
// Long timTypeId, String direction) {

// ActiveTim activeTim = null;
// List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
// Connection connection = null;
// Statement statement = null;
// ResultSet rs = null;

// try {
// connection = DbUtility.getConnectionPool();
// statement = connection.createStatement();
// rs = statement.executeQuery("select * from active_tim where MILEPOST_START =
// " + milepostStart
// + " and MILEPOST_STOP = " + milepostStop + " and TIM_TYPE_ID = " + timTypeId
// + " and DIRECTION = '"
// + direction + "' and SAT_RECORD_ID is not null");

// // convert to ActiveTim object
// while (rs.next()) {
// activeTim = new ActiveTim();
// activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
// activeTim.setTimId(rs.getLong("TIM_ID"));
// activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
// activeTims.add(activeTim);
// }
// } catch (SQLException e) {
// e.printStackTrace();
// } finally {
// try {
// // close prepared statement
// if (statement != null)
// statement.close();
// // return connection back to pool
// if (connection != null)
// connection.close();
// // close result set
// if (rs != null)
// rs.close();
// } catch (SQLException e) {
// e.printStackTrace();
// }
// }
// return activeTims;
// }

// public static List<ActiveTim> getActiveRsuTimsByClientIdDirection(String
// clientId, Long timTypeId,
// String direction) {
// ActiveTim activeTim = null;
// List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
// Connection connection = null;
// Statement statement = null;
// ResultSet rs = null;

// try {
// connection = DbUtility.getConnectionPool();
// statement = connection.createStatement();
// rs = statement
// .executeQuery("select * from active_tim where (CLIENT_ID = '" + clientId + "'
// and TIM_TYPE_ID = "
// + timTypeId + " and SAT_RECORD_ID is null" + "' and direction = " +
// direction);

// // convert to ActiveTim object
// while (rs.next()) {
// activeTim = new ActiveTim();
// activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
// activeTim.setTimId(rs.getLong("TIM_ID"));
// activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
// activeTim.setClientId(rs.getString("CLIENT_ID"));
// activeTim.setDirection(rs.getString("DIRECTION"));
// activeTim.setEndDateTime(rs.getString("TIM_END"));
// activeTim.setStartDateTime(rs.getString("TIM_START"));
// activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
// activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
// activeTim.setRoute(rs.getString("ROUTE"));
// activeTim.setPk(rs.getInt("PK"));
// activeTims.add(activeTim);
// }
// } catch (SQLException e) {
// e.printStackTrace();
// } finally {
// try {
// // close prepared statement
// if (statement != null)
// statement.close();
// // return connection back to pool
// if (connection != null)
// connection.close();
// // close result set
// if (rs != null)
// rs.close();
// } catch (SQLException e) {
// e.printStackTrace();
// }
// }

// return activeTims;
// }

// get active rsu tims by segment
// public static List<ActiveTim> getActiveTimsOnRsuByRoadSegment(String
// ipv4Address, Long timTypeId, Double fromRm,
// Double toRm, String direction) {

// ActiveTim activeTim = null;
// List<ActiveTim> activeTims = new ArrayList<ActiveTim>();

// Connection connection = null;
// Statement statement = null;
// ResultSet rs = null;

// try {

// connection = DbUtility.getConnectionPool();
// statement = connection.createStatement();

// String selectStatement = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID,
// SAT_RECORD_ID, MILEPOST_START, MILEPOST_STOP, TYPE from rsu inner join rsu_vw
// on rsu.deviceid = rsu_vw.deviceid";
// selectStatement += " inner join tim_rsu on tim_rsu.rsu_id = rsu.rsu_id";
// selectStatement += " inner join tim on tim.tim_id = tim_rsu.tim_id";
// selectStatement += " inner join active_tim on active_tim.tim_id =
// tim.tim_id";
// selectStatement += " inner join tim_type on tim_type.tim_type_id =
// active_tim.tim_type_id";
// selectStatement += " where rsu_vw.ipv4_address = '" + ipv4Address + "'";
// selectStatement += " and milepost_start = " + fromRm;
// selectStatement += " and milepost_stop = " + toRm;
// selectStatement += " and active_tim.direction = '" + direction + "'";
// selectStatement += " and active_tim.tim_type_id = " + timTypeId;

// rs = statement.executeQuery(selectStatement);

// // convert to ActiveTim object
// while (rs.next()) {
// activeTim = new ActiveTim();
// activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
// activeTim.setTimId(rs.getLong("TIM_ID"));
// activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
// activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
// activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
// activeTim.setTimType(rs.getString("TYPE"));
// activeTims.add(activeTim);
// }
// } catch (SQLException e) {
// e.printStackTrace();
// } finally {
// try {
// // close prepared statement
// if (statement != null)
// statement.close();
// // return connection back to pool
// if (connection != null)
// connection.close();
// // close result set
// if (rs != null)
// rs.close();
// } catch (SQLException e) {
// e.printStackTrace();
// }
// }

// return activeTims;
// }