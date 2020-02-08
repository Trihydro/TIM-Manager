package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.BsmOracleTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.j2735.J2735BsmPart2Content;
import us.dot.its.jpo.ode.plugin.j2735.J2735SpecialVehicleExtensions;

@Component
public class BsmPart2SpveService extends BaseService {

    private BsmOracleTables bsmOracleTables;
    //TODO: refactor code to use sqlNullHandler
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(BsmOracleTables _bsmOracleTables, SQLNullHandler _sqlNullHandler) {
        bsmOracleTables = _bsmOracleTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long insertBSMPart2SPVE(J2735BsmPart2Content part2Content, J2735SpecialVehicleExtensions spve,
            Long bsmCoreDataId) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            connection = GetConnectionPool();
            String insertQueryStatement = bsmOracleTables.buildInsertQueryStatement("bsm_part2_spve",
                    bsmOracleTables.getBsmPart2SpveTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "bsm_part2_spve_id" });

            int fieldNum = 1;

            // bsmCoreDataId 1
            preparedStatement.setString(fieldNum, Long.toString(bsmCoreDataId));
            fieldNum++;

            // id 2
            preparedStatement.setString(fieldNum, part2Content.getId().name());
            fieldNum++;

            // va_ssprights 3
            if (spve.getVehicleAlerts() != null && spve.getVehicleAlerts().getSspRights() != null)
                preparedStatement.setString(fieldNum, spve.getVehicleAlerts().getSspRights().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // va_events 4
            if (spve.getVehicleAlerts() != null && spve.getVehicleAlerts().getEvents() != null
                    && spve.getVehicleAlerts().getEvents().getEvent() != null)
                preparedStatement.setString(fieldNum, spve.getVehicleAlerts().getEvents().getEvent().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // va_events_ssprights 5
            if (spve.getVehicleAlerts() != null && spve.getVehicleAlerts().getEvents() != null
                    && spve.getVehicleAlerts().getEvents().getSspRights() != null)
                preparedStatement.setString(fieldNum, spve.getVehicleAlerts().getEvents().getSspRights().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // lightsUse 6
            if (spve.getVehicleAlerts() != null && spve.getVehicleAlerts().getLightsUse() != null)
                preparedStatement.setString(fieldNum, spve.getVehicleAlerts().getLightsUse().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // va_multi 7
            if (spve.getVehicleAlerts() != null && spve.getVehicleAlerts().getMulti() != null)
                preparedStatement.setString(fieldNum, spve.getVehicleAlerts().getMulti().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // va_responsetype 8
            if (spve.getVehicleAlerts() != null && spve.getVehicleAlerts().getResponseType() != null)
                preparedStatement.setString(fieldNum, spve.getVehicleAlerts().getResponseType().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // va_sirenuse 9
            if (spve.getVehicleAlerts() != null && spve.getVehicleAlerts().getSirenUse() != null)
                preparedStatement.setString(fieldNum, spve.getVehicleAlerts().getSirenUse().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // description 10
            if (spve.getDescription() != null && spve.getDescription().getDescription() != null)
                preparedStatement.setString(fieldNum, spve.getDescription().getDescription().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // desc_extent 11
            if (spve.getDescription() != null && spve.getDescription().getExtent() != null)
                preparedStatement.setString(fieldNum, spve.getDescription().getExtent().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // desc_heading 12
            if (spve.getDescription() != null && spve.getDescription().getHeading() != null)
                preparedStatement.setString(fieldNum, spve.getDescription().getHeading().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // desc_priority 13
            if (spve.getDescription() != null)
                preparedStatement.setString(fieldNum, spve.getDescription().getPriority());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // desc_regional 14
            if (spve.getDescription() != null && spve.getDescription().getRegional() != null)
                preparedStatement.setString(fieldNum, spve.getDescription().getRegional().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // desc_typeevent 15
            if (spve.getDescription() != null && spve.getDescription().getTypeEvent() != null)
                preparedStatement.setString(fieldNum, spve.getDescription().getTypeEvent().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // tr_conn_pivotoffset 16
            if (spve.getTrailers() != null && spve.getTrailers().getConnection() != null
                    && spve.getTrailers().getConnection().getPivotOffset() != null)
                preparedStatement.setString(fieldNum, spve.getTrailers().getConnection().getPivotOffset().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // tr_conn_pivotangle 17
            if (spve.getTrailers() != null && spve.getTrailers().getConnection() != null
                    && spve.getTrailers().getConnection().getPivotAngle() != null)
                preparedStatement.setString(fieldNum, spve.getTrailers().getConnection().getPivotAngle().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // tr_conn_pivots 18
            if (spve.getTrailers() != null && spve.getTrailers().getConnection() != null
                    && spve.getTrailers().getConnection().getPivots() != null) {
                if (spve.getTrailers().getConnection().getPivots())
                    preparedStatement.setString(fieldNum, "1");
                else
                    preparedStatement.setString(fieldNum, "0");
            } else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // tr_ssprights 19
            if (spve.getTrailers() != null && spve.getTrailers().getSspRights() != null)
                preparedStatement.setString(fieldNum, spve.getTrailers().getSspRights().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // tr_units 20
            if (spve.getTrailers() != null && spve.getTrailers().getUnits() != null)
                preparedStatement.setString(fieldNum, spve.getTrailers().getUnits().toString());
            else
                preparedStatement.setString(fieldNum, null);
            fieldNum++;

            // execute insert statement
            Long bsmPart2SpveId = log(preparedStatement, "bsmPart2SpveId");
            return bsmPart2SpveId;

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