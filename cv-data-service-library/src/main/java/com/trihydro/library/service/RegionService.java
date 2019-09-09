package com.trihydro.library.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.SharedFieldsModel;

import java.sql.SQLException;
import com.trihydro.library.tables.TimOracleTables;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;

public class RegionService extends CvDataServiceLibrary {

	private static SharedFieldsModel setSharedInsertFields(Connection connection, Long dataFrameId, Region region)
			throws SQLException {
		String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("region",
				TimOracleTables.getRegionTable());
		PreparedStatement preparedStatement = connection.prepareStatement(insertQueryStatement,
				new String[] { "region_id" });

		OdePosition3D anchor = null;
		if (region != null)
			anchor = region.getAnchorPosition();

		int fieldNum = 1;

		for (String col : TimOracleTables.getRegionTable()) {
			if (col.equals("DATA_FRAME_ID"))
				SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, dataFrameId);
			else if (col.equals("ANCHOR_LAT") && anchor != null)
				SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, anchor.getLatitude());
			else if (col.equals("ANCHOR_LONG") && anchor != null)
				SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, anchor.getLongitude());
			else if (col.equals("NAME"))
				SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, null);
			else if (col.equals("LANE_WIDTH"))
				SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, region.getLaneWidth());
			else if (col.equals("DIRECTIONALITY"))
				SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum,
						new BigDecimal(region.getDirectionality()));
			else if (col.equals("DIRECTION"))
				SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, region.getDirection());
			else if (col.equals("CLOSED_PATH"))
				preparedStatement.setBoolean(fieldNum, region.isClosedPath());
			fieldNum++;
		}

		SharedFieldsModel model = new SharedFieldsModel();
		model.setFieldCount(fieldNum);
		model.setPreparedStatement(preparedStatement);
		return model;
	}

	public static Long insertPathRegion(Long dataFrameId, Long pathId, Region region) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = DbUtility.getConnectionPool();
			SharedFieldsModel model = setSharedInsertFields(connection, dataFrameId, region);
			preparedStatement = model.getPreparedStatement();
			int fieldNum = model.getFieldCount();

			for (String col : TimOracleTables.getRegionTable()) {
				if (col.equals("PATH_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);
				fieldNum++;
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
			SharedFieldsModel model = setSharedInsertFields(connection, dataFrameId, region);
			preparedStatement = model.getPreparedStatement();
			int fieldNum = model.getFieldCount();

			for (String col : TimOracleTables.getRegionTable()) {
				if (col.equals("GEOMETRY_DIRECTION"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, geometry.getDirection());
				else if (col.equals("GEOMETRY_EXTENT"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, geometry.getExtent());
				else if (col.equals("GEOMETRY_LANE_WIDTH"))
					SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, geometry.getLaneWidth());
				else if (col.equals("GEOMETRY_CIRCLE_POSITION_LAT"))
					SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum,
							geometry.getCircle().getPosition().getLatitude());
				else if (col.equals("GEOMETRY_CIRCLE_POSITION_LONG"))
					SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum,
							geometry.getCircle().getPosition().getLongitude());
				else if (col.equals("GEOMETRY_CIRCLE_POSITION_ELEV"))
					SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum,
							geometry.getCircle().getPosition().getElevation());
				else if (col.equals("GEOMETRY_CIRCLE_RADIUS"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, geometry.getCircle().getRadius());
				else if (col.equals("GEOMETRY_CIRCLE_UNITS"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, geometry.getCircle().getUnits());
				fieldNum++;
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
}
