package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

public class PathServiceTest extends TestBase<PathService> {

    @Spy
    private TimDbTables mockTimDbTables = new TimDbTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
    }

    @Test
    public void InsertPath_SUCCESS() throws SQLException {
        // Arrange

        // Act
        Long data = uut.InsertPath();

        // Assert
        Assertions.assertEquals(Long.valueOf(-1), data);
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 1, 0);// SCALE
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void InsertPath_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 1, 0);
        // Act
        Long data = uut.InsertPath();

        // Assert
        Assertions.assertEquals(Long.valueOf(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

}