package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandlerStatic;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.tables.TimOracleTablesStatic;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ActiveTimService extends CvDataServiceLibrary {

	public static Long insertActiveTim(ActiveTim activeTim) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			String insertQueryStatement = TimOracleTablesStatic.buildInsertQueryStatement("active_tim",
					TimOracleTablesStatic.getActiveTimTable());

			// get connection
			connection = DbUtility.getConnectionPool();

			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "active_tim_id" });
			int fieldNum = 1;

			for (String col : TimOracleTablesStatic.getActiveTimTable()) {
				if (col.equals("TIM_ID"))
					SQLNullHandlerStatic.setLongOrNull(preparedStatement, fieldNum, activeTim.getTimId());
				else if (col.equals("MILEPOST_START"))
					SQLNullHandlerStatic.setDoubleOrNull(preparedStatement, fieldNum, activeTim.getMilepostStart());
				else if (col.equals("MILEPOST_STOP"))
					SQLNullHandlerStatic.setDoubleOrNull(preparedStatement, fieldNum, activeTim.getMilepostStop());
				else if (col.equals("DIRECTION"))
					SQLNullHandlerStatic.setStringOrNull(preparedStatement, fieldNum, activeTim.getDirection());
				else if (col.equals("TIM_START"))
					SQLNullHandlerStatic.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(
							LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
				else if (col.equals("TIM_END"))
					if (activeTim.getEndDateTime() != null)
						SQLNullHandlerStatic.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(
								LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
					else
						preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
				else if (col.equals("TIM_TYPE_ID"))
					SQLNullHandlerStatic.setLongOrNull(preparedStatement, fieldNum, activeTim.getTimTypeId());
				else if (col.equals("ROUTE"))
					SQLNullHandlerStatic.setStringOrNull(preparedStatement, fieldNum, activeTim.getRoute());
				else if (col.equals("CLIENT_ID"))
					SQLNullHandlerStatic.setStringOrNull(preparedStatement, fieldNum, activeTim.getClientId());
				else if (col.equals("SAT_RECORD_ID"))
					SQLNullHandlerStatic.setStringOrNull(preparedStatement, fieldNum, activeTim.getSatRecordId());
				else if (col.equals("PK"))
					SQLNullHandlerStatic.setIntegerOrNull(preparedStatement, fieldNum, activeTim.getPk());

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
		String url = String.format("%s/update-sat-record-id/%d/%s", CVRestUrl, activeTimId, satRecordId);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(null, headers);
		ResponseEntity<Boolean> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.PUT, entity,
				Boolean.class);
		return response.getBody();
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
			SQLNullHandlerStatic.setLongOrNull(preparedStatement, 1, activeTim.getTimId());
			SQLNullHandlerStatic.setDoubleOrNull(preparedStatement, 2, activeTim.getMilepostStart());
			SQLNullHandlerStatic.setDoubleOrNull(preparedStatement, 3, activeTim.getMilepostStop());
			SQLNullHandlerStatic.setTimestampOrNull(preparedStatement, 4, java.sql.Timestamp
					.valueOf(LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));

			if (activeTim.getEndDateTime() == null)
				preparedStatement.setString(5, null);
			else
				SQLNullHandlerStatic.setTimestampOrNull(preparedStatement, 5, java.sql.Timestamp
						.valueOf(LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));

			SQLNullHandlerStatic.setIntegerOrNull(preparedStatement, 6, activeTim.getPk());
			SQLNullHandlerStatic.setLongOrNull(preparedStatement, 7, activeTim.getActiveTimId());
			activeTimIdResult = updateOrDelete(preparedStatement);
			System.out.println("------ Updated active_tim with id: " + activeTim.getActiveTimId() + " --------------");
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

			// execute delete SQL statement
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

			System.out.println("Active Tim (active_tim_id " + activeTimId + ") is deleted!");

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

	public static boolean deleteActiveTimsById(List<Long> activeTimIds) {
		boolean deleteActiveTimResult = false;

		String deleteSQL = "DELETE FROM ACTIVE_TIM WHERE ACTIVE_TIM_ID in (";
		for (int i = 0; i < activeTimIds.size(); i++) {
			deleteSQL += "?,";
		}
		deleteSQL = deleteSQL.substring(0, deleteSQL.length() - 1);
		deleteSQL += ")";

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {

			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(deleteSQL);
			for (int i = 0; i < activeTimIds.size(); i++) {
				preparedStatement.setLong(i + 1, activeTimIds.get(i));
			}

			// execute delete SQL stetement
			deleteActiveTimResult = updateOrDelete(preparedStatement);

			System.out.println("Active Tims (active_tim_ids "
					+ activeTimIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") deleted!");

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

	public static List<ActiveTim> getActiveTimsByWydotTim(List<? extends WydotTim> wydotTims, Long timTypeId) {
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		WydotTim wydotTim = null;

		try {
			connection = DbUtility.getConnectionPool();
			String query = "select * from active_tim where ";
			if (timTypeId != null) {
				query += "TIM_TYPE_ID = ? and (";
			}

			for (int i = 0; i < wydotTims.size(); i++) {
				if (i > 0) {
					query += " OR ";
				}
				query += "(CLIENT_ID like ?";
				wydotTim = wydotTims.get(i);
				if (wydotTim.getDirection() != null) {
					query += " and DIRECTION = ?";
				}
				query += ")";
			}
			if (timTypeId != null) {
				query += ")";
			}
			ps = connection.prepareStatement(query);

			int index = 1;
			if (timTypeId != null) {
				ps.setLong(index, timTypeId);
				index++;
			}
			for (int i = 0; i < wydotTims.size(); i++) {
				wydotTim = wydotTims.get(i);

				// set client id
				ps.setString(index, wydotTim.getClientId() + "%");
				index++;

				// set direction
				if (wydotTim.getDirection() != null) {
					ps.setString(index, wydotTim.getDirection());
					index++;
				}
			}
			rs = ps.executeQuery();

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
				// close prepared statement and result set (rs closed by prepared statement)
				if (ps != null) {
					ps.close();
				}
				// return connection back to pool
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return activeTims;
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
		ResponseEntity<TimUpdateModel[]> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(CVRestUrl + "/active-tim/expired", TimUpdateModel[].class);
		return Arrays.asList(response.getBody());
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
	public static ActiveTim getActiveSatTim(String satRecordId, String direction) {

		ActiveTim activeTim = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			String query = "select * from active_tim";
			query += " where sat_record_id = '" + satRecordId + "' and active_tim.direction = '" + direction + "'";

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

	public static HttpHeaders GetDefaultHttpHeaders() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "application/json");
		return responseHeaders;
	}

	/**
	 * Calls out to the cv-data-controller REST function to fetch expiring TIMs
	 * 
	 * @return List of TimUpdateModel representing all TIMs expiring within 1 day
	 */
	public static List<TimUpdateModel> getExpiringActiveTims() {
		ResponseEntity<TimUpdateModel[]> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(CVRestUrl + "/active-tim/expiring", TimUpdateModel[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<ActiveTim> getActiveTimsMissingItisCodes() {
		ResponseEntity<TimUpdateModel[]> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(CVRestUrl + "/active-tim/missing-itis", TimUpdateModel[].class);
		return Arrays.asList(response.getBody());
	}

	public static List<ActiveTim> getActiveTimsNotSent() {
		ResponseEntity<TimUpdateModel[]> response = RestTemplateProvider.GetRestTemplate()
				.getForEntity(CVRestUrl + "/active-tim/not-sent", TimUpdateModel[].class);
		return Arrays.asList(response.getBody());
	}
}