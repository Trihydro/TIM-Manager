package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ActiveTimServiceTest extends TestBase<ActiveTimService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void insertActiveTim_SUCCESS() throws SQLException {
        // Arrange
        ActiveTim activeTim = new ActiveTim();
        activeTim.setStartPoint(new Coordinate(-1, -2));
        activeTim.setEndPoint(new Coordinate(-3, -4));
        activeTim.setStartDateTime("2020-02-03T16:00:00.000Z");
        activeTim.setEndDateTime("2020-02-03T16:00:00.000Z");
        doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
        doReturn(-1l).when(uut).executeAndLog(mockPreparedStatement, "active tim");

        // Act
        Long data = uut.insertActiveTim(activeTim);

        // Assert
        assertEquals(new Long(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, activeTim.getTimId());// TIM_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4, activeTim.getDirection());// DIRECTION
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 5, java.sql.Timestamp// TIM_START
                .valueOf(LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 6, java.sql.Timestamp// TIM_END
                .valueOf(LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 7, activeTim.getTimTypeId());// TIM_TYPE_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 8, activeTim.getRoute());// ROUTE
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 9, activeTim.getClientId());// CLIENT_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 10, activeTim.getSatRecordId());// SAT_RECORD_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 11, activeTim.getPk());// PK
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 12, activeTim.getStartPoint().getLatitude());// START_LATITUDE
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 13, activeTim.getStartPoint().getLongitude());// START_LONGITUDE
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 14, activeTim.getEndPoint().getLatitude());// END_LATITUDE
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 15, activeTim.getEndPoint().getLongitude());// END_LONGITUDE
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 12, activeTim.getStartPoint().getLatitude());// START_LATITUDE
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 13, activeTim.getStartPoint().getLongitude());// START_LONGITUDE
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 14, activeTim.getEndPoint().getLatitude());// END_LATITUDE
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 15, activeTim.getEndPoint().getLongitude());// END_LONGITUDE
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
        assertEquals(new Long(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void updateActiveTim_SUCCESS() throws SQLException {
        // Arrange
        doReturn(true).when(uut).updateOrDelete(mockPreparedStatement);
        ActiveTim activeTim = new ActiveTim();
        Coordinate start = new Coordinate(-1, -2);
        Coordinate end = new Coordinate(-3, -4);
        activeTim.setStartPoint(start);
        activeTim.setEndPoint(end);
        activeTim.setActiveTimId(-1l);
        activeTim.setStartDateTime("2020-02-03T16:00:00.000Z");
        activeTim.setEndDateTime("2020-02-03T16:00:00.000Z");

        // Act
        boolean data = uut.updateActiveTim(activeTim);

        // Assert
        assertTrue("Failed to update activeTim", data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, activeTim.getTimId());
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 2, activeTim.getStartPoint().getLatitude());
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 3, activeTim.getStartPoint().getLongitude());
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 4, activeTim.getEndPoint().getLatitude());
        verify(mockSqlNullHandler).setDoubleOrNull(mockPreparedStatement, 5, activeTim.getEndPoint().getLongitude());
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 6, java.sql.Timestamp
                .valueOf(LocalDateTime.parse(activeTim.getStartDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 7, java.sql.Timestamp
                .valueOf(LocalDateTime.parse(activeTim.getEndDateTime(), DateTimeFormatter.ISO_DATE_TIME)));
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 8, activeTim.getPk());
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 9, activeTim.getActiveTimId());
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
        assertFalse("Success reported on failed update activeTim", data);
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
        assertNotNull("Null ActiveTim returned", data);
        assertEquals(new Long(99), data.getActiveTimId());
        assertEquals(new Long(-99), data.getTimId());
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
        assertNull("ActiveTimID returned when expected null", data.getActiveTimId());
        assertNull("TimID returned when expected null", data.getTimId());
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getActiveRsuTim_SUCCESS() throws SQLException {
        // Arrange
        when(mockRs.getLong("ACTIVE_TIM_ID")).thenReturn(99l);
        when(mockRs.getLong("TIM_ID")).thenReturn(-99l);
        String query = "select * from active_tim";
        query += " inner join tim_rsu on active_tim.tim_id = tim_rsu.tim_id";
        query += " inner join rsu on tim_rsu.rsu_id = rsu.rsu_id";
        query += " inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid";
        query += " where sat_record_id is null";
        query += " and ipv4_address = 'ipv4Address' and client_id = 'clientId'";
        query += " and active_tim.direction = 'direction'";

        // Act
        ActiveTim data = uut.getActiveRsuTim("clientId", "direction", "ipv4Address");

        // Assert
        assertNotNull("Null ActiveTim returned", data);
        assertEquals(new Long(99), data.getActiveTimId());
        assertEquals(new Long(-99), data.getTimId());
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
        assertNull("ActiveTimID returned when expected null", data.getActiveTimId());
        assertNull("TimID returned when expected null", data.getTimId());
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }
}