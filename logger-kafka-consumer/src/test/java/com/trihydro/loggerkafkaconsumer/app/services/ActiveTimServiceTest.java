package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.tables.TimDbTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

public class ActiveTimServiceTest extends TestBase<ActiveTimService> {

    @Spy
    private TimDbTables mockTimDbTables = new TimDbTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    private Coordinate startPoint;
    private Coordinate endPoint;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
        startPoint = new Coordinate(BigDecimal.valueOf(-1), BigDecimal.valueOf(-2));
        endPoint = new Coordinate(BigDecimal.valueOf(-3), BigDecimal.valueOf(-4));
    }

    @Test
    public void insertActiveTim_SUCCESS() throws SQLException {
        // Arrange
        ActiveTim activeTim = new ActiveTim();
        activeTim.setStartPoint(startPoint);
        activeTim.setEndPoint(endPoint);
        activeTim.setStartDateTime("2020-02-03T16:22:23.000Z");
        activeTim.setEndDateTime("2020-02-04T16:00:00.000Z");
        doReturn("").when(mockTimDbTables).buildInsertQueryStatement(any(), any());
        doReturn(-1l).when(mockDbInteractions).executeAndLog(mockPreparedStatement, "active tim");

        var stTime = Instant.parse(activeTim.getStartDateTime());
        var endTime = Instant.parse(activeTim.getEndDateTime());
        java.util.Date stDate = java.util.Date.from(stTime);
        java.util.Date endDate = java.util.Date.from(endTime);
        Timestamp startDateTimestamp = Timestamp.from(stTime);
        Timestamp endDateTimestamp = Timestamp.from(endTime);
        doReturn(stDate).when(mockUtility).convertDate(activeTim.getStartDateTime());
        doReturn(endDate).when(mockUtility).convertDate(activeTim.getEndDateTime());
        mockUtility.timestampFormat = timestampFormat;

        // Act
        Long data = uut.insertActiveTim(activeTim);

        // Assert
        Assertions.assertEquals(Long.valueOf(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, activeTim.getTimId());// TIM_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, null);// DIRECTION

        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 3, startDateTimestamp);// TIM_START
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 4, endDateTimestamp);// TIM_END
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 5, activeTim.getTimTypeId());// TIM_TYPE_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 6, activeTim.getRoute());// ROUTE
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 7, activeTim.getClientId());// CLIENT_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 8, activeTim.getSatRecordId());// SAT_RECORD_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 9, activeTim.getPk());// PK
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 10,
                activeTim.getStartPoint().getLatitude());// START_LATITUDE
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 11,
                activeTim.getStartPoint().getLongitude());// START_LONGITUDE
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 12,
                activeTim.getEndPoint().getLatitude());// END_LATITUDE
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 13,
                activeTim.getEndPoint().getLongitude());// END_LONGITUDE
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 15, null); // PROJECT_KEY
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void insertActiveTim_FAIL() throws SQLException {
        // Arrange
        ActiveTim activeTim = new ActiveTim();
        activeTim.setStartDateTime("2020-02-03T16:00:00.000Z");
        activeTim.setEndDateTime("2020-02-03T16:00:00.000Z");
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, (Long) null);

        // Act
        Long data = uut.insertActiveTim(activeTim);

        // Assert
        Assertions.assertEquals(Long.valueOf(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void updateActiveTim_SUCCESS() throws SQLException {
        // Arrange
        doReturn(true).when(mockDbInteractions).updateOrDelete(mockPreparedStatement);
        ActiveTim activeTim = new ActiveTim();
        activeTim.setStartPoint(startPoint);
        activeTim.setEndPoint(endPoint);
        activeTim.setActiveTimId(-1l);
        activeTim.setStartDateTime("2020-02-03T16:02:00.000Z");
        activeTim.setEndDateTime("2020-02-03T16:00:00.000Z");

        var stTime = Instant.parse(activeTim.getStartDateTime());
        var endTime = Instant.parse(activeTim.getEndDateTime());
        java.util.Date stDate = java.util.Date.from(stTime);
        java.util.Date endDate = java.util.Date.from(endTime);
        Timestamp startDateTimestamp = Timestamp.from(stTime);
        Timestamp endDateTimestamp = Timestamp.from(endTime);
        doReturn(stDate).when(mockUtility).convertDate(activeTim.getStartDateTime());
        doReturn(endDate).when(mockUtility).convertDate(activeTim.getEndDateTime());
        mockUtility.timestampFormat = timestampFormat;

        // Act
        boolean data = uut.updateActiveTim(activeTim);

        // Assert
        Assertions.assertTrue(data, "Failed to update activeTim");
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, activeTim.getTimId());
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 2,
                activeTim.getStartPoint().getLatitude());
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 3,
                activeTim.getStartPoint().getLongitude());
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 4, activeTim.getEndPoint().getLatitude());
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 5,
                activeTim.getEndPoint().getLongitude());
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 6, startDateTimestamp);
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 7, endDateTimestamp);
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 8, activeTim.getPk());
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 9, null);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 10, activeTim.getActiveTimId());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void updateActiveTim_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, (Long) null);
        ActiveTim activeTim = new ActiveTim();
        activeTim.setActiveTimId(-1l);
        activeTim.setStartDateTime("2020-02-03T16:00:00.000Z");
        activeTim.setEndDateTime("2020-02-03T16:00:00.000Z");

        // Act
        boolean data = uut.updateActiveTim(activeTim);

        // Assert
        Assertions.assertFalse(data, "Success reported on failed update activeTim");
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void getActiveSatTim_SUCCESS() throws SQLException {
        // Arrange
        when(mockRs.getLong("ACTIVE_TIM_ID")).thenReturn(99l);
        when(mockRs.getLong("TIM_ID")).thenReturn(-99l);
        String query = "select * from active_tim";
        query += " where sat_record_id = 'satRecordId' and active_tim.direction = 'direction'";

        // Act
        ActiveTim data = uut.getActiveSatTim("satRecordId", "direction");

        // Assert
        Assertions.assertNotNull(data, "Null ActiveTim returned");
        Assertions.assertEquals(Long.valueOf(99), data.getActiveTimId());
        Assertions.assertEquals(Long.valueOf(-99), data.getTimId());
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getActiveSatTim_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getLong("ACTIVE_TIM_ID");

        // Act
        ActiveTim data = uut.getActiveSatTim("satRecordId", "direction");

        // Assert
        Assertions.assertNull(data.getActiveTimId(), "ActiveTimID returned when expected null");
        Assertions.assertNull(data.getTimId(), "TimID returned when expected null");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getActiveRsuTim_SUCCESS() throws SQLException {
        // Arrange
        when(mockRs.getLong("ACTIVE_TIM_ID")).thenReturn(99l);
        when(mockRs.getLong("TIM_ID")).thenReturn(-99l);
        String query = "select distinct atim.ACTIVE_TIM_ID, atim.TIM_ID, atim.SAT_RECORD_ID,";
        query += " atim.CLIENT_ID, atim.DIRECTION, atim.TIM_END, atim.TIM_START,";
        query += " atim.EXPIRATION_DATE, atim.ROUTE, atim.PK,";
        query += " atim.START_LATITUDE, atim.START_LONGITUDE, atim.END_LATITUDE, atim.END_LONGITUDE";
        query += " from active_tim atim";
        query += " inner join tim_rsu on atim.tim_id = tim_rsu.tim_id";
        query += " inner join rsu on tim_rsu.rsu_id = rsu.rsu_id";
        query += " inner join its.rsu_vw on rsu.deviceid = its.rsu_vw.deviceid";
        query += " where sat_record_id is null";
        query += " and ipv4_address = 'ipv4Address' and client_id = 'clientId'";
        query += " and atim.direction = 'direction'";

        // Act
        ActiveTim data = uut.getActiveRsuTim("clientId", "direction", "ipv4Address");

        // Assert
        Assertions.assertNotNull(data, "Null ActiveTim returned");
        Assertions.assertEquals(Long.valueOf(99), data.getActiveTimId());
        Assertions.assertEquals(Long.valueOf(-99), data.getTimId());
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getActiveRsuTim_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getLong("ACTIVE_TIM_ID");

        // Act
        ActiveTim data = uut.getActiveRsuTim("clientId", "direction", "ipv4Address");

        // Assert
        Assertions.assertNull(data.getActiveTimId(), "ActiveTimID returned when expected null");
        Assertions.assertNull(data.getTimId(), "TimID returned when expected null");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getMinExpiration_SUCCESS() throws Exception {
        // Arrange
        Timestamp dbValue = Timestamp.valueOf("2021-01-01 00:00:00");
        String expDate = "2021-01-03T00:00:00.000Z"; // Later than dbValue
        when(mockRs.getTimestamp(eq("MINSTART"), any())).thenReturn(dbValue);
        mockUtility.timestampFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");

        String query = "SELECT LEAST((SELECT TO_TIMESTAMP('03-Jan-21 12.00.00.000 AM', 'DD-MON-YYYY HH12.MI.SS.SSS a')), "
                + "(COALESCE((SELECT MIN(EXPIRATION_DATE) FROM ACTIVE_TIM atim INNER JOIN TIM ON atim.TIM_ID = TIM.TIM_ID "
                + "WHERE TIM.PACKET_ID = '0000'),"
                + "(SELECT TO_TIMESTAMP('03-Jan-21 12.00.00.000 AM', 'DD-MON-YYYY HH12.MI.SS.SSS a'))))) minStart";

        // Act
        String minExp = uut.getMinExpiration("0000", expDate);

        // Assert
        verify(mockStatement).executeQuery(query);
        Assertions.assertEquals("01-Jan-21 12.00.00.000 AM", minExp);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getMinExpiration_FAIL() throws Exception {
        // Arrange
        String expDate = "2021-01-03T00:00:00.000Z";
        doThrow(new SQLException()).when(mockRs).getTimestamp(eq("MINSTART"), any());

        // Act
        String minExp = uut.getMinExpiration("0000", expDate);

        // Assert
        Assertions.assertNull(minExp);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void updateActiveTimExpiration_SUCCESS() throws SQLException {
        // Arrange
        doReturn(true).when(mockDbInteractions).updateOrDelete(mockPreparedStatement);
        String expDate = "31-Dec-19 11.59.56.000 PM";
        String query = "UPDATE ACTIVE_TIM SET EXPIRATION_DATE = ? "
                + "WHERE ACTIVE_TIM_ID IN (SELECT ACTIVE_TIM_ID FROM ACTIVE_TIM atim "
                + "INNER JOIN TIM ON atim.TIM_ID = TIM.TIM_ID WHERE TIM.PACKET_ID = ?)";
        // Act
        var result = uut.updateActiveTimExpiration("0000", expDate);

        // Assert
        Assertions.assertTrue(result);
        verify(mockConnection).prepareStatement(query);
        verify(mockPreparedStatement).setTimestamp(1, Timestamp.valueOf("2019-12-31 23:59:56"));
        verify(mockPreparedStatement).setString(2, "0000");
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void updateActiveTimExpiration_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockConnection).prepareStatement(any());
        String expDate = "2021-01-03T00:00:00.000Z";

        // Act
        var result = uut.updateActiveTimExpiration("0000", expDate);

        // Assert
        Assertions.assertFalse(result);
        verify(mockConnection).close();
    }
}