package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import com.trihydro.library.model.ActiveTimHolding;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertNotNull(data, "Null ActiveTimHolding returned");
        Assertions.assertEquals(athId, data.getActiveTimHoldingId());
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
        Assertions.assertNull(data, "getActiveTimHolding returned object when should have returned empty");
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
        Assertions.assertNotNull(data, "Null ActiveTimHolding returned");
        Assertions.assertEquals(athId, data.getActiveTimHoldingId());
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
        Assertions.assertNull(data, "getActiveTimHolding returned object when should have returned empty");
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getActiveTimHoldingByPacketId_SUCCESS() throws SQLException {
        // Arrange
        Long athId = 99l;
        when(mockRs.getLong("ACTIVE_TIM_HOLDING_ID")).thenReturn(athId);
        String query = "select * from active_tim_holding";
        query += " where packet_id = 'packetId'";

        // Act
        ActiveTimHolding data = uut.getActiveTimHoldingByPacketId("packetId");

        // Assert
        Assertions.assertNotNull(data, "Null ActiveTimHolding returned");
        Assertions.assertEquals(athId, data.getActiveTimHoldingId());
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getActiveTimHoldingByPacketId_FAIL() throws SQLException {
        // Arrange
        String query = "select * from active_tim_holding";
        query += " where packet_id = 'packetId'";
        doThrow(new SQLException()).when(mockRs).getLong("ACTIVE_TIM_HOLDING_ID");

        // Act
        ActiveTimHolding data = uut.getActiveTimHoldingByPacketId("packetId");

        // Assert
        Assertions.assertNull(data, "getActiveTimHoldingByPacketId returned object when should have returned empty");
        verify(mockStatement).executeQuery(query);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void deleteActiveTimHolding_SUCCESS() throws SQLException {
        // Arrange

        // Act
        Boolean data = uut.deleteActiveTimHolding(1l);

        // Assert
        Assertions.assertTrue(data);
        verify(mockConnection).prepareStatement("DELETE FROM ACTIVE_TIM_HOLDING WHERE ACTIVE_TIM_HOLDING_ID = ?");
        verify(mockPreparedStatement).setLong(1, 1l);
        verify(mockConnection).close();
        verify(mockPreparedStatement).close();
    }

    @Test
    public void deleteActiveTimHolding_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockConnection).prepareStatement(anyString());
        // Act
        Boolean data = uut.deleteActiveTimHolding(1l);

        // Assert
        Assertions.assertFalse(data);
        verify(mockConnection).close();
    }
}