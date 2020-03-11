package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.sql.Timestamp;
import java.time.Instant;

import com.trihydro.library.model.HttpLoggingModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoggingService.class })
public class LoggingServiceTest extends BaseServiceTest {

    private LoggingService uut = new LoggingService();

    @Test
    public void LogHttpRequest() {
        // Arrange
        String request = "request";
        Timestamp requestTime = Timestamp.from(Instant.now());
        Timestamp responseTime = Timestamp.from(Instant.now());

        HttpLoggingModel httpLoggingModel = new HttpLoggingModel();
        httpLoggingModel.setRequest(request);
        httpLoggingModel.setRequestTime(requestTime);
        httpLoggingModel.setResponseTime(responseTime);

        HttpEntity<HttpLoggingModel> entity = getEntity(httpLoggingModel, HttpLoggingModel.class);
        String url = "null/http-logging/add-http-logging";
        doReturn(mockResponseEntityLong).when(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);

        // Act
        Long data = uut.LogHttpRequest(httpLoggingModel);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);
        assertEquals(new Long(1), data);
    }
}