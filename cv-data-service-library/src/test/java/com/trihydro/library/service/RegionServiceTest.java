package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;

@RunWith(PowerMockRunner.class)

public class RegionServiceTest extends BaseServiceTest {

    @Mock
    protected ResponseEntity<Boolean> mockResponseEntityBoolean;

    @Before
    public void setupSubTest() {
        doReturn(true).when(mockResponseEntityBoolean).getBody();
    }

    @Test
    public void insertRegion() {
        // Arrange
        Long dataFrameId = -1l;
        Long pathId = -2l;
        Region region = new Region();
        HttpEntity<Region> entity = getEntity(region, Region.class);
        String url = String.format("null/region/add-region/%d/%d", dataFrameId, pathId);
        doReturn(mockResponseEntityLong).when(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);

        // Act
        Long data = RegionService.insertRegion(dataFrameId, pathId, region);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);
        assertEquals(new Long(1), data);
    }

    @Test
    public void updateRegionName() {
        // Arrange
        Long regionId = -1l;
        String name = "newName";
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("null/region/update-region-name/%d/%s", regionId, name);
        doReturn(mockResponseEntityBoolean).when(mockRestTemplate).exchange(url, HttpMethod.PUT, entity, Boolean.class);

        // Act
        Boolean data = RegionService.updateRegionName(regionId, name);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.PUT, entity, Boolean.class);
        assertEquals(true, data);
    }
}