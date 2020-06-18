package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

public class DriverAlertItisCodeServiceTest extends TestBase<DriverAlertItisCodeService> {

    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;

    private Long driverAlertId;
    private int itisCodeId;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
        driverAlertId = Long.valueOf(-1);
        itisCodeId = -2;
    }

    @Test
    public void insertDriverAlertItisCode_SUCCESS() throws SQLException {
        // Arrange

        // Act
        Long data = uut.insertDriverAlertItisCode(driverAlertId, itisCodeId);

        // Assert
        Assertions.assertEquals(Long.valueOf(-1), data);
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
        Assertions.assertEquals(Long.valueOf(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}