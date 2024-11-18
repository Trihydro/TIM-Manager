package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.TimType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimTypeServiceTest extends TestBase<TimTypeService> {

    @Test
    public void getTimTypes_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getLong("TIM_TYPE_ID");

        // Act
        List<TimType> data = uut.getTimTypes();

        // Assert
        Assertions.assertEquals(0, data.size());
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }

    @Test
    public void getTimTypes_SUCCESS() throws SQLException {
        // Arrange

        // Act
        List<TimType> data = uut.getTimTypes();

        // Assert
        Assertions.assertEquals(1, data.size());
        verify(mockRs).getLong("TIM_TYPE_ID");
        verify(mockRs).getString("TYPE");
        verify(mockRs).getString("DESCRIPTION");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
    }
}