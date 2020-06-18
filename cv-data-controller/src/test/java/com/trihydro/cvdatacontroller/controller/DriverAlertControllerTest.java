package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class DriverAlertControllerTest extends TestBase<DriverAlertController> {
    @Test
    public void DeleteOldDriverAlert() throws SQLException {
        // Arrange
        DateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
        TimeZone toTimeZone = TimeZone.getTimeZone("MST");
        sdf.setTimeZone(toTimeZone);
        Date dte = java.sql.Date.valueOf(LocalDate.now().minus(1, ChronoUnit.MONTHS));
        String strDate = sdf.format(dte.getTime());
        doReturn(strDate).when(uut).getOneMonthPrior();

        // Act
        var data = uut.DeleteOldDriverAlert();

        // Assert
        String deleteSQL = "DELETE FROM driver_alert_itis_code WHERE driver_alert_id IN";
        deleteSQL += " (SELECT driver_alert_id FROM driver_alert WHERE ode_received_at < ?)";

        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertTrue(data.getBody(), "Fail return on success");
        verify(mockConnection).prepareStatement(deleteSQL);
        verify(mockConnection).prepareStatement("DELETE FROM driver_alert WHERE ode_received_at < ?");

        verify(mockPreparedStatement, times(2)).close();
        verify(mockConnection, times(2)).close();
    }

}