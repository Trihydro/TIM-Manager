package com.trihydro.loggerkafkaconsumer.app.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;

@Component
public class RegionService extends BaseService {

    private TimOracleTables timOracleTables;
	private SQLNullHandler sqlNullHandler;

	@Autowired
	public void InjectDependencies(TimOracleTables _timOracleTables, SQLNullHandler _sqlNullHandler) {
		timOracleTables = _timOracleTables;
		sqlNullHandler = _sqlNullHandler;
    }
    
    public Long AddRegion(Long dataFrameId, Long pathId, Region region) {

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
            Long regionId = executeAndLog(preparedStatement, "regionID");
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