package com.trihydro.loggerkafkaconsumer.app.services;

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class TimRsuServiceTest {

    @Mock
    private DbInteractions mockDbInteractions;
    @Mock
    private TimDbTables mockTimDbTables;
    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockPreparedStatement;

    private TimRsuService uut;

    @BeforeEach
    public void setup() throws Exception {
        // Initialize mocks
        mockDbInteractions = mock(DbInteractions.class);
        mockTimDbTables = mock(TimDbTables.class);
        mockSqlNullHandler = mock(SQLNullHandler.class);
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);

        // Stub common behaviors
        when(mockDbInteractions.getConnectionPool()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString(), any(String[].class))).thenReturn(mockPreparedStatement);
        when(mockTimDbTables.buildInsertQueryStatement(anyString(), anyList())).thenReturn("INSERT INTO tim_rsu ...");
        when(mockTimDbTables.getTimRsuTable()).thenReturn(List.of("TIM_ID", "RSU_ID", "RSU_INDEX"));

        // Instantiate the service with the mocked dependencies
        uut = new TimRsuService(mockDbInteractions, mockTimDbTables, mockSqlNullHandler);
    }

    @Test
    public void AddTimRsu_SUCCESS() throws SQLException {
        // Arrange
        Long timId = -1L;
        int rsuId = -2;
        int rsuIndex = 0;

        when(mockDbInteractions.executeAndLog(any(PreparedStatement.class), anyString())).thenReturn(-1L);

        // Act
        Long data = uut.AddTimRsu(timId, rsuId, rsuIndex);

        // Assert
        assertEquals(Long.valueOf(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, timId); // TIM_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 2, rsuId); // RSU_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 3, rsuIndex); // RSU_INDEX
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddTimRsu_FAIL_UniqueConstraint() throws SQLException {
        // Arrange
        Long timId = -1L;
        int rsuId = -2;
        int rsuIndex = 0;

        doThrow(new SQLException("unique constraint")).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, timId);

        // Act
        Long data = uut.AddTimRsu(timId, rsuId, rsuIndex);

        // Assert
        assertEquals(Long.valueOf(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddTimRsu_FAIL_OtherSQLException() throws SQLException {
        // Arrange
        Long timId = -1L;
        int rsuId = -2;
        int rsuIndex = 0;

        doThrow(new SQLException("other sql exception")).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, timId);

        // Act
        Long data = uut.AddTimRsu(timId, rsuId, rsuIndex);

        // Assert
        assertEquals(Long.valueOf(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}