package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.HttpLoggingModel;
import com.trihydro.library.tables.LoggingTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HttpLoggingControllerTest extends TestBase<HttpLoggingController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private LoggingTables mockLoggingTables;

    @BeforeEach
    public void setupSubTest() {
        doReturn("").when(mockLoggingTables).buildInsertQueryStatement(any(), any());
        uut.InjectDependencies(mockLoggingTables, mockSqlNullHandler);
    }

    @Test
    public void LogHttpRequest_SUCCESS() throws SQLException {
        // Arrange
        HttpLoggingModel httpLoggingModel = new HttpLoggingModel();
        httpLoggingModel.setRequest("request");
        httpLoggingModel.setRequestTime(Timestamp.from(Instant.now()));

        // Act
        ResponseEntity<Long> data = uut.LogHttpRequest(httpLoggingModel);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(Long.valueOf(-1), data.getBody());
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 1, httpLoggingModel.getRequestTime());// REQUEST_TIME
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, httpLoggingModel.getRequest());// REST_REQUEST
        verify(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 3, httpLoggingModel.getResponseTime());// RESPONSE_TIME
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void LogHttpRequest_FAIL() throws SQLException {
        // Arrange
        HttpLoggingModel httpLoggingModel = new HttpLoggingModel();
        httpLoggingModel.setRequest("request");
        httpLoggingModel.setRequestTime(Timestamp.from(Instant.now()));
        doThrow(new SQLException()).when(mockSqlNullHandler).setTimestampOrNull(mockPreparedStatement, 1,
                httpLoggingModel.getRequestTime());

        // Act
        ResponseEntity<Long> data = uut.LogHttpRequest(httpLoggingModel);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(Long.valueOf(0), data.getBody());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}