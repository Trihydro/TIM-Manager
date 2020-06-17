package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Instant;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.HttpLoggingModel;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class LoggingServiceTest extends BaseServiceTest {

    @Mock
    protected ResponseEntity<Long> mockResponseEntityLong;
    @Mock
    private CVRestServiceProps mockConfig;

    @InjectMocks
    private LoggingService uut = new LoggingService();

    private String baseUrl = "baseUrl";

    @BeforeEach
    public void setupSubTest() {
        doReturn(baseUrl).when(mockConfig).getCvRestService();
    }

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
        String url = String.format("%s/http-logging/add-http-logging", baseUrl);
        when(mockResponseEntityLong.getBody()).thenReturn(1l);
        doReturn(mockResponseEntityLong).when(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);

        // Act
        Long data = uut.LogHttpRequest(httpLoggingModel);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);
        Assertions.assertEquals(Long.valueOf(1), data);
    }
}