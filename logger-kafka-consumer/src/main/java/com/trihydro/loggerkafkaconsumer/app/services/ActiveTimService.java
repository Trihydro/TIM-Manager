package com.trihydro.loggerkafkaconsumer.app.services;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.tables.TimDbTables;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ActiveTimService extends BaseService {

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;
    private final Calendar UTCCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) { // TODO: use constructor instead of InjectDependencies
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long insertActiveTim(ActiveTim activeTim) {

        String insertQueryStatement = timDbTables.buildInsertQueryStatement("active_tim",
            timDbTables.getActiveTimTable());

        try (
            Connection connection = dbInteractions.getConnectionPool();
            PreparedStatement preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"active_tim_id"});
        ) {
            int fieldNum = 1;

            for (String col : timDbTables.getActiveTimTable()) {
                if (col.equals("TIM_ID")) {
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, activeTim.getTimId());
                } else if (col.equals("DIRECTION")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getDirection());
                } else if (col.equals("TIM_START")) {
                    log.info("Converting {} for TIM_START value", activeTim.getStartDateTime());
                    java.util.Date tim_start_date = utility.convertDate(activeTim.getStartDateTime());
                    Timestamp ts = new Timestamp(tim_start_date.getTime());
                    sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, ts);
                } else if (col.equals("TIM_END")) {
                    if (activeTim.getEndDateTime() != null) {
                        java.util.Date tim_end_date = utility.convertDate(activeTim.getEndDateTime());
                        Timestamp ts = new Timestamp(tim_end_date.getTime());
                        sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, ts);
                    } else {
                        preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
                    }
                } else if (col.equals("EXPIRATION_DATE")) {
                    if (activeTim.getExpirationDateTime() != null) {
                        java.util.Date tim_exp_date = utility.convertDate(activeTim.getExpirationDateTime());
                        Timestamp ts = new Timestamp(tim_exp_date.getTime());
                        sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, ts);
                    } else {
                        preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
                    }
                } else if (col.equals("TIM_TYPE_ID")) {
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, activeTim.getTimTypeId());
                } else if (col.equals("ROUTE")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getRoute());
                } else if (col.equals("CLIENT_ID")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getClientId());
                } else if (col.equals("SAT_RECORD_ID")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTim.getSatRecordId());
                } else if (col.equals("PK")) {
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, activeTim.getPk());
                } else if (col.equals("START_LATITUDE")) {
                    BigDecimal start_lat = null;
                    if (activeTim.getStartPoint() != null) {
                        start_lat = activeTim.getStartPoint().getLatitude();
                    }
                    sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, start_lat);
                } else if (col.equals("START_LONGITUDE")) {
                    BigDecimal start_lon = null;
                    if (activeTim.getStartPoint() != null) {
                        start_lon = activeTim.getStartPoint().getLongitude();
                    }
                    sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, start_lon);
                } else if (col.equals("END_LATITUDE")) {
                    BigDecimal end_lat = null;
                    if (activeTim.getEndPoint() != null) {
                        end_lat = activeTim.getEndPoint().getLatitude();
                    }
                    sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, end_lat);
                } else if (col.equals("END_LONGITUDE")) {
                    BigDecimal end_lon = null;
                    if (activeTim.getEndPoint() != null) {
                        end_lon = activeTim.getEndPoint().getLongitude();
                    }
                    sqlNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, end_lon);
                } else if (col.equals("PROJECT_KEY")) {
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, activeTim.getProjectKey());
                }

                fieldNum++;
            }

            return dbInteractions.executeAndLog(preparedStatement, "active tim");
        } catch (SQLException e) {
            log.error("Error inserting active_tim", e);
        }

        return 0L;
    }

    public boolean updateActiveTim(ActiveTim activeTim) {

        boolean activeTimIdResult = false;
        String updateTableSQL = "UPDATE ACTIVE_TIM SET TIM_ID = ?, START_LATITUDE = ?, START_LONGITUDE = ?, END_LATITUDE = ?,";
        updateTableSQL += "END_LONGITUDE = ?, TIM_START = ?, TIM_END = ?, PK = ?, PROJECT_KEY = ? WHERE ACTIVE_TIM_ID = ?";

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
        try (
            Connection connection = dbInteractions.getConnectionPool();
            PreparedStatement preparedStatement = connection.prepareStatement(updateTableSQL);
        ) {
            sqlNullHandler.setLongOrNull(preparedStatement, 1, activeTim.getTimId());
            sqlNullHandler.setBigDecimalOrNull(preparedStatement, 2, start_lat);
            sqlNullHandler.setBigDecimalOrNull(preparedStatement, 3, start_lon);
            sqlNullHandler.setBigDecimalOrNull(preparedStatement, 4, end_lat);
            sqlNullHandler.setBigDecimalOrNull(preparedStatement, 5, end_lon);

            java.util.Date tim_start_date = utility.convertDate(activeTim.getStartDateTime());
            Timestamp ts = new Timestamp(tim_start_date.getTime());
            sqlNullHandler.setTimestampOrNull(preparedStatement, 6, ts);

            if (activeTim.getEndDateTime() == null) {
                preparedStatement.setNull(7, java.sql.Types.TIMESTAMP);
            } else {
                java.util.Date tim_end_date = utility.convertDate(activeTim.getEndDateTime());
                Timestamp ts2 = new Timestamp(tim_end_date.getTime());
                sqlNullHandler.setTimestampOrNull(preparedStatement, 7, ts2);
            }

            sqlNullHandler.setIntegerOrNull(preparedStatement, 8, activeTim.getPk());
            sqlNullHandler.setIntegerOrNull(preparedStatement, 9, activeTim.getProjectKey());
            sqlNullHandler.setLongOrNull(preparedStatement, 10, activeTim.getActiveTimId());
            activeTimIdResult = dbInteractions.updateOrDelete(preparedStatement);
            log.info("------ Updated active_tim with id: {} --------------", activeTim.getActiveTimId());
        } catch (SQLException e) {
            log.error("Error updating active_tim with id: {}", activeTim.getActiveTimId(), e);
        }

        return activeTimIdResult;
    }

    public ActiveTim getActiveSatTim(String satRecordId, String direction) {

        ActiveTim activeTim = null;

        String query = "select * from active_tim";
        query += " where sat_record_id = '" + satRecordId + "' and active_tim.direction = '" + direction + "'";

        try (
            Connection connection = dbInteractions.getConnectionPool();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
        ) {
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
            log.error("Error getting active_sat_tim with satRecordId: {}, direction: {}", satRecordId, direction, e);
        }

        return activeTim;
    }

    public ActiveTim getActiveRsuTim(String clientId, String direction, String ipv4Address) {

        ActiveTim activeTim = null;

        String query = "select distinct atim.ACTIVE_TIM_ID, atim.TIM_ID, atim.SAT_RECORD_ID,";
        query += " atim.CLIENT_ID, atim.DIRECTION, atim.TIM_END, atim.TIM_START,";
        query += " atim.EXPIRATION_DATE, atim.ROUTE, atim.PK,";
        query += " atim.START_LATITUDE, atim.START_LONGITUDE, atim.END_LATITUDE, atim.END_LONGITUDE";
        query += " from active_tim atim";
        query += " inner join tim_rsu on atim.tim_id = tim_rsu.tim_id";
        query += " inner join rsu on tim_rsu.rsu_id = rsu.rsu_id";
        query += " inner join rsu_view on rsu.deviceid = rsu_view.deviceid";
        query += " where sat_record_id is null and ipv4_address = '" + ipv4Address + "' and client_id = '"
            + clientId + "' and atim.direction = '" + direction + "'";

        try (
            Connection connection = dbInteractions.getConnectionPool();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
        ) {
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
            log.error("Error getting active_rsu_tim with clientId: {}, direction: {}, ipv4Address: {}", clientId,
                direction, ipv4Address, e);
        }

        return activeTim;
    }

    public ActiveTim getActiveTimByPacketId(String packetID) {
        ActiveTim activeTim = null;

        String query = "SELECT ACTIVE_TIM.* FROM ACTIVE_TIM JOIN TIM ON ACTIVE_TIM.TIM_ID = TIM.TIM_ID "
            + "WHERE TIM.PACKET_ID = '" + packetID + "'";

        try (
            Connection connection = dbInteractions.getConnectionPool();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
        ) {
            // convert to ActiveTim object
            while (rs.next()) {
                activeTim = new ActiveTim();
                activeTim.setActiveTimId(rs.getLong("ACTIVE_TIM_ID"));
                activeTim.setTimId(rs.getLong("TIM_ID"));
                activeTim.setSatRecordId(rs.getString("SAT_RECORD_ID"));
                activeTim.setClientId(rs.getString("CLIENT_ID"));
                activeTim.setDirection(rs.getString("DIRECTION"));
                activeTim.setEndDateTime(rs.getString("TIM_END"));
                activeTim.setStartTimestamp(rs.getTimestamp("TIM_START", UTCCalendar));
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
            log.error("Error getting active_tim by packetId: {}", packetID, e);
        }

        return activeTim;
    }

    public boolean updateActiveTimExpiration(String packetID, String expDate) {
        boolean success;

        String query = "SELECT ACTIVE_TIM_ID FROM ACTIVE_TIM atim";
        query += " INNER JOIN TIM ON atim.TIM_ID = TIM.TIM_ID";
        query += " WHERE TIM.PACKET_ID = ?";

        String updateStatement = "UPDATE ACTIVE_TIM SET EXPIRATION_DATE = ? WHERE ACTIVE_TIM_ID IN (";
        updateStatement += query;
        updateStatement += ")";

        try (
            Connection connection = dbInteractions.getConnectionPool();
            PreparedStatement preparedStatement = connection.prepareStatement(updateStatement);
        ) {
            DateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
            Date dte = sdf.parse(expDate);
            Timestamp ts = new Timestamp(dte.getTime());
            preparedStatement.setTimestamp(1, ts);// expDate comes in as MST from previously called function
            // (GetMinExpiration)
            preparedStatement.setString(2, packetID);

            // execute update statement
            success = dbInteractions.updateOrDelete(preparedStatement);
        } catch (Exception e) {
            log.error("Error updating active_tim expiration date with packetID: {}, expDate: {}", packetID, expDate, e);
            return false;
        }
        log.info("Called UpdateExpiration with packetID: {}, expDate: {}. Successful: {}", packetID, expDate, success);
        return success;
    }

    public String getMinExpiration(String packetID, String expDate) throws ParseException {
        String targetFormat = "DD-MON-YYYY HH12.MI.SS a";
        String selectTimestamp = String.format("SELECT TO_TIMESTAMP('%s', '%s')",
            translateIso8601ToTimestampFormat(expDate), targetFormat);

        String minExpDate = "SELECT MIN(EXPIRATION_DATE) FROM ACTIVE_TIM atim";
        minExpDate += " INNER JOIN TIM ON atim.TIM_ID = TIM.TIM_ID";
        minExpDate += " WHERE TIM.PACKET_ID = '" + packetID + "'";

        String query = String.format("SELECT LEAST((%s), (COALESCE((%s),(%s)))) minStart",
            selectTimestamp, minExpDate, selectTimestamp);

        String minStart = "";

        try (
            Connection connection = dbInteractions.getConnectionPool();
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(query);
        ) {
            // Fetch the minimum of passed in expDate and database held
            // active_tim.expiration_date. To compare like values we convert the expDate
            // TO_TIMESTAMP. Without this it compares string length.
            // Also, there are some null values in the db. To get around these, we use the
            // coalesce function with the expDate passed in value.
            while (rs.next()) {
                var tmpTs = rs.getTimestamp("MINSTART", UTCCalendar);
                minStart = utility.timestampFormat.format(tmpTs);
            }
        } catch (SQLException e) {
            log.error("Error getting min expiration date with packetID: {}, expDate: {}", packetID, expDate, e);
            return null;
        }
        log.info("Called GetMinExpiration with packetID: {}, expDate: {}. Min start date: {}", packetID, expDate,
            minStart);
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