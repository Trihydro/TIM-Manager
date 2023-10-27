package com.trihydro.library.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TriggerRoad;
import com.trihydro.library.model.WydotTim;

public class CascadeServiceTest extends BaseServiceTest {

    @Mock
    private CVRestServiceProps mockConfig;
    private String baseUrl = "baseUrl";

    @Mock
    private ResponseEntity<TriggerRoad> mockResponseEntityTriggerRoad;
    private String roadCode = "test";
    private TriggerRoad responseTriggerRoad = new TriggerRoad(roadCode, new ArrayList<>());

    @Mock
    private ResponseEntity<Milepost[]> mockResponseEntityMileposts;
    private int countyRoadId = 1;
    private CountyRoadSegment countyRoadSegment = new CountyRoadSegment(countyRoadId, "", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false, false, false, false);
    private Milepost[] responseMileposts = new Milepost[0];

    @InjectMocks
    private CascadeService uut = new CascadeService();

    @Test
    public void testGetTriggerRoad() {
        // prepare
        String url = String.format("%s/cascade/trigger-road/%s", baseUrl, roadCode);
        
        mockConfig = Mockito.mock(CVRestServiceProps.class);
        mockRestTemplateProvider = Mockito.mock(RestTemplateProvider.class);
        mockRestTemplate = Mockito.mock(RestTemplate.class);
        mockResponseEntityTriggerRoad = Mockito.mock(ResponseEntity.class);

        when(mockConfig.getCvRestService()).thenReturn(baseUrl);
        when(mockResponseEntityTriggerRoad.getBody()).thenReturn(responseTriggerRoad);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, null, TriggerRoad.class)).thenReturn(mockResponseEntityTriggerRoad);
        when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

        uut.InjectDependencies(mockConfig, mockRestTemplateProvider);

        // execute
        TriggerRoad result = uut.getTriggerRoad(roadCode);

        // verify
        verify(mockConfig).getCvRestService();
        verify(mockRestTemplateProvider).GetRestTemplate();
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, null, TriggerRoad.class);
        verify(mockResponseEntityTriggerRoad).getBody();
        Assertions.assertEquals(responseTriggerRoad, result);
    }

    @Test
    public void testGetMilepostsForSegment() {
        // prepare
        String url = String.format("%s/cascade/mileposts/%s", baseUrl, countyRoadId);

        mockConfig = Mockito.mock(CVRestServiceProps.class);
        mockRestTemplateProvider = Mockito.mock(RestTemplateProvider.class);
        mockRestTemplate = Mockito.mock(RestTemplate.class);
        mockResponseEntityMileposts = Mockito.mock(ResponseEntity.class);

        when(mockConfig.getCvRestService()).thenReturn(baseUrl);
        when(mockResponseEntityMileposts.getBody()).thenReturn(responseMileposts);
        when(mockRestTemplate.exchange(url, HttpMethod.GET, null, Milepost[].class)).thenReturn(mockResponseEntityMileposts);
        when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

        uut.InjectDependencies(mockConfig, mockRestTemplateProvider);

        // execute
        List<Milepost> result = uut.getMilepostsForSegment(countyRoadSegment);

        // verify
        verify(mockConfig).getCvRestService();
        verify(mockRestTemplateProvider).GetRestTemplate();
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, null, Milepost[].class);
        verify(mockResponseEntityMileposts).getBody();
        Assertions.assertEquals(responseMileposts.length, result.size());
    }

    @Test
    public void testContainsCascadingCondition_TRUE() {
        // prepare
        List<String> itisCodes = new ArrayList<String>() {
            {
                add("770");
                add("Closed to light, high profile vehicles");
            }
        };
        WydotTim wydotTim = new WydotTim();
        wydotTim.setItisCodes(itisCodes);

        // execute
        boolean result = uut.containsCascadingCondition(wydotTim);

        // verify
        Assertions.assertTrue(result);
    }

    @Test
    public void testContainsCascadingCondition_FALSE() {
        // prepare
        List<String> itisCodes = new ArrayList<String>() {
            {
                add("test");
            }
        };
        WydotTim wydotTim = new WydotTim();
        wydotTim.setItisCodes(itisCodes);

        // execute
        boolean result = uut.containsCascadingCondition(wydotTim);

        // verify
        Assertions.assertFalse(result);
    }

    @Test
    public void testBuildCascadeTim_CLOSED() {
        // prepare
        CountyRoadSegment countyRoadSegment = new CountyRoadSegment(1, "", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, true, false, false, false);
        Milepost anchor = new Milepost();
        Milepost lastMilepost = new Milepost();
        String clientId = "test";

        // execute
        WydotTim result = uut.buildCascadeTim(countyRoadSegment, anchor, lastMilepost, clientId);

        // verify
        String expectedClientId = String.format(clientId + "_triggered_" + countyRoadSegment.getId());
        Coordinate expectedStartPoint = new Coordinate(anchor.getLatitude(), anchor.getLongitude());
        Coordinate expectedEndPoint = new Coordinate(lastMilepost.getLatitude(), lastMilepost.getLongitude());
        List<String> expectedItisCodes = new ArrayList<String>() {
            {
                add("770");
            }
        };
        Assertions.assertEquals(expectedClientId, result.getClientId());
        Assertions.assertEquals(expectedStartPoint.getLatitude(), result.getStartPoint().getLatitude());
        Assertions.assertEquals(expectedStartPoint.getLongitude(), result.getStartPoint().getLongitude());
        Assertions.assertEquals(expectedEndPoint.getLatitude(), result.getEndPoint().getLatitude());
        Assertions.assertEquals(expectedEndPoint.getLongitude(), result.getEndPoint().getLongitude());
        Assertions.assertEquals(expectedItisCodes, result.getItisCodes());
    }

    @Test
    public void testBuildCascadeTim_C2LHPV() {
        // prepare
        CountyRoadSegment countyRoadSegment = new CountyRoadSegment(1, "", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false, true, false, false);
        Milepost anchor = new Milepost();
        Milepost lastMilepost = new Milepost();
        String clientId = "test";

        // execute
        WydotTim result = uut.buildCascadeTim(countyRoadSegment, anchor, lastMilepost, clientId);

        // verify
        String expectedClientId = String.format(clientId + "_triggered_" + countyRoadSegment.getId());
        Coordinate expectedStartPoint = new Coordinate(anchor.getLatitude(), anchor.getLongitude());
        Coordinate expectedEndPoint = new Coordinate(lastMilepost.getLatitude(), lastMilepost.getLongitude());
        List<String> expectedItisCodes = new ArrayList<String>() {
            {
                add("Closed to light, high profile vehicles");
            }
        };
        Assertions.assertEquals(expectedClientId, result.getClientId());
        Assertions.assertEquals(expectedStartPoint.getLatitude(), result.getStartPoint().getLatitude());
        Assertions.assertEquals(expectedStartPoint.getLongitude(), result.getStartPoint().getLongitude());
        Assertions.assertEquals(expectedEndPoint.getLatitude(), result.getEndPoint().getLatitude());
        Assertions.assertEquals(expectedEndPoint.getLongitude(), result.getEndPoint().getLongitude());
        Assertions.assertEquals(expectedItisCodes, result.getItisCodes());
    }
}