package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.trihydro.library.model.CVRestServiceProps;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class RegionServiceTest extends BaseServiceTest {

    @Mock
    protected ResponseEntity<Boolean> mockResponseEntityBoolean;

    @Mock
    private CVRestServiceProps mockConfig;

    @InjectMocks
    private RegionService uut;

    private String baseUrl = "baseUrl";

    @BeforeEach
    public void setupSubTest() {
        doReturn(true).when(mockResponseEntityBoolean).getBody();
        doReturn(baseUrl).when(mockConfig).getCvRestService();
    }

    @Test
    public void updateRegionName() {
        // Arrange
        Long regionId = -1l;
        String name = "newName";
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("%s/region/update-region-name/%d/%s", baseUrl, regionId, name);
        doReturn(mockResponseEntityBoolean).when(mockRestTemplate).exchange(url, HttpMethod.PUT, entity, Boolean.class);

        // Act
        Boolean data = uut.updateRegionName(regionId, name);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.PUT, entity, Boolean.class);
        Assertions.assertEquals(true, data);
    }
}