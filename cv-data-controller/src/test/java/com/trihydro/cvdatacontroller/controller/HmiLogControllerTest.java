package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpStatus;

@RunWith(StrictStubs.class)
public class HmiLogControllerTest extends TestBase<HmiLogController> {
    @Test
    public void DeleteOldHmiLogs() throws SQLException {
        // Arrange
        DateFormat sdf = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");
        TimeZone toTimeZone = TimeZone.getTimeZone("MST");
        sdf.setTimeZone(toTimeZone);
        Date dte = java.sql.Date.valueOf(LocalDate.now().minus(1, ChronoUnit.MONTHS));
        String strDate = sdf.format(dte.getTime());
        doReturn(strDate).when(uut).getOneMonthPrior();

        // Act
        var data = uut.DeleteOldHmiLogs();

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        assertTrue("Fail return on success", data.getBody());
        verify(mockConnection).prepareStatement("DELETE FROM hmi_log WHERE received_at < ?");

        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}