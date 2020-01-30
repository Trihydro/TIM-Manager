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
import com.trihydro.library.model.TimUpdateModel;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
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
	public void SetTables(TimOracleTables _timOracleTables) {
		timOracleTables = _timOracleTables;
	}

	// select all ITIS codes
	@RequestMapping(value = "/expiring", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<TimUpdateModel> getExpiringActiveTims() {
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

	@RequestMapping(value = "/update-sat-record-id/{activeTimId}/{satRecordId}", method = RequestMethod.PUT)
	public Boolean updateActiveTim_SatRecordId(@PathVariable Long activeTimId, @PathVariable String satRecordId) {
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
}
