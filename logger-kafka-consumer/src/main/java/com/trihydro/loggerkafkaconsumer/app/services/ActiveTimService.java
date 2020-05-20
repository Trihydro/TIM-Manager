package com.trihydro.loggerkafkaconsumer.app.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.tables.TimOracleTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ActiveTimService extends BaseService {

    private TimOracleTables timOracleTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimOracleTables _timOracleTables, SQLNullHandler _sqlNullHandler) {
        timOracleTables = _timOracleTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long insertActiveTim(ActiveTim activeTim) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

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
                else if (col.equals("TIM_START"))
                    sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(
                            LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
                else if (col.equals("TIM_END"))
                    if (activeTim.getEndDateTime() != null)
                        sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(
                                LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
                    else
                        preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
                else if (col.equals("EXPIRATION_DATE")) {
                    if (activeTim.getExpirationDateTime() != null) {
                        sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum,
                                java.sql.Timestamp.valueOf(LocalDateTime.parse(activeTim.getExpirationDateTime(),
                                        DateTimeFormatter.ISO_DATE_TIME)));
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
                } else if (col.equals("PROJECT_KEY")) {
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, activeTim.getProjectKey());
                }

                fieldNum++;
            }

            Long activeTimId = dbInteractions.executeAndLog(preparedStatement, "active tim");
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

        return Long.valueOf(0);
    }

    public boolean updateActiveTim(ActiveTim activeTim) {

        boolean activeTimIdResult = false;
        String updateTableSQL = "UPDATE ACTIVE_TIM SET TIM_ID = ?, START_LATITUDE = ?, START_LONGITUDE = ?, END_LATITUDE = ?,";
        updateTableSQL += "END_LONGITUDE = ?, TIM_START = ?, TIM_END = ?, PK = ?, PROJECT_KEY = ? WHERE ACTIVE_TIM_ID = ?";
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        BigDecimal start_lat = null;
        BigDecimal start_lon = null;
        BigDecimal end_lat = null;
        BigDecimal end_lon = null;
        if (activeTim.getStartPoint() != null) {
            start_lat = activeTim.getStartPoint().getLatitude();
            start_lon = activeTim.getStartPoint().getLongitude();
        }
        if (activeTim.getEndPoint() != null) {
            end_lat = activeTim.getEndPoint().getLatitude();
            end_lon = activeTim.getEndPoint().getLongitude();
        }
        try {
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(updateTableSQL);
            sqlNullHandler.setLongOrNull(preparedStatement, 1, activeTim.getTimId());
            sqlNullHandler.setBigDecimalOrNull(preparedStatement, 2, start_lat);
            sqlNullHandler.setBigDecimalOrNull(preparedStatement, 3, start_lon);
            sqlNullHandler.setBigDecimalOrNull(preparedStatement, 4, end_lat);
            sqlNullHandler.setBigDecimalOrNull(preparedStatement, 5, end_lon);
            sqlNullHandler.setTimestampOrNull(preparedStatement, 6, java.sql.Timestamp
                    .valueOf(LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));

            if (activeTim.getEndDateTime() == null)
                preparedStatement.setString(7, null);
            else
                sqlNullHandler.setTimestampOrNull(preparedStatement, 7, java.sql.Timestamp
                        .valueOf(LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));

            sqlNullHandler.setIntegerOrNull(preparedStatement, 8, activeTim.getPk());
            sqlNullHandler.setIntegerOrNull(preparedStatement, 9, activeTim.getProjectKey());
            sqlNullHandler.setLongOrNull(preparedStatement, 10, activeTim.getActiveTimId());
            activeTimIdResult = dbInteractions.updateOrDelete(preparedStatement);
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

    public ActiveTim getActiveSatTim(String satRecordId, String direction) {

        ActiveTim activeTim = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            connection = dbInteractions.getConnectionPool();
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
                activeTim.setExpirationDateTime(rs.getString("EXPIRATION_DATE"));
                activeTim.setRoute(rs.getString("ROUTE"));
                activeTim.setPk(rs.getInt("PK"));

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

    public ActiveTim getActiveRsuTim(String clientId, String direction, String ipv4Address) {

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
            query += " where sat_record_id is null and ipv4_address = '" + ipv4Address + "' and client_id = '"
                    + clientId + "' and active_tim.direction = '" + direction + "'";

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
                activeTim.setExpirationDateTime(rs.getString("EXPIRATION_DATE"));
                activeTim.setRoute(rs.getString("ROUTE"));
                activeTim.setPk(rs.getInt("PK"));

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
}