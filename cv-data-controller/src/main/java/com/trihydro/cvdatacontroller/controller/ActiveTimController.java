package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.cvdatacontroller.tables.TimOracleTables;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimUpdateModel;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("active-tim")
@ApiIgnore
public class ActiveTimController extends BaseController {

	private TimOracleTables timOracleTables;

	@Autowired
	public ActiveTimController(TimOracleTables _timOracleTables) {
		timOracleTables = _timOracleTables;
	}

	// select all ITIS codes
	@RequestMapping(value = "/expiring", method = RequestMethod.GET, headers = "Accept=application/json")
	public ResponseEntity<List<TimUpdateModel>> GetExpiringActiveTims() {
		TimUpdateModel activeTim = null;
		List<TimUpdateModel> activeTims = new ArrayList<TimUpdateModel>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = GetConnectionPool();
			statement = connection.createStatement();

			String selectStatement = "SELECT atim.*, tt.type as tim_type_name, tt.description as tim_type_description";
			selectStatement += ", t.msg_cnt, t.url_b, t.is_satellite, t.sat_record_id, t.packet_id";
			selectStatement += ", df.data_frame_id, df.frame_type, df.duration_time, df.ssp_tim_rights, df.ssp_location_rights";
			selectStatement += ", df.ssp_msg_types, df.ssp_msg_content, df.content AS df_Content, df.url";
			selectStatement += ", r.region_id, r.name as region_name, r.anchor_lat, r.anchor_long, r.lane_width";
			selectStatement += ", r.path_id, r.closed_path, r.description AS region_description";
			selectStatement += ", r.directionality, r.direction AS region_direction";
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
				activeTim.setPacketId(rs.getString("PACKET_ID"));

				// Tim Type properties
				activeTim.setTimTypeName(rs.getString("TIM_TYPE_NAME"));
				activeTim.setTimTypeDescription(rs.getString("TIM_TYPE_DESCRIPTION"));

				// Region Properties
				activeTim.setRegionId(rs.getInt("REGION_ID"));
				activeTim.setRegionName(rs.getString("REGION_NAME"));
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
				activeTim.setSspLocationRights(Utility.GetShortValueFromResultSet(rs, "SSP_LOCATION_RIGHTS"));
				activeTim.setSspTimRights(Utility.GetShortValueFromResultSet(rs, "SSP_TIM_RIGHTS"));
				activeTim.setSspMsgTypes(Utility.GetShortValueFromResultSet(rs, "SSP_MSG_TYPES"));
				activeTim.setSspMsgContent(Utility.GetShortValueFromResultSet(rs, "SSP_MSG_CONTENT"));

				// set dataFrame content. it's required for the ODE, so if we didn't record it,
				// assume Advisory
				String dfContent = rs.getString("DF_CONTENT");
				if (dfContent == null || dfContent == "") {
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

	@RequestMapping(value = "/update-sat-record-id/{activeTimId}/{satRecordId}", method = RequestMethod.PUT)
	public ResponseEntity<Boolean> updateActiveTim_SatRecordId(@PathVariable Long activeTimId,
			@PathVariable String satRecordId) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		List<Pair<String, Object>> cols = new ArrayList<Pair<String, Object>>();
		cols.add(new ImmutablePair<String, Object>("SAT_RECORD_ID", satRecordId));
		boolean success = false;

		try {
			connection = GetConnectionPool();
			preparedStatement = timOracleTables.buildUpdateStatement(activeTimId, "ACTIVE_TIM", "ACTIVE_TIM_ID", cols,
					connection);

			// execute update statement
			success = updateOrDelete(preparedStatement);
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
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = GetConnectionPool();

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

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new ActiveTim();
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
				activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
				activeTim.setDirection(rs.getString("DIRECTION"));
				activeTim.setRoute(rs.getString("ROUTE"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTims.add(activeTim);
			}
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
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = GetConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select active_tim.* from active_tim";
			selectStatement += " left join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
			selectStatement += " where active_tim.sat_record_id is null";
			selectStatement += " and tim_rsu.rsu_id is null";

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				activeTim = new ActiveTim();
				activeTim.setTimId(rs.getLong("TIM_ID"));
				activeTim.setMilepostStart(rs.getDouble("MILEPOST_START"));
				activeTim.setMilepostStop(rs.getDouble("MILEPOST_STOP"));
				activeTim.setDirection(rs.getString("DIRECTION"));
				activeTim.setRoute(rs.getString("ROUTE"));
				activeTim.setClientId(rs.getString("CLIENT_ID"));
				activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
				activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
				activeTims.add(activeTim);
			}
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
		ActiveTim activeTim = null;
		List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = GetConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select ACTIVE_TIM_ID, ACTIVE_TIM.TIM_ID, ACTIVE_TIM.DIRECTION, SAT_RECORD_ID, MILEPOST_START, MILEPOST_STOP, TYPE, CLIENT_ID, ROUTE from active_tim";
			selectStatement += " inner join tim_type on tim_type.tim_type_id = active_tim.tim_type_id";
			selectStatement += "  WHERE TIM_END <= SYS_EXTRACT_UTC(SYSTIMESTAMP)";

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
}
