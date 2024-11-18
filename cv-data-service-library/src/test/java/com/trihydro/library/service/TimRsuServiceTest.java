package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.TimRsu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class TimRsuServiceTest extends BaseServiceTest {
    @Mock
    protected ResponseEntity<TimRsu[]> mockResponseEntityTimRsuArray;
    @Mock
    protected ResponseEntity<TimRsu> mockResponseEntityTimRsu;
    @Mock
    protected CVRestServiceProps mockCVRestServiceProps;

    private String baseUrl = "baseUrl";

    @InjectMocks
    private TimRsuService uut;

    @BeforeEach
    public void setupSubTest() {
        doReturn(baseUrl).when(mockCVRestServiceProps).getCvRestService();
    }

    @Test
    public void getTimRsusByTimId() {
        // Arrange
        Long timId = -1l;
        TimRsu[] timRsus = new TimRsu[1];
        TimRsu timRsu = new TimRsu();
        timRsus[0] = timRsu;
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("%s/tim-rsu/tim-id/%d", baseUrl, timId);
        when(mockResponseEntityTimRsuArray.getBody()).thenReturn(timRsus);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, TimRsu[].class))
                .thenReturn(mockResponseEntityTimRsuArray);

        // Act
        List<TimRsu> data = uut.getTimRsusByTimId(timId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, TimRsu[].class);
        Assertions.assertEquals(1, data.size());
    }

    @Test
    public void getTimRsu() {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("%s/tim-rsu/tim-rsu/%d/%d", baseUrl, timId, rsuId);
        when(mockResponseEntityTimRsu.getBody()).thenReturn(new TimRsu());
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, TimRsu.class)).thenReturn(mockResponseEntityTimRsu);

        // Act
        TimRsu data = uut.getTimRsu(timId, rsuId);

        // Assert
        Assertions.assertNotNull(data);
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, TimRsu.class);
    }
}