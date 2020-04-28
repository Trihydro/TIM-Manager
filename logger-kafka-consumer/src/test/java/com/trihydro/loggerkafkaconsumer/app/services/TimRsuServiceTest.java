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
public class TimRsuServiceTest extends TestBase<TimRsuService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
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
        assertEquals(Long.valueOf(-1), data);
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
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, timId);

        // Act
        Long data = uut.AddTimRsu(timId, rsuId, rsuIndex);

        // Assert
        assertEquals(Long.valueOf(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}