package com.trihydro.library.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.SharedFieldsModel;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.tables.TimOracleTables;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;

public class RegionService extends CvDataServiceLibrary {

	private static int setSharedInsertFields(PreparedStatement preparedStatement, Long dataFrameId, Region region)
			throws SQLException {
		OdePosition3D anchor = null;
		if (region != null)
			anchor = region.getAnchorPosition();

		int fieldNum = 1;

		for (String col : TimOracleTables.getRegionTable()) {
			if (col.equals("DATA_FRAME_ID")) {
				SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, dataFrameId);
				fieldNum++;
			} else if (col.equals("NAME")) {
				SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, region.getName());
				fieldNum++;
			} else if (col.equals("LANE_WIDTH")) {
				SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, region.getLaneWidth());
				fieldNum++;
			} else if (col.equals("DIRECTIONALITY")) {
				SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, region.getDirectionality());
				fieldNum++;
			} else if (col.equals("DIRECTION")) {
				SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, region.getDirection());
				fieldNum++;
			} else if (col.equals("CLOSED_PATH")) {
				preparedStatement.setBoolean(fieldNum, region.isClosedPath());
				fieldNum++;
			} else if (col.equals("ANCHOR_LAT") && anchor != null) {
				SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, anchor.getLatitude());
				fieldNum++;
			} else if (col.equals("ANCHOR_LONG") && anchor != null) {
				SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, anchor.getLongitude());
				fieldNum++;
			}
		}
		return fieldNum;

		// SharedFieldsModel model = new SharedFieldsModel();
		// model.setFieldCount(fieldNum);
		// model.setPreparedStatement(preparedStatement);
		// return model;
	}

	public static Long insertPathRegion(Long dataFrameId, Long pathId, Region region) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = DbUtility.getConnectionPool();
			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("region",
					TimOracleTables.getRegionTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "region_id" });
			int fieldNum = setSharedInsertFields(preparedStatement, dataFrameId, region);

			for (String col : TimOracleTables.getRegionTable()) {
				if (col.equals("PATH_ID")) {
					Utility.logWithDate("Inserting path_id at index " + fieldNum);
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);
					fieldNum++;
				}
			}
			// execute insert statement
			Long regionId = log(preparedStatement, "regionID");
			return regionId;
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

	public static Long insertGeometryRegion(Long dataFrameId, Region.Geometry geometry, Region region) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = DbUtility.getConnectionPool();
			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("region",
					TimOracleTables.getRegionTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "region_id" });
			int fieldNum = setSharedInsertFields(preparedStatement, dataFrameId, region);

			for (String col : TimOracleTables.getRegionTable()) {
				if (col.equals("GEOMETRY_DIRECTION")) {
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, geometry.getDirection());
					fieldNum++;
				} else if (col.equals("GEOMETRY_EXTENT")) {
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, geometry.getExtent());
					fieldNum++;
				} else if (col.equals("GEOMETRY_LANE_WIDTH")) {
					SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, geometry.getLaneWidth());
					fieldNum++;
				} else if (col.equals("GEOMETRY_CIRCLE_POSITION_LAT")) {
					SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum,
							geometry.getCircle().getPosition().getLatitude());
					fieldNum++;
				} else if (col.equals("GEOMETRY_CIRCLE_POSITION_LONG")) {
					SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum,
							geometry.getCircle().getPosition().getLongitude());
					fieldNum++;
				} else if (col.equals("GEOMETRY_CIRCLE_POSITION_ELEV")) {
					SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum,
							geometry.getCircle().getPosition().getElevation());
					fieldNum++;
				} else if (col.equals("GEOMETRY_CIRCLE_RADIUS")) {
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, geometry.getCircle().getRadius());
					fieldNum++;
				} else if (col.equals("GEOMETRY_CIRCLE_UNITS")) {
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, geometry.getCircle().getUnits());
					fieldNum++;
				}
			}
			// execute insert statement
			Long regionId = log(preparedStatement, "regionID");
			return regionId;
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

	public static Boolean updateRegionName(Long regionId, String name) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		List<Pair<String, Object>> cols = new ArrayList<Pair<String, Object>>();
		cols.add(new ImmutablePair<String, Object>("NAME", name));

		try {
			connection = DbUtility.getConnectionPool();
			preparedStatement = TimOracleTables.buildUpdateStatement(regionId, "REGION", "REGION_ID", cols, connection);

			// execute update statement
			Boolean success = updateOrDelete(preparedStatement);
			return success;
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
		return false;
	}

}
