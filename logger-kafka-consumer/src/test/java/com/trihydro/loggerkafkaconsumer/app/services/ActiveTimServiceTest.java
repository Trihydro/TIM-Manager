package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

public class ActiveTimServiceTest extends TestBase<ActiveTimService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    private Coordinate startPoint;
    private Coordinate endPoint;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
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
        doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
        doReturn(-1l).when(mockDbInteractions).executeAndLog(mockPreparedStatement, "active tim");
        var startTimeConverted = "03-Feb-20 04.22.23.000 PM";
        var endTimeConverted = "04-Feb-20 04.00.00.000 PM";

        var stTime = Instant.parse(activeTim.getStartDateTime());
        var endTime = Instant.parse(activeTim.getEndDateTime());
        java.util.Date stDate = java.util.Date.from(stTime);
        java.util.Date endDate = java.util.Date.from(endTime);
        doReturn(stDate).when(mockUtility).convertDate(activeTim.getStartDateTime());
        doReturn(endDate).when(mockUtility).convertDate(activeTim.getEndDateTime());
        mockUtility.timestampFormat = timestampFormat;

        // Act
        Long data = uut.insertActiveTim(activeTim);

        // Assert
        Assertions.assertEquals(Long.valueOf(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, activeTim.getTimId());// TIM_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, activeTim.getDirection());// DIRECTION

        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3, startTimeConverted);// TIM_START
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4, endTimeConverted);// TIM_END
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
        var startTimeConverted = "03-Feb-20 04.02.00.000 PM";
        var endTimeConverted = "03-Feb-20 04.00.00.000 PM";

        var stTime = Instant.parse(activeTim.getStartDateTime());
        var endTime = Instant.parse(activeTim.getEndDateTime());
        java.util.Date stDate = java.util.Date.from(stTime);
        java.util.Date endDate = java.util.Date.from(endTime);
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
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 6, startTimeConverted);
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 7, endTimeConverted);
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
        query += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
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
}