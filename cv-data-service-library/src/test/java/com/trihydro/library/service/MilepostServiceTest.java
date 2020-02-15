package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.Milepost;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)

public class MilepostServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<Milepost[]> mockResponseEntityMilepostArray;

    @Test
    public void selectMilepostRange() {
        // Arrange
        String direction = "westward";
        String route = "route";
        Double fromMilepost = 10.0;
        Double toMilepost = 20.0;
        String url = String.format("null/get-milepost-range/%s/%f/%f/%s", direction, fromMilepost, toMilepost, route);
        Milepost[] mileposts = new Milepost[1];
        Milepost milepost = new Milepost();
        milepost.setBearing(22d);
        milepost.setDirection("direction");
        milepost.setRoute("route");
        mileposts[0] = milepost;
        doReturn(mileposts).when(mockResponseEntityMilepostArray).getBody();
        when(mockRestTemplate.getForEntity(url, Milepost[].class)).thenReturn(mockResponseEntityMilepostArray);

        // Act
        List<Milepost> data = MilepostService.selectMilepostRange(direction, route, fromMilepost, toMilepost);

        // Assert
        verify(mockRestTemplate).getForEntity(url, Milepost[].class);
        assertEquals(1, data.size());
        assertEquals(milepost, data.get(0));
    }
}