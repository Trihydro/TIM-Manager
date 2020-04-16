package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
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

@RunWith(StrictStubs.class)
public class PathServiceTest extends TestBase<PathService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void InsertPath_SUCCESS() throws SQLException {
        // Arrange

        // Act
        Long data = uut.InsertPath();

        // Assert
        assertEquals(new Long(-1), data);
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
        assertEquals(new Long(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

}