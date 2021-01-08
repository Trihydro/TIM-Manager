package com.trihydro.loggerkafkaconsumer.app.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
    private Calendar UTCCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

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
                else if (col.equals("TIM_START")) {
                    utility.logWithDate(
                            String.format("Converting %s for TIM_START value", activeTim.getStartDateTime()));
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

            java.util.Date tim_start_date = utility.convertDate(activeTim.getStartDateTime());
            sqlNullHandler.setStringOrNull(preparedStatement, 6, utility.timestampFormat.format(tim_start_date));

            if (activeTim.getEndDateTime() == null)
                preparedStatement.setString(7, null);
            else {
                java.util.Date tim_end_date = utility.convertDate(activeTim.getEndDateTime());
                sqlNullHandler.setStringOrNull(preparedStatement, 7, utility.timestampFormat.format(tim_end_date));
            }

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
            //
            String query = "select distinct atim.ACTIVE_TIM_ID, atim.TIM_ID, atim.SAT_RECORD_ID,";
            query += " atim.CLIENT_ID, atim.DIRECTION, atim.TIM_END, atim.TIM_START,";
            query += " atim.EXPIRATION_DATE, atim.ROUTE, atim.PK,";
            query += " atim.START_LATITUDE, atim.START_LONGITUDE, atim.END_LATITUDE, atim.END_LONGITUDE";
            query += " from active_tim atim";
            query += " inner join tim_rsu on atim.tim_id = tim_rsu.tim_id";
            query += " inner join rsu on tim_rsu.rsu_id = rsu.rsu_id";
            query += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
            query += " where sat_record_id is null and ipv4_address = '" + ipv4Address + "' and client_id = '"
                    + clientId + "' and atim.direction = '" + direction + "'";

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

    public ActiveTim getActiveTimByPacketId(String packetID) {
        ActiveTim activeTim = null;
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {
            connection = dbInteractions.getConnectionPool();
            statement = connection.createStatement();
            String query = "SELECT ACTIVE_TIM.* FROM ACTIVE_TIM JOIN TIM ON ACTIVE_TIM.TIM_ID = TIM.TIM_ID "
                    + "WHERE TIM.PACKET_ID = '" + packetID + "';";

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

    public boolean updateActiveTimExpiration(String packetID, String startDate, String expDate) {
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
            return false;
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
        return success;
    }

    public String getMinExpiration(String packetID, String startDate, String expDate) throws ParseException {
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
            return null;
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
        return minStart;
    }

    private String translateIso8601ToTimestampFormat(String date) throws ParseException {
        DateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
        // TimeZone toTimeZone = TimeZone.getTimeZone("MST");
        // sdf.setTimeZone(toTimeZone);
        DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date dte = m_ISO8601Local.parse(date);
        return sdf.format(dte.getTime());
    }
}