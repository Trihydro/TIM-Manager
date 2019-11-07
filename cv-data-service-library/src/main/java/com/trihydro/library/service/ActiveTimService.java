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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimUpdateModel;

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

	public static Boolean updateActiveTim_SatRecordId(Long activeTimId, String satRecordId) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		List<Pair<String, Object>> cols = new ArrayList<Pair<String, Object>>();
		cols.add(new ImmutablePair<String, Object>("SAT_RECORD_ID", satRecordId));
		boolean success = false;
		try {
			connection = DbUtility.getConnectionPool();
			preparedStatement = TimOracleTables.buildUpdateStatement(activeTimId, "ACTIVE_TIM", "ACTIVE_TIM_ID", cols,
					connection);

			// execute update statement
			success = updateOrDelete(preparedStatement);
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
		return success;
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

			String selectStatement = "select tim_rsu.rsu_index from active_tim";
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
			String query = "select * from active_tim where CLIENT_ID like '" + clientId + "%' and TIM_TYPE_ID = "
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

			String selectStatement = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, ACTIVE_TIM.DIRECTION, SAT_RECORD_ID, MILEPOST_START, MILEPOST_STOP, TYPE, CLIENT_ID, ROUTE from active_tim";
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
				activeTim.setRoute(rs.getString("ROUTE"));
				activeTim.setDirection(rs.getString("DIRECTION"));
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
			selectStatement += " where ipv4_address = '" + rsuIpAddress + "'";

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
			query += " where ipv4_address = '" + ipv4Address + "' and client_id = '" + clientId
					+ "' and active_tim.direction = '" + direction + "'";

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
			query += " where client_id = '" + clientId + "' and active_tim.direction = '" + direction
					+ "' and sat_record_id is not null";

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

	public static List<TimUpdateModel> getExpiringActiveTims() {
		TimUpdateModel activeTim = null;
		List<TimUpdateModel> activeTims = new ArrayList<TimUpdateModel>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "SELECT atim.*, tt.type as tim_type_name, tt.description as tim_type_description, t.msg_cnt, t.url_b, t.is_satellite, t.sat_record_id";
			selectStatement += ", df.data_frame_id, df.frame_type, df.duration_time, df.ssp_tim_rights, df.ssp_location_rights";
			selectStatement += ", df.ssp_msg_types, df.ssp_msg_content, df.content AS df_Content, df.url";
			selectStatement += ", r.region_id, r.name as region_name, r.anchor_lat, r.anchor_long, r.lane_width, r.path_id, r.closed_path";
			selectStatement += ", r.directionality, r.direction AS region_direction, r.path_id";
			selectStatement += " FROM active_tim atim";
			selectStatement += " INNER JOIN tim t ON atim.tim_id = t.tim_id";
			selectStatement += " LEFT JOIN data_frame df on atim.tim_id = df.tim_id";
			selectStatement += " LEFT JOIN region r on df.data_frame_id = r.data_frame_id";
			selectStatement += " LEFT JOIN tim_type tt ON atim.tim_type_id = tt.tim_type_id";
			selectStatement += " WHERE tim_start + INTERVAL '14' DAY <= SYSDATE + INTERVAL '1' DAY";
			selectStatement += " AND (tim_end is null OR tim_end >= SYSDATE + INTERVAL '1' DAY)";

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new TimUpdateModel();

				// Active_Tim properties
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setDirection(rs.getString("DIRECTION"));
				activeTim.setStartDateTime(rs.getString("TIM_START"));
				activeTim.setEndDateTime(rs.getString("TIM_END"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
				activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTim.setRoute(rs.getString("ROUTE"));

				activeTim.setStartDate_Timestamp(rs.getTimestamp("TIM_START"));
				activeTim.setEndDate_Timestamp(rs.getTimestamp("TIM_END"));

				// Tim properties
				activeTim.setMsgCnt(rs.getInt("MSG_CNT"));
				activeTim.setUrlB(rs.getString("URL_B"));

				// Tim Type properties
				activeTim.setTimTypeName(rs.getString("TIM_TYPE_NAME"));
				activeTim.setTimTypeDescription(rs.getString("TIM_TYPE_DESCRIPTION"));

				// Region Properties
				activeTim.setRegionId(rs.getInt("REGION_ID"));
				activeTim.setRegionName(rs.getString("REGION_NAME"));
				activeTim.setAnchorLat(rs.getBigDecimal("ANCHOR_LAT"));
				activeTim.setAnchorLong(rs.getBigDecimal("ANCHOR_LONG"));
				activeTim.setLaneWidth(rs.getBigDecimal("LANE_WIDTH"));
				// activeTim.setDirection(rs.getString("REGION_DIRECTION"));
				activeTim.setDirectionality(rs.getString("DIRECTIONALITY"));
				activeTim.setClosedPath(rs.getBoolean("CLOSED_PATH"));
				activeTim.setPathId(rs.getInt("PATH_ID"));

				// DataFrame properties
				activeTim.setDataFrameId(rs.getInt("DATA_FRAME_ID"));
				activeTim.setFrameType(rs.getInt("FRAME_TYPE"));
				activeTim.setDurationTime(rs.getInt("DURATION_TIME"));
				activeTim.setSspLocationRights(rs.getShort("SSP_LOCATION_RIGHTS"));
				activeTim.setSspTimRights(rs.getShort("SSP_TIM_RIGHTS"));
				activeTim.setSspMsgTypes(rs.getShort("SSP_MSG_TYPES"));
				activeTim.setSspMsgContent(rs.getShort("SSP_MSG_CONTENT"));
				activeTim.setDfContent(rs.getString("DF_CONTENT"));
				activeTim.setUrl(rs.getString("URL"));

				activeTims.add(activeTim);
			}
		} catch (Exception e) {
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