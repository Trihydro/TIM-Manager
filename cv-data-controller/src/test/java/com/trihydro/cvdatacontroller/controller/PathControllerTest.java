package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(StrictStubs.class)
public class PathControllerTest extends TestBase<PathController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimOracleTables mockTimOracleTables;

    @Before
    public void setupSubTest() {
        doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
        doReturn(-1l).when(uut).executeAndLog(mockPreparedStatement, "pathId");
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void InsertPath_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<Long> data = uut.InsertPath();

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();

    }

    @Test
    public void InsertPath_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 1,0);

        // Act
        ResponseEntity<Long> data = uut.InsertPath();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}