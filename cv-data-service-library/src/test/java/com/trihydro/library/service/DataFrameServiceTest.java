package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)

public class DataFrameServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<String[]> mockResponseEntityStringArray;
    private String[] responseArray = new String[1];

    @Before
    public void setupSubTest() {
        responseArray[0] = "test";
        doReturn(responseArray).when(mockResponseEntityStringArray).getBody();
    }

    @Test
    public void getItisCodesForDataFrameId() {
        // Arrange
        Integer dataFrameId = -1;
        String url = String.format("null/data-frame/itis-for-data-frame/%d", dataFrameId);
        HttpEntity<String> entity = getEntity(null, String.class);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, String[].class))
                .thenReturn(mockResponseEntityStringArray);

        // Act
        String[] data = DataFrameService.getItisCodesForDataFrameId(dataFrameId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, String[].class);
        assertEquals(1, data.length);
        assertEquals(responseArray[0], data[0]);
    }
}