package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.trihydro.library.model.CVRestServiceProps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RunWith(StrictStubs.class)

public class RegionServiceTest extends BaseServiceTest {

    @Mock
    protected ResponseEntity<Boolean> mockResponseEntityBoolean;

    @Mock
    private CVRestServiceProps mockConfig;

    @InjectMocks
    private RegionService uut;

    private String baseUrl = "baseUrl";

    @Before
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
        assertEquals(true, data);
    }
}