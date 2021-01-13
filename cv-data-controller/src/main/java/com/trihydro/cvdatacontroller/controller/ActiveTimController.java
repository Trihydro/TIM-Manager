package com.trihydro.cvdatacontroller.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveRsuTimQueryModel;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.tables.TimOracleTables;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("active-tim")
@ApiIgnore
public class ActiveTimController extends BaseController {

	private TimOracleTables timOracleTables;
	private SQLNullHandler sqlNullHandler;
	protected Calendar UTCCalendar;

	public ActiveTimController() {
		UTCCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	}

	@Autowired
	public void InjectDependencies(TimOracleTables _timOracleTables, SQLNullHandler _sqlNullHandler) {
		timOracleTables = _timOracleTables;
		sqlNullHandler = _sqlNullHandler;
	}

	@RequestMapping(value = "/expiring", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<List<TimUpdateModel>> GetExpiringActiveTims() {
		TimUpdateModel activeTim = null;
		List<TimUpdateModel> activeTims = new ArrayList<TimUpdateModel>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();

			String selectStatement = "SELECT atim.*, tt.type as tim_type_name, tt.description as tim_type_description";
			selectStatement += ", t.msg_cnt, t.url_b, t.is_satellite, t.sat_record_id, t.packet_id";
			selectStatement += ", df.data_frame_id, df.frame_type, df.duration_time, df.ssp_tim_rights, df.ssp_location_rights";
			selectStatement += ", df.ssp_msg_types, df.ssp_msg_content, df.content AS df_Content, df.url";
			selectStatement += ", r.region_id, r.anchor_lat, r.anchor_long, r.lane_width";
			selectStatement += ", r.path_id, r.closed_path, r.description AS region_description";
			selectStatement += ", r.directionality, r.direction AS region_direction";
			selectStatement += " FROM active_tim atim";
			selectStatement += " INNER JOIN tim t ON atim.tim_id = t.tim_id";
			selectStatement += " LEFT JOIN data_frame df on atim.tim_id = df.tim_id";
			selectStatement += " LEFT JOIN region r on df.data_frame_id = r.data_frame_id";
			selectStatement += " LEFT JOIN tim_type tt ON atim.tim_type_id = tt.tim_type_id";
			// where starting less than 2 hours away
			selectStatement += " WHERE atim.tim_start <= SYSDATE + INTERVAL '2' HOUR";
			// and expiration_date within 2hrs
			selectStatement += " AND (atim.expiration_date is null OR atim.expiration_date <= SYSDATE + INTERVAL '2' HOUR)";
			// check that end time isn't within 2hrs
			selectStatement += " AND (atim.tim_end is null OR atim.tim_end >= SYSDATE + INTERVAL '2' HOUR)";
			// check that this TIM is capable of being refreshed (direction = I or D)
			selectStatement += " AND atim.direction IN ('I', 'D')";

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
				activeTim.setExpirationDateTime(rs.getString("EXPIRATION_DATE"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTim.setRoute(rs.getString("ROUTE"));

				Coordinate startPoint = null;
				Coordinate endPoint = null;
				BigDecimal startLat = rs.getBigDecimal("START_LATITUDE");
				BigDecimal startLon = rs.getBigDecimal("START_LONGITUDE");
				if (!rs.wasNull()) {
					startPoint = new Coordinate(startLat, startLon);
				}
				activeTim.setStartPoint(startPoint);

				BigDecimal endLat = rs.getBigDecimal("END_LATITUDE");
				BigDecimal endLon = rs.getBigDecimal("END_LONGITUDE");
				if (!rs.wasNull()) {
					endPoint = new Coordinate(endLat, endLon);
				}
				activeTim.setEndPoint(endPoint);

				activeTim.setStartDate_Timestamp(rs.getTimestamp("TIM_START", UTCCalendar));
				activeTim.setEndDate_Timestamp(rs.getTimestamp("TIM_END", UTCCalendar));

				// Tim properties
				activeTim.setMsgCnt(rs.getInt("MSG_CNT"));
				activeTim.setUrlB(rs.getString("URL_B"));
				activeTim.setPacketId(rs.getString("PACKET_ID"));

				// Tim Type properties
				activeTim.setTimTypeName(rs.getString("TIM_TYPE_NAME"));
				activeTim.setTimTypeDescription(rs.getString("TIM_TYPE_DESCRIPTION"));

				// Region Properties
				activeTim.setRegionId(rs.getInt("REGION_ID"));
				activeTim.setAnchorLat(rs.getBigDecimal("ANCHOR_LAT"));
				activeTim.setAnchorLong(rs.getBigDecimal("ANCHOR_LONG"));

				activeTim.setLaneWidth(rs.getBigDecimal("LANE_WIDTH"));
				activeTim.setRegionDirection(rs.getString("REGION_DIRECTION"));
				activeTim.setDirectionality(rs.getString("DIRECTIONALITY"));
				activeTim.setClosedPath(rs.getBoolean("CLOSED_PATH"));
				activeTim.setPathId(rs.getInt("PATH_ID"));
				activeTim.setRegionDescription(rs.getString("REGION_DESCRIPTION"));

				// DataFrame properties
				activeTim.setDataFrameId(rs.getInt("DATA_FRAME_ID"));
				activeTim.setFrameType(rs.getInt("FRAME_TYPE"));
				activeTim.setDurationTime(rs.getInt("DURATION_TIME"));
				activeTim.setSspLocationRights(utility.GetShortValueFromResultSet(rs, "SSP_LOCATION_RIGHTS"));
				activeTim.setSspTimRights(utility.GetShortValueFromResultSet(rs, "SSP_TIM_RIGHTS"));
				activeTim.setSspMsgTypes(utility.GetShortValueFromResultSet(rs, "SSP_MSG_TYPES"));
				activeTim.setSspMsgContent(utility.GetShortValueFromResultSet(rs, "SSP_MSG_CONTENT"));

				// set dataFrame content. it's required for the ODE, so if we didn't record it,
				// assume Advisory
				String dfContent = rs.getString("DF_CONTENT");
				if (dfContent == null || dfContent.isEmpty()) {
					dfContent = "advisory";
				}
				activeTim.setDfContent(dfContent);
				activeTim.setUrl(rs.getString("URL"));

				activeTims.add(activeTim);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTims);
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

		return ResponseEntity.ok(activeTims);
	}

	@RequestMapping(value = "/update-model/{activeTimId}", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<TimUpdateModel> GetUpdateModelFromActiveTimId(@PathVariable Long activeTimId) {
		TimUpdateModel activeTim = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();

			String selectStatement = "SELECT atim.*, tt.type AS tim_type_name, tt.description AS tim_type_description";
			selectStatement += ", t.msg_cnt, t.url_b, t.is_satellite, t.sat_record_id, t.packet_id";
			selectStatement += ", df.data_frame_id, df.frame_type, df.duration_time, df.ssp_tim_rights, df.ssp_location_rights";
			selectStatement += ", df.ssp_msg_types, df.ssp_msg_content, df.content AS df_Content, df.url";
			selectStatement += ", r.region_id, r.anchor_lat, r.anchor_long, r.lane_width";
			selectStatement += ", r.path_id, r.closed_path, r.description AS region_description";
			selectStatement += ", r.directionality, r.direction AS region_direction";
			selectStatement += " FROM active_tim atim";
			selectStatement += " INNER JOIN tim t ON atim.tim_id = t.tim_id";
			selectStatement += " LEFT JOIN data_frame df on atim.tim_id = df.tim_id";
			selectStatement += " LEFT JOIN region r on df.data_frame_id = r.data_frame_id";
			selectStatement += " LEFT JOIN tim_type tt ON atim.tim_type_id = tt.tim_type_id";
			// where active_tim_id is provided
			selectStatement += " WHERE atim.active_tim_id = " + activeTimId;
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
				activeTim.setExpirationDateTime(rs.getString("EXPIRATION_DATE"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTim.setRoute(rs.getString("ROUTE"));

				Coordinate startPoint = null;
				Coordinate endPoint = null;
				BigDecimal startLat = rs.getBigDecimal("START_LATITUDE");
				BigDecimal startLon = rs.getBigDecimal("START_LONGITUDE");
				if (!rs.wasNull()) {
					startPoint = new Coordinate(startLat, startLon);
				}
				activeTim.setStartPoint(startPoint);

				BigDecimal endLat = rs.getBigDecimal("END_LATITUDE");
				BigDecimal endLon = rs.getBigDecimal("END_LONGITUDE");
				if (!rs.wasNull()) {
					endPoint = new Coordinate(endLat, endLon);
				}
				activeTim.setEndPoint(endPoint);

				activeTim.setStartDate_Timestamp(rs.getTimestamp("TIM_START", UTCCalendar));
				activeTim.setEndDate_Timestamp(rs.getTimestamp("TIM_END", UTCCalendar));

				// Tim properties
				activeTim.setMsgCnt(rs.getInt("MSG_CNT"));
				activeTim.setUrlB(rs.getString("URL_B"));
				activeTim.setPacketId(rs.getString("PACKET_ID"));

				// Tim Type properties
				activeTim.setTimTypeName(rs.getString("TIM_TYPE_NAME"));
				activeTim.setTimTypeDescription(rs.getString("TIM_TYPE_DESCRIPTION"));

				// Region Properties
				activeTim.setRegionId(rs.getInt("REGION_ID"));
				activeTim.setAnchorLat(rs.getBigDecimal("ANCHOR_LAT"));
				activeTim.setAnchorLong(rs.getBigDecimal("ANCHOR_LONG"));

				activeTim.setLaneWidth(rs.getBigDecimal("LANE_WIDTH"));
				activeTim.setRegionDirection(rs.getString("REGION_DIRECTION"));
				activeTim.setDirectionality(rs.getString("DIRECTIONALITY"));
				activeTim.setClosedPath(rs.getBoolean("CLOSED_PATH"));
				activeTim.setPathId(rs.getInt("PATH_ID"));
				activeTim.setRegionDescription(rs.getString("REGION_DESCRIPTION"));

				// DataFrame properties
				activeTim.setDataFrameId(rs.getInt("DATA_FRAME_ID"));
				activeTim.setFrameType(rs.getInt("FRAME_TYPE"));
				activeTim.setDurationTime(rs.getInt("DURATION_TIME"));
				activeTim.setSspLocationRights(utility.GetShortValueFromResultSet(rs, "SSP_LOCATION_RIGHTS"));
				activeTim.setSspTimRights(utility.GetShortValueFromResultSet(rs, "SSP_TIM_RIGHTS"));
				activeTim.setSspMsgTypes(utility.GetShortValueFromResultSet(rs, "SSP_MSG_TYPES"));
				activeTim.setSspMsgContent(utility.GetShortValueFromResultSet(rs, "SSP_MSG_CONTENT"));

				// set dataFrame content. it's required for the ODE, so if we didn't record it,
				// assume Advisory
				String dfContent = rs.getString("DF_CONTENT");
				if (dfContent == null || dfContent.isEmpty()) {
					dfContent = "advisory";
				}
				activeTim.setDfContent(dfContent);
				activeTim.setUrl(rs.getString("URL"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTim);
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

		return ResponseEntity.ok(activeTim);
	}

	@RequestMapping(value = "/update-sat-record-id/{activeTimId}/{satRecordId}", method = RequestMethod.PUT)
	public ResponseEntity<Boolean> updateActiveTim_SatRecordId(@PathVariable Long activeTimId,
			@PathVariable String satRecordId) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		List<Pair<String, Object>> cols = new ArrayList<Pair<String, Object>>();
		cols.add(new ImmutablePair<String, Object>("SAT_RECORD_ID", satRecordId));
		boolean success = false;

		try {
			connection = dbInteractions.getConnectionPool();
			preparedStatement = timOracleTables.buildUpdateStatement(activeTimId, "ACTIVE_TIM", "ACTIVE_TIM_ID", cols,
					connection);

			// execute update statement
			success = dbInteractions.updateOrDelete(preparedStatement);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
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
		return ResponseEntity.ok(success);
	}

	@RequestMapping(value = "/missing-itis", method = RequestMethod.GET)
	public ResponseEntity<List<ActiveTim>> GetActiveTimsMissingItisCodes() {
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();

			statement = connection.createStatement();

			// The inner subqueries leave us with a list of tim_ids that aren't associated
			// with any valid itis codes. Select the active_tims with
			// those tim_ids
			String selectStatement = " select * from active_tim where active_tim.tim_id in";

			// Outer subquery: Get all records that have a tim_id found to be associated
			// with a null itis code (from inner subquery)
			// We need to do this because there could me multiple records for a single
			// tim_id
			selectStatement += " (select active_tim.tim_id from active_tim";
			selectStatement += " left join data_frame on active_tim.tim_id = data_frame.tim_id";
			selectStatement += " left join data_frame_itis_code on data_frame.data_frame_id = data_frame_itis_code.data_frame_id";
			selectStatement += " where active_tim.tim_id in";

			// Inner subquery: Get tim_ids of active_tims that _might_ not have an
			// associated itis code
			selectStatement += " (select active_tim.tim_id from active_tim";
			selectStatement += " left join data_frame on active_tim.tim_id = data_frame.tim_id";
			selectStatement += " left join data_frame_itis_code ON data_frame.data_frame_id = data_frame_itis_code.data_frame_id";
			selectStatement += " where data_frame_itis_code.itis_code_id is null)";

			// Outer subquery (cont'd): Group by tim_id and filter out any records that have
			// a tim_id
			// associated with both null and valid itis codes (we only want tim_ids
			// associated with just null itis codes)
			selectStatement += " group by active_tim.tim_id";
			selectStatement += " having max(data_frame_itis_code.itis_code_id) is null)";

			rs = statement.executeQuery(selectStatement);

			activeTims = getActiveTimFromRS(rs, false);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTims);
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
		return ResponseEntity.ok(activeTims);
	}

	@RequestMapping(value = "/not-sent", method = RequestMethod.GET)
	public ResponseEntity<List<ActiveTim>> GetActiveTimsNotSent() {
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select active_tim.* from active_tim";
			selectStatement += " left join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
			selectStatement += " where active_tim.sat_record_id is null";
			selectStatement += " and tim_rsu.rsu_id is null";

			rs = statement.executeQuery(selectStatement);

			activeTims = getActiveTimFromRS(rs, false);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTims);
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
		return ResponseEntity.ok(activeTims);
	}

	@RequestMapping(value = "/expired", method = RequestMethod.GET)
	public ResponseEntity<List<ActiveTim>> GetExpiredActiveTims() {
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, ACTIVE_TIM.DIRECTION, SAT_RECORD_ID, START_LATITUDE, START_LONGITUDE, END_LATITUDE, END_LONGITUDE, TYPE, CLIENT_ID, ROUTE, TIM_END, TIM_START, EXPIRATION_DATE, PK, ACTIVE_TIM.TIM_TYPE_ID from active_tim";
			selectStatement += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
			selectStatement += "  WHERE TIM_END <= SYS_EXTRACT_UTC(SYSTIMESTAMP)";

			rs = statement.executeQuery(selectStatement);
			activeTims = getActiveTimFromRS(rs, true);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTims);
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

		return ResponseEntity.ok(activeTims);
	}

	@RequestMapping(value = "/indices-rsu/{rsuTarget}", method = RequestMethod.GET)
	public ResponseEntity<List<Integer>> GetActiveTimIndicesByRsu(@PathVariable String rsuTarget) {

		List<Integer> indices = new ArrayList<Integer>();

		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = dbInteractions.getConnectionPool();
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
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(indices);
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

		return ResponseEntity.ok(indices);
	}

	@RequestMapping(value = { "/client-id-direction/{clientId}/{timTypeId}",
			"/client-id-direction/{clientId}/{timTypeId}/{direction}" }, method = RequestMethod.GET)
	public ResponseEntity<List<ActiveTim>> GetActiveTimsByClientIdDirection(@PathVariable String clientId,
			@PathVariable Long timTypeId, @PathVariable(required = false) String direction) {
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();
			String query = "select * from active_tim where CLIENT_ID = '" + clientId + "' and TIM_TYPE_ID = "
					+ timTypeId;

			if (direction != null) {
				query += " and DIRECTION = '" + direction + "'";
			}

			rs = statement.executeQuery(query);
			activeTims = getActiveTimFromRS(rs, false);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTims);
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

		return ResponseEntity.ok(activeTims);
	}

	@RequestMapping(value = "/itis-codes/{activeTimId}", method = RequestMethod.GET)
	public ResponseEntity<List<Integer>> GetItisCodesForActiveTim(@PathVariable Long activeTimId) {
		List<Integer> itisCodes = new ArrayList<>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();
			String selectStatement = "select itis_code from active_tim ";
			selectStatement += "inner join tim on tim.tim_id = active_tim.tim_id ";
			selectStatement += "inner join data_frame on tim.tim_id = data_frame.tim_id ";
			selectStatement += "inner join data_frame_itis_code on data_frame_itis_code.data_frame_id = data_frame.data_frame_id ";
			selectStatement += "inner join itis_code on data_frame_itis_code.itis_code_id = itis_code.itis_code_id ";
			selectStatement += "where active_tim_id = " + activeTimId;
			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				itisCodes.add(rs.getInt("ITIS_CODE"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(itisCodes);
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
		return ResponseEntity.ok(itisCodes);
	}

	@RequestMapping(value = "/delete-id/{activeTimId}", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<Boolean> DeleteActiveTim(@PathVariable Long activeTimId) {

		boolean deleteActiveTimResult = false;

		String deleteSQL = "DELETE FROM ACTIVE_TIM WHERE ACTIVE_TIM_ID = ?";

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {

			connection = dbInteractions.getConnectionPool();
			preparedStatement = connection.prepareStatement(deleteSQL);
			preparedStatement.setLong(1, activeTimId);

			// execute delete SQL stetement
			deleteActiveTimResult = dbInteractions.updateOrDelete(preparedStatement);

			System.out.println("Active Tim (active_tim_id " + activeTimId + ") is deleted!");

		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(deleteActiveTimResult);
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

		return ResponseEntity.ok(deleteActiveTimResult);
	}

	@RequestMapping(value = "/delete-ids", method = RequestMethod.DELETE, headers = "Accept=application/json")
	public ResponseEntity<Boolean> DeleteActiveTimsById(@RequestBody List<Long> activeTimIds) {
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

			connection = dbInteractions.getConnectionPool();
			preparedStatement = connection.prepareStatement(deleteSQL);
			for (int i = 0; i < activeTimIds.size(); i++) {
				preparedStatement.setLong(i + 1, activeTimIds.get(i));
			}

			// execute delete SQL stetement
			deleteActiveTimResult = dbInteractions.updateOrDelete(preparedStatement);

			System.out.println("Active Tims (active_tim_ids "
					+ activeTimIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ") deleted!");

		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(deleteActiveTimResult);
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

		return ResponseEntity.ok(deleteActiveTimResult);
	}

	@RequestMapping(value = "/get-by-ids", method = RequestMethod.POST)
	public ResponseEntity<List<ActiveTim>> GetActiveTimsByIds(@RequestBody List<Long> ids) {
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		if (ids == null || ids.size() == 0) {
			return ResponseEntity.badRequest().body(activeTims);
		}
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();
			String query = "select * from active_tim where active_tim_id in (";

			for (int i = 0; i < ids.size(); i++) {
				query += "?, ";
			}
			query = query.substring(0, query.length() - 2);// subtract ', '
			query += ")";
			ps = connection.prepareStatement(query);

			for (int i = 0; i < ids.size(); i++) {
				// set active_tim_id
				ps.setLong(i + 1, ids.get(i));
			}
			rs = ps.executeQuery();
			activeTims = getActiveTimFromRS(rs, false);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTims);
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

		return ResponseEntity.ok(activeTims);
	}

	@RequestMapping(value = "/get-by-wydot-tim/{timTypeId}", method = RequestMethod.POST)
	public ResponseEntity<List<ActiveTim>> GetActiveTimsByWydotTim(@RequestBody List<? extends WydotTim> wydotTims,
			@PathVariable Long timTypeId) {
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		WydotTim wydotTim = null;

		try {
			connection = dbInteractions.getConnectionPool();
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
				if (wydotTim.getDirection() != null && !wydotTim.getDirection().equalsIgnoreCase("B")) {
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
				if (wydotTim.getDirection() != null && !wydotTim.getDirection().equalsIgnoreCase("B")) {
					ps.setString(index, wydotTim.getDirection());
					index++;
				}
			}
			rs = ps.executeQuery();
			activeTims = getActiveTimFromRS(rs, false);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTims);
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

		return ResponseEntity.ok(activeTims);
	}

	@RequestMapping(value = "/tim-type-id/{timTypeId}", method = RequestMethod.GET)
	public ResponseEntity<List<ActiveTim>> GetActiveTimsByType(@PathVariable Long timTypeId) {
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from active_tim where TIM_TYPE_ID = " + timTypeId);
			activeTims = getActiveTimFromRS(rs, false);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTims);
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

		return ResponseEntity.ok(activeTims);
	}

	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public ResponseEntity<List<ActiveTim>> GetAllActiveTims() {
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select * from active_tim";

			rs = statement.executeQuery(selectStatement);
			activeTims = getActiveTimFromRS(rs, false);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTims);
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

		return ResponseEntity.ok(activeTims);
	}

	@RequestMapping(value = "/all-sdx", method = RequestMethod.GET)
	public ResponseEntity<List<ActiveTim>> GetAllActiveSDXTims() {
		return getActiveTimsWithItisCodes(true, false);
	}

	@RequestMapping(value = "/all-with-itis", method = RequestMethod.GET)
	public ResponseEntity<List<ActiveTim>> GetAllActiveTimsWithItis(
			@RequestParam(required = false) Boolean excludeVslAndParking) {
		// Configure default value
		if (excludeVslAndParking == null) {
			excludeVslAndParking = false;
		}

		return getActiveTimsWithItisCodes(false, excludeVslAndParking);
	}

	private ResponseEntity<List<ActiveTim>> getActiveTimsWithItisCodes(boolean sdxOnly, boolean excludeVslAndParking) {
		List<ActiveTim> results = new ArrayList<ActiveTim>();
		ActiveTim activeTim = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();

			String query = "select active_tim.*, tim_type.type, itis_code.itis_code from active_tim";
			query += " left join tim_type on active_tim.tim_type_id = tim_type.tim_type_id";
			query += " left join data_frame on active_tim.tim_id = data_frame.tim_id";
			query += " left join data_frame_itis_code on data_frame.data_frame_id = data_frame_itis_code.data_frame_id";
			query += " left join itis_code on data_frame_itis_code.itis_code_id = itis_code.itis_code_id";

			if (sdxOnly) {
				query += " where sat_record_id is not null";
			}

			if (excludeVslAndParking) {
				if (query.contains("where")) {
					query += " and tim_type.type not in ('P', 'VSL')";
				} else {
					query += " where tim_type.type not in ('P', 'VSL')";
				}
			}

			query += " order by active_tim.active_tim_id";

			rs = statement.executeQuery(query);

			// convert to ActiveTim object
			while (rs.next()) {
				Long activeTimId = rs.getLong("ACTIVE_TIM_ID");

				// If we're looking at the first record or the record doesn't have
				// the same ACTIVE_TIM_ID as the record we just processed...
				if (activeTim == null || !activeTim.getActiveTimId().equals(activeTimId)) {
					if (activeTim != null) {
						results.add(activeTim);
					}

					// Create a new record and set the ActiveTim properties.
					activeTim = new ActiveTim();
					activeTim.setActiveTimId(activeTimId);
					activeTim.setTimId(rs.getLong("TIM_ID"));
					activeTim.setDirection(rs.getString("DIRECTION"));
					activeTim.setStartDateTime(rs.getString("TIM_START"));
					activeTim.setEndDateTime(rs.getString("TIM_END"));
					activeTim.setExpirationDateTime(rs.getString("EXPIRATION_DATE"));
					activeTim.setRoute(rs.getString("ROUTE"));
					activeTim.setClientId(rs.getString("CLIENT_ID"));
					activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
					activeTim.setPk(rs.getInt("PK"));
					activeTim.setItisCodes(new ArrayList<Integer>());

					Coordinate startPoint = null;
					Coordinate endPoint = null;

					// Set startPoint
					BigDecimal startLat = rs.getBigDecimal("START_LATITUDE");
					BigDecimal startLon = rs.getBigDecimal("START_LONGITUDE");
					if (!rs.wasNull()) {
						startPoint = new Coordinate(startLat, startLon);
					}
					activeTim.setStartPoint(startPoint);

					// Set endPoint
					BigDecimal endLat = rs.getBigDecimal("END_LATITUDE");
					BigDecimal endLon = rs.getBigDecimal("END_LONGITUDE");
					if (!rs.wasNull()) {
						endPoint = new Coordinate(endLat, endLon);
					}
					activeTim.setEndPoint(endPoint);

					// Set timType
					long timTypeId = rs.getLong("TIM_TYPE_ID");
					if (!rs.wasNull()) {
						activeTim.setTimTypeId(timTypeId);
						activeTim.setTimType(rs.getString("TYPE"));
					}

					// Set projectKey
					int projectKey = rs.getInt("PROJECT_KEY");
					if (!rs.wasNull()) {
						activeTim.setProjectKey(projectKey);
					}
				}

				// Add the ITIS code to the ActiveTim's ITIS codes, if not null
				var itisCode = rs.getInt("ITIS_CODE");
				if (!rs.wasNull()) {
					activeTim.getItisCodes().add(itisCode);
				}
			}

			if (activeTim != null) {
				results.add(activeTim);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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

		return ResponseEntity.ok(results);
	}

	/**
	 * Get all ActiveTims (including RSU address and RSU Index)
	 * 
	 * @return List of ActiveTims, sorted by RSU and RSU Index
	 */
	@RequestMapping(value = "/active-rsu-tims", method = RequestMethod.GET)
	public ResponseEntity<List<ActiveTim>> GetActiveRsuTims() {
		List<ActiveTim> results = new ArrayList<ActiveTim>();
		ActiveTim activeTim = null;

		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();

			String query = "select active_tim.*, rsu_vw.ipv4_address, tim_rsu.rsu_index from active_tim";
			query += " inner join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
			query += " inner join rsu on tim_rsu.rsu_id = rsu.rsu_id";
			query += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
			query += " where sat_record_id is null";
			query += " order by rsu_vw.ipv4_address, tim_rsu.rsu_index"; // Required by ValidateRsus

			rs = statement.executeQuery(query);

			// convert to ActiveTim object
			while (rs.next()) {
				// Create ActiveTim record
				activeTim = new ActiveTim();
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setDirection(rs.getString("DIRECTION"));
				activeTim.setStartDateTime(rs.getString("TIM_START"));
				activeTim.setEndDateTime(rs.getString("TIM_END"));
				activeTim.setExpirationDateTime(rs.getString("EXPIRATION_DATE"));
				activeTim.setTimTypeId(rs.getLong("TIM_TYPE_ID"));
				activeTim.setRoute(rs.getString("ROUTE"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setPk(rs.getInt("PK"));
				activeTim.setRsuTarget(rs.getString("IPV4_ADDRESS"));
				activeTim.setRsuIndex(rs.getInt("RSU_INDEX"));
				Coordinate startPoint = null;
				Coordinate endPoint = null;
				BigDecimal startLat = rs.getBigDecimal("START_LATITUDE");
				BigDecimal startLon = rs.getBigDecimal("START_LONGITUDE");
				if (!rs.wasNull()) {
					startPoint = new Coordinate(startLat, startLon);
				}
				activeTim.setStartPoint(startPoint);

				BigDecimal endLat = rs.getBigDecimal("END_LATITUDE");
				BigDecimal endLon = rs.getBigDecimal("END_LONGITUDE");
				if (!rs.wasNull()) {
					endPoint = new Coordinate(endLat, endLon);
				}
				activeTim.setEndPoint(endPoint);
				results.add(activeTim);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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

		return ResponseEntity.ok(results);
	}

	@RequestMapping(value = "/active-rsu-tim", method = RequestMethod.POST)
	public ResponseEntity<ActiveTim> GetActiveRsuTim(@RequestBody ActiveRsuTimQueryModel artqm) {

		ActiveTim activeTim = null;
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();
			String query = "select * from active_tim";
			query += " inner join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
			query += " inner join rsu on tim_rsu.rsu_id = rsu.rsu_id";
			query += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
			query += " where ipv4_address = '" + artqm.getIpv4() + "' and client_id = '" + artqm.getClientId()
					+ "' and active_tim.direction = '" + artqm.getDirection() + "'";

			rs = statement.executeQuery(query);
			List<ActiveTim> activeTims = getActiveTimFromRS(rs, false);
			if (activeTims.size() > 0) {
				activeTim = activeTims.get(activeTims.size() - 1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTim);
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

		return ResponseEntity.ok(activeTim);
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	public ResponseEntity<Long> InsertActiveTim(@RequestBody ActiveTim activeTim) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;
		Long activeTimId = 0l;
		try {
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("active_tim",
					timOracleTables.getActiveTimTable());

			// get connection
			connection = dbInteractions.getConnectionPool();

			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "active_tim_id" });
			int fieldNum = 1;

			for (String col : timOracleTables.getActiveTimTable()) {
				if (col.equals("TIM_ID"))
					sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, activeTim.getTimId());
				else if (col.equals("DIRECTION"))
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getDirection());
				else if (col.equals("TIM_START")) {
					java.util.Date tim_start_date = utility.convertDate(activeTim.getStartDateTime());
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
							utility.timestampFormat.format(tim_start_date));
				} else if (col.equals("TIM_END")) {
					if (activeTim.getEndDateTime() != null) {
						java.util.Date tim_end_date = utility.convertDate(activeTim.getEndDateTime());
						sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
								utility.timestampFormat.format(tim_end_date));
					} else
						preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
				} else if (col.equals("EXPIRATION_DATE")) {
					if (activeTim.getExpirationDateTime() != null) {
						java.util.Date tim_exp_date = utility.convertDate(activeTim.getExpirationDateTime());
						sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
								utility.timestampFormat.format(tim_exp_date));
					} else {
						preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
					}
				} else if (col.equals("TIM_TYPE_ID"))
					sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, activeTim.getTimTypeId());
				else if (col.equals("ROUTE"))
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getRoute());
				else if (col.equals("CLIENT_ID"))
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getClientId());
				else if (col.equals("SAT_RECORD_ID"))
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getSatRecordId());
				else if (col.equals("PK"))
					sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, activeTim.getPk());
				else if (col.equals("START_LATITUDE")) {
					BigDecimal start_lat = null;
					if (activeTim.getStartPoint() != null)
						start_lat = activeTim.getStartPoint().getLatitude();
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, start_lat);
				} else if (col.equals("START_LONGITUDE")) {
					BigDecimal start_lon = null;
					if (activeTim.getStartPoint() != null)
						start_lon = activeTim.getStartPoint().getLongitude();
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, start_lon);
				} else if (col.equals("END_LATITUDE")) {
					BigDecimal end_lat = null;
					if (activeTim.getEndPoint() != null)
						end_lat = activeTim.getEndPoint().getLatitude();
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, end_lat);
				} else if (col.equals("END_LONGITUDE")) {
					BigDecimal end_lon = null;
					if (activeTim.getEndPoint() != null)
						end_lon = activeTim.getEndPoint().getLongitude();
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, end_lon);
				}

				fieldNum++;
			}

			activeTimId = dbInteractions.executeAndLog(preparedStatement, "active tim");
			return ResponseEntity.ok(activeTimId);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTimId);
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
	}

	private List<ActiveTim> getActiveTimFromRS(ResultSet rs, boolean includeType) throws SQLException {
		List<ActiveTim> activeTims = new ArrayList<>();
		ActiveTim activeTim = null;

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
			activeTim.setExpirationDateTime(rs.getString("EXPIRATION_DATE"));
			activeTim.setRoute(rs.getString("ROUTE"));
			activeTim.setPk(rs.getInt("PK"));
			activeTim.setTimTypeId(rs.getLong("TIM_TYPE_ID"));

			if (includeType) {
				activeTim.setTimType(rs.getString("TYPE"));
			}

			Coordinate startPoint = null;
			Coordinate endPoint = null;
			BigDecimal startLat = rs.getBigDecimal("START_LATITUDE");
			BigDecimal startLon = rs.getBigDecimal("START_LONGITUDE");
			if (!rs.wasNull()) {
				startPoint = new Coordinate(startLat, startLon);
			}
			activeTim.setStartPoint(startPoint);

			BigDecimal endLat = rs.getBigDecimal("END_LATITUDE");
			BigDecimal endLon = rs.getBigDecimal("END_LONGITUDE");
			if (!rs.wasNull()) {
				endPoint = new Coordinate(endLat, endLon);
			}
			activeTim.setEndPoint(endPoint);

			activeTims.add(activeTim);
		}

		return activeTims;
	}

	private String translateIso8601ToTimestampFormat(String date) throws ParseException {
		DateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
		// TimeZone toTimeZone = TimeZone.getTimeZone("MST");
		// sdf.setTimeZone(toTimeZone);
		DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		Date dte = m_ISO8601Local.parse(date);
		return sdf.format(dte.getTime());
	}

	@RequestMapping(value = "/update-expiration/{packetID}/{startDate}/{expDate}", method = RequestMethod.PUT)
	public ResponseEntity<Boolean> UpdateExpiration(@PathVariable String packetID, @PathVariable String startDate,
			@PathVariable String expDate) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		boolean success = false;

		String query = "SELECT ACTIVE_TIM_ID FROM ACTIVE_TIM atim";
		query += " INNER JOIN TIM ON atim.TIM_ID = TIM.TIM_ID";
		query += " WHERE TIM.PACKET_ID = ? AND atim.TIM_START = ?";

		String updateStatement = "UPDATE ACTIVE_TIM SET EXPIRATION_DATE = ? WHERE ACTIVE_TIM_ID IN (";
		updateStatement += query;
		updateStatement += ")";

		try {
			connection = dbInteractions.getConnectionPool();
			preparedStatement = connection.prepareStatement(updateStatement);
			preparedStatement.setObject(1, expDate);// expDate comes in as MST from previously called function
													// (GetMinExpiration)
			preparedStatement.setObject(2, packetID);
			preparedStatement.setObject(3, translateIso8601ToTimestampFormat(startDate));

			// execute update statement
			success = dbInteractions.updateOrDelete(preparedStatement);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
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
		utility.logWithDate(
				String.format("Called UpdateExpiration with packetID: %s, startDate: %s, expDate: %s. Successful: %s",
						packetID, startDate, expDate, success));
		return ResponseEntity.ok(success);
	}

	@RequestMapping(value = "/get-min-expiration/{packetID}/{startDate}/{expDate}")
	public ResponseEntity<String> GetMinExpiration(@PathVariable String packetID, @PathVariable String startDate,
			@PathVariable String expDate) throws ParseException {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		String minStart = "";

		try {
			// Fetch the minimum of passed in expDate and database held
			// active_tim.expiration_date. To compare like values we convert the expDate
			// TO_TIMESTAMP. Without this it compares string length.
			// Also, there are some null values in the db. To get around these, we use the
			// coalesce function with the expDate passed in value.
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();
			String selectTimestamp = String.format("SELECT TO_TIMESTAMP('%s', 'DD-MON-RR HH12.MI.SS.FF PM') FROM DUAL",
					translateIso8601ToTimestampFormat(expDate));

			String minExpDate = "SELECT MIN(EXPIRATION_DATE) FROM ACTIVE_TIM atim";
			minExpDate += " INNER JOIN TIM ON atim.TIM_ID = TIM.TIM_ID";
			minExpDate += " WHERE TIM.PACKET_ID = '" + packetID + "'";
			minExpDate += " AND atim.TIM_START = '" + translateIso8601ToTimestampFormat(startDate) + "'";

			String query = String.format("SELECT LEAST((%s), (COALESCE((%s),(%s)))) minStart FROM DUAL",
					selectTimestamp, minExpDate, selectTimestamp);
			rs = statement.executeQuery(query);
			while (rs.next()) {
				var tmpTs = rs.getTimestamp("MINSTART", UTCCalendar);
				minStart = utility.timestampFormat.format(tmpTs);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(minStart);
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
		utility.logWithDate(String.format(
				"Called GetMinExpiration with packetID: %s, startDate: %s, expDate: %s. Min start date: %s", packetID,
				startDate, expDate, minStart));
		return ResponseEntity.ok(minStart);
	}
}
