package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.DriverAlertType;

import org.junit.Test;

public class DriverAlertTypeServiceTest extends TestBase<DriverAlertTypeService> {

    @Test
    public void getDriverAlertTypes_SUCCESS() throws SQLException {
        // Arrange

        // Act
        List<DriverAlertType> data = uut.getDriverAlertTypes();

        // Assert
        assertEquals(1, data.size());
        verify(mockStatement).executeQuery("select * from DRIVER_ALERT_TYPE");
        verify(mockRs).getInt("DRIVER_ALERT_TYPE_ID");
        verify(mockRs).getString("SHORT_NAME");
        verify(mockRs).getString("DESCRIPTION");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getDriverAlertTypes_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getInt("DRIVER_ALERT_TYPE_ID");
        // Act
        List<DriverAlertType> data = uut.getDriverAlertTypes();

        // Assert
        assertEquals(0, data.size());
        verify(mockStatement).executeQuery("select * from DRIVER_ALERT_TYPE");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }
}