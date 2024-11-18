package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.TimType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TimTypeControllerTest extends TestBase<TimTypeController> {
    @Test
    public void SelectAll_SUCCESS() throws SQLException {
        // Arrange
        String selectStatement = "select * from TIM_TYPE";

        // Act
        ResponseEntity<List<TimType>> data = uut.SelectAll();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getLong("TIM_TYPE_ID");
        verify(mockRs).getString("TYPE");
        verify(mockRs).getString("DESCRIPTION");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectAll_FAIL() throws SQLException {
        // Arrange
        String selectStatement = "select * from TIM_TYPE";
        doThrow(new SQLException()).when(mockRs).getLong("TIM_TYPE_ID");

        // Act
        ResponseEntity<List<TimType>> data = uut.SelectAll();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }
}