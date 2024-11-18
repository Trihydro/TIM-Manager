package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class StatusLogControllerTest extends TestBase<StatusLogController> {
    @Test
    public void DeleteOldStatusLogs() throws SQLException {
        // Arrange
        String strDate = uut.getOneMonthPriorString();
        doReturn(strDate).when(uut).getOneMonthPriorString();

        // Act
        var data = uut.DeleteOldStatusLogs();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertTrue(data.getBody(),"Fail return on success");
        verify(mockConnection).prepareStatement("DELETE FROM status_log WHERE status_time < ?");

        verify(mockPreparedStatement).setString(1, strDate);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

}