package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PathControllerTest extends TestBase<PathController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimDbTables mockTimDbTables;

    @BeforeEach
    public void setupSubTest() {
        doReturn("").when(mockTimDbTables).buildInsertQueryStatement(any(), any());
        uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
    }

    @Test
    public void InsertPath_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<Long> data = uut.InsertPath();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();

    }

    @Test
    public void InsertPath_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 1, 0);

        // Act
        ResponseEntity<Long> data = uut.InsertPath();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}