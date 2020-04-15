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
public class DriverAlertItisCodeServiceTest extends TestBase<DriverAlertItisCodeService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    private Long driverAlertId;
    private int itisCodeId;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
        driverAlertId = new Long(-1);
        itisCodeId = -2;
    }

    @Test
    public void insertDriverAlertItisCode_SUCCESS() throws SQLException {
        // Arrange

        // Act
        Long data = uut.insertDriverAlertItisCode(driverAlertId, itisCodeId);

        // Assert
        assertEquals(new Long(-1), data);
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 1, itisCodeId);// ITIS_CODE_ID
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 2, driverAlertId);// DRIVER_ALERT_ID
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void insertDriverAlertItisCode_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 1, itisCodeId);

        // Act
        Long data = uut.insertDriverAlertItisCode(driverAlertId, itisCodeId);

        // Assert
        assertEquals(new Long(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}