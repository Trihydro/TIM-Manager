package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.WydotTim;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)

public class MilepostServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<Milepost[]> mockResponseEntityMilepostArray;

    @Test
    public void getMilepostsByStartEndPoint(){
        // Arrange
        MilepostService mps = new MilepostService();
        WydotTim wydotTim = new WydotTim();
        Milepost[] mileposts = new Milepost[1];
        Milepost milepost = new Milepost();
        milepost.setBearing(22d);
        milepost.setDirection("B");
        milepost.setCommonName("route");
        mileposts[0] = milepost;
        doReturn(mileposts).when(mockResponseEntityMilepostArray).getBody();
        HttpEntity<WydotTim> entity = getEntity(wydotTim, WydotTim.class);
        String url = "null/get-milepost-start-end";
        when(mockRestTemplate.exchange(url, HttpMethod.POST, entity, Milepost[].class)).thenReturn(mockResponseEntityMilepostArray);

        // Act
        List<Milepost> data =mps.getMilepostsByStartEndPointDirection(wydotTim);
    
        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Milepost[].class);
        assertEquals(1, data.size());
        assertEquals(milepost, data.get(0));
    }
}