package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.sql.Timestamp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class HmiLogControllerTest extends TestBase<HmiLogController> {
    @Test
    public void DeleteOldHmiLogs() throws SQLException {
        // Arrange
        Timestamp oneMonthPriorTimestamp = uut.getOneMonthPriorTimestamp();
        doReturn(oneMonthPriorTimestamp).when(uut).getOneMonthPriorTimestamp();

        // Act
        var data = uut.DeleteOldHmiLogs();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertTrue(data.getBody(), "Fail return on success");
        verify(mockConnection).prepareStatement("DELETE FROM hmi_log WHERE received_at < ?");

        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}