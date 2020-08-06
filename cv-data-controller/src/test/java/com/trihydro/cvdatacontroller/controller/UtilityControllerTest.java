package com.trihydro.cvdatacontroller.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.http.HttpStatus;

public class UtilityControllerTest extends TestBase<UtilityController> {

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Test
    public void getBsmCoreDataPartitions_success() throws SQLException {
        // Arrange
        doReturn("sys_1").when(mockRs).getString("PARTITION_NAME");
        doReturn("TIMESTAMP' 2020-01-01 00:00:00'").when(mockRs).getString("HIGH_VALUE");

        // Act
        var result = uut.getBsmCoreDataPartitions();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        assertEquals("sys_1", result.getBody().get(0).getPartitionName());
        assertEquals("Wed Jan 01 00:00:00 UTC 2020", result.getBody().get(0).getHighValue().toString());
    }

    @Test
    public void getBsmCoreDataPartitions_noRecords() throws SQLException {
        // Arrange
        doReturn(false).when(mockRs).next();

        // Act
        var result = uut.getBsmCoreDataPartitions();

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(0, result.getBody().size());
    }

    @Test
    public void getBsmCoreDataPartitions_handlesError() throws SQLException {
        // Arrange
        when(mockRs.next()).thenThrow(new SQLException("something went wrong..."));

        // Act
        var result = uut.getBsmCoreDataPartitions();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void dropBsmPartitions_success() throws SQLException {
        // Arrange
        when(mockPreparedStatement.execute()).thenReturn(true);

        // Act
        var result = uut.dropBsmPartitions(Arrays.asList(new String[] { "sys_1" }));

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(mockConnection).prepareStatement(sqlCaptor.capture());
        verify(mockPreparedStatement).execute();

        assertEquals("ALTER TABLE BSM_CORE_DATA DROP PARTITION sys_1 UPDATE INDEXES", sqlCaptor.getValue());
    }

    @Test
    public void dropBsmPartitions_invalidRequest() throws SQLException {
        // Arrange
        var names = Arrays.asList(new String[0]);

        // Act
        var result = uut.dropBsmPartitions(names);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        verify(mockPreparedStatement, times(0)).execute();
    }

    @Test
    public void dropBsmPartitions_handlesError() throws SQLException {
        // Arrange
        when(mockPreparedStatement.execute()).thenThrow(new SQLException("something went wrong..."));

        // Act
        var result = uut.dropBsmPartitions(Arrays.asList(new String[] { "sys_1" }));

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }
}