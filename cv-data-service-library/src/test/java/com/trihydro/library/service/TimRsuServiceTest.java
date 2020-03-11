package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.TimRsu;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RestTemplateProvider.class })
public class TimRsuServiceTest extends BaseServiceTest {
    @Mock
    protected ResponseEntity<TimRsu[]> mockResponseEntityTimRsuArray;
    @Mock
    protected ResponseEntity<TimRsu> mockResponseEntityTimRsu;

    @Test
    public void insertTimRsu() {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;
        int rsuIndex = -3;
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("null/tim-rsu/add-tim-rsu/%d/%d/%d", timId, rsuId, rsuIndex);
        when(mockRestTemplate.exchange(url, HttpMethod.POST, entity, Long.class)).thenReturn(mockResponseEntityLong);

        // Act
        Long data = TimRsuService.insertTimRsu(timId, rsuId, rsuIndex);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);
        assertEquals(new Long(1), data);
    }

    @Test
    public void getTimRsusByTimId() {
        // Arrange
        Long timId = -1l;
        TimRsu[] timRsus = new TimRsu[1];
        TimRsu timRsu = new TimRsu();
        timRsus[0] = timRsu;
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("null/tim-rsu/tim-id/%d", timId);
        when(mockResponseEntityTimRsuArray.getBody()).thenReturn(timRsus);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, TimRsu[].class))
                .thenReturn(mockResponseEntityTimRsuArray);

        // Act
        List<TimRsu> data = TimRsuService.getTimRsusByTimId(timId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, TimRsu[].class);
        assertEquals(1, data.size());
    }

    @Test
    public void getTimRsu() {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("null/tim-rsu/tim-rsu/%d/%d", timId, rsuId);
        when(mockResponseEntityTimRsu.getBody()).thenReturn(new TimRsu());
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, TimRsu.class)).thenReturn(mockResponseEntityTimRsu);

        // Act
        TimRsu data = TimRsuService.getTimRsu(timId, rsuId);

        // Assert
        assertNotNull(data);
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, TimRsu.class);
    }
}