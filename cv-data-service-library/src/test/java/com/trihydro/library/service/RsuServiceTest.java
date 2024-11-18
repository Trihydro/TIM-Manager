package com.trihydro.library.service;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

public class RsuServiceTest extends BaseServiceTest {

    @Mock
    protected ResponseEntity<WydotRsu[]> mockResponseEntityWydotRsuArray;
    @Mock
    protected ResponseEntity<WydotRsuTim[]> mockResponseEntityWydotRsuTimArray;
    @Mock
    protected CVRestServiceProps mockCVRestServiceProps;

    @InjectMocks
    private RsuService uut;

    private String baseUrl = "baseUrl";

    @BeforeEach
    public void setupSubtest() {
        doReturn(baseUrl).when(mockCVRestServiceProps).getCvRestService();
    }

    private void setWydotRsuReturn(){
        WydotRsu[] wydotRsuData = new WydotRsu[1];
        wydotRsuData[0] = new WydotRsu();
        when(mockResponseEntityWydotRsuArray.getBody()).thenReturn(wydotRsuData);
    }

    @Test
    public void selectAll() {
        // Arrange
        setWydotRsuReturn();
        String url = String.format("%s/rsus", baseUrl);
        when(mockRestTemplate.getForEntity(url, WydotRsu[].class)).thenReturn(mockResponseEntityWydotRsuArray);

        // Act
        List<WydotRsu> data = uut.selectAll();

        // Assert
        verify(mockRestTemplate).getForEntity(url, WydotRsu[].class);
        Assertions.assertEquals(1, data.size());
    }

    @Test
    public void selectRsusByRoute() {
        // Arrange
        setWydotRsuReturn();
        String route = "i80";
        String url = String.format("%s/rsus-by-route/%s", baseUrl, route);
        when(mockRestTemplate.getForEntity(url, WydotRsu[].class)).thenReturn(mockResponseEntityWydotRsuArray);

        // Act
        List<WydotRsu> data = uut.selectRsusByRoute(route);

        // Assert
        verify(mockRestTemplate).getForEntity(url, WydotRsu[].class);
        Assertions.assertEquals(1, data.size());
    }

    @Test
    public void getFullRsusTimIsOn() {
        // Arrange
        Long timId = -1l;
        String url = String.format("%s/rsus-for-tim/%d", baseUrl, timId);
        WydotRsuTim[] wydotRsuTimArray = new WydotRsuTim[1];
        WydotRsuTim wrt = new WydotRsuTim();
        wydotRsuTimArray[0] = wrt;
        when(mockResponseEntityWydotRsuTimArray.getBody()).thenReturn(wydotRsuTimArray);
        when(mockRestTemplate.getForEntity(url, WydotRsuTim[].class)).thenReturn(mockResponseEntityWydotRsuTimArray);

        // Act
        List<WydotRsuTim> data = uut.getFullRsusTimIsOn(timId);

        // Assert
        verify(mockRestTemplate).getForEntity(url, WydotRsuTim[].class);
        Assertions.assertEquals(1, data.size());
    }
}