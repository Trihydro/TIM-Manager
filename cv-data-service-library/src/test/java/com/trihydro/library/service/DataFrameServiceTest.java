package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trihydro.library.model.CVRestServiceProps;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class DataFrameServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<String[]> mockResponseEntityStringArray;
    private String[] responseArray = new String[1];
    private String baseUrl = "baseUrl";

    @Mock
    private CVRestServiceProps mockConfig;

    @InjectMocks
    private DataFrameService uut;

    @BeforeEach
    public void setupSubTest() {
        responseArray[0] = "test";
        doReturn(responseArray).when(mockResponseEntityStringArray).getBody();
        doReturn(baseUrl).when(mockConfig).getCvRestService();
    }

    @Test
    public void getItisCodesForDataFrameId() {
        // Arrange
        Integer dataFrameId = -1;
        String url = String.format("%s/data-frame/itis-for-data-frame/%d", baseUrl, dataFrameId);
        HttpEntity<String> entity = getEntity(null, String.class);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, entity, String[].class))
                .thenReturn(mockResponseEntityStringArray);

        // Act
        String[] data = uut.getItisCodesForDataFrameId(dataFrameId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, entity, String[].class);
        Assertions.assertEquals(1, data.length);
        Assertions.assertEquals(responseArray[0], data[0]);
    }
}