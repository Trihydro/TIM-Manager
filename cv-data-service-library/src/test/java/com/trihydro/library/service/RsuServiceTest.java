package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RestTemplateProvider.class })
public class RsuServiceTest extends BaseServiceTest {

    @Mock
    protected ResponseEntity<WydotRsu[]> mockResponseEntityWydotRsuArray;

    @Mock
    protected ResponseEntity<WydotRsuTim[]> mockResponseEntityWydotRsuTimArray;

    @Before
    public void setupSubtest() {
        WydotRsu[] wydotRsuData = new WydotRsu[1];
        wydotRsuData[0] = new WydotRsu();
        when(mockResponseEntityWydotRsuArray.getBody()).thenReturn(wydotRsuData);
    }

    @Test
    public void selectAll() {
        // Arrange
        String url = String.format("null/rsus");
        when(mockRestTemplate.getForEntity(url, WydotRsu[].class)).thenReturn(mockResponseEntityWydotRsuArray);

        // Act
        List<WydotRsu> data = RsuService.selectAll();

        // Assert
        verify(mockRestTemplate).getForEntity(url, WydotRsu[].class);
        assertEquals(1, data.size());
    }

    @Test
    public void selectRsusByRoute() {
        // Arrange
        String route = "i80";
        String url = String.format("null/rsus-by-route/%s", route);
        when(mockRestTemplate.getForEntity(url, WydotRsu[].class)).thenReturn(mockResponseEntityWydotRsuArray);

        // Act
        List<WydotRsu> data = RsuService.selectRsusByRoute(route);

        // Assert
        verify(mockRestTemplate).getForEntity(url, WydotRsu[].class);
        assertEquals(1, data.size());
    }

    @Test
    public void getFullRsusTimIsOn() {
        // Arrange
        Long timId = -1l;
        String url = String.format("null/rsus-for-tim/%d", timId);
        WydotRsuTim[] wydotRsuTimArray = new WydotRsuTim[1];
        WydotRsuTim wrt = new WydotRsuTim();
        wydotRsuTimArray[0] = wrt;
        when(mockResponseEntityWydotRsuTimArray.getBody()).thenReturn(wydotRsuTimArray);
        when(mockRestTemplate.getForEntity(url, WydotRsuTim[].class)).thenReturn(mockResponseEntityWydotRsuTimArray);

        // Act
        List<WydotRsuTim> data = RsuService.getFullRsusTimIsOn(timId);

        // Assert
        verify(mockRestTemplate).getForEntity(url, WydotRsuTim[].class);
        assertEquals(1, data.size());
    }
}