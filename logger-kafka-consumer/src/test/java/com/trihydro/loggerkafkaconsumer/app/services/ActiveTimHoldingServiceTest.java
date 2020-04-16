package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import com.trihydro.library.model.ActiveTimHolding;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;

@RunWith(StrictStubs.class)
public class ActiveTimHoldingServiceTest extends TestBase<ActiveTimHoldingService> {
    @Test
    public void getActiveTimHolding_SUCCESS() throws SQLException {
        // Arrange
        Long athId = 99l;
        when(mockRs.getLong("ACTIVE_TIM_HOLDING_ID")).thenReturn(athId);
        String query = "select * from active_tim_holding";
        query += " where rsu_target = 'ipv4Address' and client_id = 'clientId' and direction = 'direction'";

        // Act
        ActiveTimHolding data = uut.getRsuActiveTimHolding("clientId", "direction", "ipv4Address");

        // Assert
        assertNotNull("Null ActiveTimHolding returned", data);
        assertEquals(athId, data.getActiveTimHoldingId());
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getActiveTimHolding_FAIL() throws SQLException {
        // Arrange
        String query = "select * from active_tim_holding";
        query += " where rsu_target = 'ipv4Address' and client_id = 'clientId' and direction = 'direction'";
        doThrow(new SQLException()).when(mockRs).getLong("ACTIVE_TIM_HOLDING_ID");

        // Act
        ActiveTimHolding data = uut.getRsuActiveTimHolding("clientId", "direction", "ipv4Address");

        // Assert
        assertNull(data.getActiveTimHoldingId(), "getActiveTimHolding returned object when should have returned empty");
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getSdxActiveTimHolding_SUCCESS() throws SQLException {
        // Arrange
        Long athId = 99l;
        when(mockRs.getLong("ACTIVE_TIM_HOLDING_ID")).thenReturn(athId);
        String query = "select * from active_tim_holding";
        query += " where sat_record_id = 'satRecordId' and client_id = 'clientId' and direction = 'direction'";

        // Act
        ActiveTimHolding data = uut.getSdxActiveTimHolding("clientId", "direction", "satRecordId");

        // Assert
        assertNotNull("Null ActiveTimHolding returned", data);
        assertEquals(athId, data.getActiveTimHoldingId());
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getSdxActiveTimHolding_FAIL() throws SQLException {
        // Arrange
        String query = "select * from active_tim_holding";
        query += " where sat_record_id = 'satRecordId' and client_id = 'clientId' and direction = 'direction'";
        doThrow(new SQLException()).when(mockRs).getLong("ACTIVE_TIM_HOLDING_ID");

        // Act
        ActiveTimHolding data = uut.getSdxActiveTimHolding("clientId", "direction", "satRecordId");

        // Assert
        assertNull(data.getActiveTimHoldingId(), "getActiveTimHolding returned object when should have returned empty");
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void deleteActiveTimHolding_SUCCESS() throws SQLException {
        // Arrange

        // Act
        Boolean data = uut.deleteActiveTimHolding(-1l);

        // Assert
        assertTrue(data);
        verify(mockConnection).prepareStatement("DELETE ACTIVE_TIM_HOLDING WHERE ACTIVE_TIM_HOLDING_ID = ?");
        verify(mockPreparedStatement).setLong(1, -1l);
        verify(mockConnection).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    public void deleteActiveTimHolding_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        // Act
        Boolean data = uut.deleteActiveTimHolding(-1l);

        // Assert
        assertFalse(data);
        verify(mockConnection).close();
    }
}