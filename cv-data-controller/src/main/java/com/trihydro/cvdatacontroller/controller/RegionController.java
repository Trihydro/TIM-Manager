package com.trihydro.cvdatacontroller.controller;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
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
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;

@CrossOrigin
@RestController
@RequestMapping("region")
@ApiIgnore
public class RegionController extends BaseController {

	private TimOracleTables timOracleTables;
	private SQLNullHandler sqlNullHandler;

	@Autowired
	public void InjectDependencies(TimOracleTables _timOracleTables, SQLNullHandler _sqlNullHandler) {
		timOracleTables = _timOracleTables;
		sqlNullHandler = _sqlNullHandler;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/update-region-name/{regionId}/{name}")
	public ResponseEntity<Boolean> UpdateRegionName(@PathVariable Long regionId, @PathVariable String name) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		List<Pair<String, Object>> cols = new ArrayList<Pair<String, Object>>();
		cols.add(new ImmutablePair<String, Object>("NAME", name));

		try {
			connection = GetConnectionPool();
			preparedStatement = timOracleTables.buildUpdateStatement(regionId, "REGION", "REGION_ID", cols, connection);

			// execute update statement
			Boolean success = updateOrDelete(preparedStatement);
			return ResponseEntity.ok(success);
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
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/add-region/{dataFrameId}/{pathId}")
	public ResponseEntity<Long> AddRegion(@PathVariable Long dataFrameId, @PathVariable Long pathId,
			@RequestBody Region region) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = GetConnectionPool();
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("region",
					timOracleTables.getRegionTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "region_id" });

			OdePosition3D anchor = null;
			if (region != null)
				anchor = region.getAnchorPosition();

			int fieldNum = 1;

			Region.Geometry geometry = region.getGeometry();

			for (String col : timOracleTables.getRegionTable()) {
				if (col.equals("DATA_FRAME_ID"))
					sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, dataFrameId);
				else if (col.equals("NAME"))
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, region.getName());
				else if (col.equals("LANE_WIDTH"))
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, region.getLaneWidth());
				else if (col.equals("DIRECTIONALITY"))
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, region.getDirectionality());
				else if (col.equals("DIRECTION"))
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, region.getDirection());
				else if (col.equals("CLOSED_PATH"))
					preparedStatement.setBoolean(fieldNum, region.isClosedPath());
				else if (col.equals("ANCHOR_LAT") && anchor != null)
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, anchor.getLatitude());
				else if (col.equals("ANCHOR_LONG") && anchor != null)
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, anchor.getLongitude());
				else if (col.equals("PATH_ID"))
					sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);
				else if (col.equals("GEOMETRY_DIRECTION")) {
					String direction = (pathId == null && geometry != null) ? geometry.getDirection() : null;
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, direction);
				} else if (col.equals("GEOMETRY_EXTENT")) {
					Integer extent = (pathId == null && geometry != null) ? geometry.getExtent() : null;
					sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, extent);
				} else if (col.equals("GEOMETRY_LANE_WIDTH")) {
					BigDecimal laneWidth = (pathId == null && geometry != null) ? geometry.getLaneWidth() : null;
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, laneWidth);
				} else if (col.equals("GEOMETRY_CIRCLE_POSITION_LAT")) {
					BigDecimal lat = null;
					if (pathId == null && geometry != null && geometry.getCircle() != null
							&& geometry.getCircle().getPosition() != null) {
						lat = geometry.getCircle().getPosition().getLatitude();
					}
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, lat);
				} else if (col.equals("GEOMETRY_CIRCLE_POSITION_LONG")) {
					BigDecimal lon = null;
					if (pathId == null && geometry != null && geometry.getCircle() != null
							&& geometry.getCircle().getPosition() != null) {
						lon = geometry.getCircle().getPosition().getLongitude();
					}
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, lon);
				} else if (col.equals("GEOMETRY_CIRCLE_POSITION_ELEV")) {
					BigDecimal elev = null;
					if (pathId == null && geometry != null && geometry.getCircle() != null
							&& geometry.getCircle().getPosition() != null) {
						elev = geometry.getCircle().getPosition().getElevation();
					}
					sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, elev);
				} else if (col.equals("GEOMETRY_CIRCLE_RADIUS")) {
					Integer rad = (pathId == null && geometry != null && geometry.getCircle() != null)
							? geometry.getCircle().getRadius()
							: null;
					sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, rad);
				} else if (col.equals("GEOMETRY_CIRCLE_UNITS")) {
					String units = (pathId == null && geometry != null && geometry.getCircle() != null)
							? geometry.getCircle().getUnits()
							: null;
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, units);
				}
				fieldNum++;
			}

			// execute insert statement
			Long regionId = log(preparedStatement, "regionID");
			return ResponseEntity.ok(regionId);
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
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Long(0));
	}

}