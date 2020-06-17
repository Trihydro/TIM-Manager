package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.MilepostBuffer;
import com.trihydro.library.model.WydotTim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class MilepostServiceTest extends BaseServiceTest {

    @Mock
    private ResponseEntity<Milepost[]> mockResponseEntityMilepostArray;

    @Mock
    private CVRestServiceProps mockConfig;

    @InjectMocks
    private MilepostService uut;

    private String baseUrl = "baseUrl";

    @BeforeEach
    public void setupSubTest() {
        doReturn(baseUrl).when(mockConfig).getCvRestService();
    }

    @Test
    public void getMilepostsByStartEndPoint() {
        // Arrange
        WydotTim wydotTim = new WydotTim();
        Milepost[] mileposts = new Milepost[1];
        Milepost milepost = new Milepost();
        milepost.setBearing(22d);
        milepost.setDirection("B");
        milepost.setCommonName("route");
        mileposts[0] = milepost;
        doReturn(mileposts).when(mockResponseEntityMilepostArray).getBody();
        HttpEntity<WydotTim> entity = getEntity(wydotTim, WydotTim.class);
        String url = String.format("%s/get-milepost-start-end", baseUrl);
        when(mockRestTemplate.exchange(url, HttpMethod.POST, entity, Milepost[].class))
                .thenReturn(mockResponseEntityMilepostArray);

        // Act
        List<Milepost> data = uut.getMilepostsByStartEndPointDirection(wydotTim);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Milepost[].class);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(milepost, data.get(0));
    }

    @Test
    public void getMilepostsByPointWithBuffer() {
         // Arrange
         MilepostBuffer mpb = new MilepostBuffer();
         Milepost[] mileposts = new Milepost[1];
         Milepost milepost = new Milepost();
         milepost.setBearing(22d);
         milepost.setDirection("B");
         milepost.setCommonName("route");
         mileposts[0] = milepost;
         doReturn(mileposts).when(mockResponseEntityMilepostArray).getBody();
         HttpEntity<MilepostBuffer> entity = getEntity(mpb, MilepostBuffer.class);
         String url = String.format("%s/get-milepost-single-point", baseUrl);
         when(mockRestTemplate.exchange(url, HttpMethod.POST, entity, Milepost[].class))
                 .thenReturn(mockResponseEntityMilepostArray);
 
         // Act
         List<Milepost> data = uut.getMilepostsByPointWithBuffer(mpb);
 
         // Assert
         verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Milepost[].class);
         Assertions.assertEquals(1, data.size());
         Assertions.assertEquals(milepost, data.get(0));
    }
}