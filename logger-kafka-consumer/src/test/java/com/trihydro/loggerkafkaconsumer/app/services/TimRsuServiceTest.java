package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

public class TimRsuServiceTest extends TestBase<TimRsuService> {

    @Spy
    private TimDbTables mockTimDbTables = new TimDbTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
    }

    @Test
    public void AddTimRsu_SUCCESS() throws SQLException {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;
        int rsuIndex = 0;
        // Act
        Long data = uut.AddTimRsu(timId, rsuId, rsuIndex);

        // Assert
        Assertions.assertEquals(Long.valueOf(-1), data);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, timId);// TIM_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 2, rsuId);// RSU_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 3, rsuIndex);// RSU_INDEX
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddTimRsu_FAIL() throws SQLException {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;
        int rsuIndex = 0;
        doThrow(new SQLException("UNIQUENESS CONSTRAINT VIOLATION")).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, timId);

        // Act
        Long data = uut.AddTimRsu(timId, rsuId, rsuIndex);

        // Assert
        Assertions.assertEquals(Long.valueOf(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void recordExists_TRUE() throws SQLException {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;
        int rsuIndex = 0;
        lenient().doReturn(1L).when(mockDbInteractions).executeAndLog(isA(PreparedStatement.class), isA(String.class));

        // Act
        boolean dataExists = uut.recordExists(timId, rsuId, rsuIndex);

        // Assert
        Assertions.assertTrue(dataExists);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, timId);// TIM_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 2, rsuId);// RSU_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 3, rsuIndex);// RSU_INDEX
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void recordExists_FALSE() throws SQLException {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;
        int rsuIndex = 0;
        lenient().doReturn(0L).when(mockDbInteractions).executeAndLog(isA(PreparedStatement.class), isA(String.class));

        // Act
        boolean dataExists = uut.recordExists(timId, rsuId, rsuIndex);

        // Assert
        Assertions.assertFalse(dataExists);
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, timId);// TIM_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 2, rsuId);// RSU_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 3, rsuIndex);// RSU_INDEX
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void recordExists_ERROR() throws SQLException {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;
        int rsuIndex = 0;
        doThrow(new SQLException("UNIQUENESS CONSTRAINT VIOLATION")).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, timId);

        // Act
        boolean dataExists = uut.recordExists(timId, rsuId, rsuIndex);

        // Assert
        Assertions.assertFalse(dataExists);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}