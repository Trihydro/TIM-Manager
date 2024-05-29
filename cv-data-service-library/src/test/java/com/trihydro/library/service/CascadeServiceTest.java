package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
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
    private ResponseEntity<String> mockResponseEntityTriggerRoadJson;
    private String roadCode = "example road code";
    private TriggerRoad responseTriggerRoad = new TriggerRoad(roadCode, new ArrayList<>());

    @Mock
    private ResponseEntity<Milepost[]> mockResponseEntityMileposts;
    private int countyRoadId = 1;
    private CountyRoadSegment countyRoadSegment = new CountyRoadSegment(countyRoadId, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, true, true, true, true);
    private Milepost[] responseMilepostsEmpty = new Milepost[0];
    private Milepost[] responseMilepostsPopulated = new Milepost[] {
        new Milepost(),
        new Milepost()
    };

    @InjectMocks
    private CascadeService uut = new CascadeService();

    @Test
    public void testGetTriggerRoad() {
        // prepare
        String url = String.format("%s/cascade/trigger-road/%s", baseUrl, roadCode);
        
        mockConfig = Mockito.mock(CVRestServiceProps.class);
        mockRestTemplateProvider = Mockito.mock(RestTemplateProvider.class);
        mockRestTemplate = Mockito.mock(RestTemplate.class);
        mockResponseEntityTriggerRoadJson = Mockito.mock(ResponseEntity.class);
        responseTriggerRoad.addCountyRoadSegment(countyRoadSegment);

        when(mockConfig.getCvRestService()).thenReturn(baseUrl);
        when(mockResponseEntityTriggerRoadJson.getBody()).thenReturn("{\"roadCode\":\"example road code\",\"countyRoadSegments\":[{\"countyRoadId\":1,\"commonName\":\"example common name\",\"mFrom\":1.0,\"mTo\":2.0,\"xFrom\":3.0,\"yFrom\":4.0,\"xTo\":5.0,\"yTo\":6.0,\"closed\":true,\"c2lhpv\":true,\"loct\":true,\"ntt\":true}]}");
        when(mockRestTemplate.exchange(url, HttpMethod.GET, null, String.class)).thenReturn(mockResponseEntityTriggerRoadJson);
        when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

        uut.InjectDependencies(mockConfig, mockRestTemplateProvider);

        // execute
        TriggerRoad result = uut.getTriggerRoad(roadCode);

        // verify
        verify(mockConfig).getCvRestService();
        verify(mockRestTemplateProvider).GetRestTemplate();
        verify(mockRestTemplate).exchange(url, HttpMethod.GET, null, String.class);
        verify(mockResponseEntityTriggerRoadJson).getBody();
        assertEquals(responseTriggerRoad.getRoadCode(), result.getRoadCode());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().size(), result.getCountyRoadSegments().size());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).getId(), result.getCountyRoadSegments().get(0).getId());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).getCommonName(), result.getCountyRoadSegments().get(0).getCommonName());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).getMFrom(), result.getCountyRoadSegments().get(0).getMFrom());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).getMTo(), result.getCountyRoadSegments().get(0).getMTo());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).getXFrom(), result.getCountyRoadSegments().get(0).getXFrom());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).getYFrom(), result.getCountyRoadSegments().get(0).getYFrom());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).getXTo(), result.getCountyRoadSegments().get(0).getXTo());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).getYTo(), result.getCountyRoadSegments().get(0).getYTo());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).isClosed(), result.getCountyRoadSegments().get(0).isClosed());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).isC2lhpv(), result.getCountyRoadSegments().get(0).isC2lhpv());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).isLoct(), result.getCountyRoadSegments().get(0).isLoct());
        assertEquals(responseTriggerRoad.getCountyRoadSegments().get(0).isNtt(), result.getCountyRoadSegments().get(0).isNtt());
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
        when(mockResponseEntityMileposts.getBody()).thenReturn(responseMilepostsEmpty);
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
        Assertions.assertEquals(responseMilepostsEmpty.length, result.size());
    }

    @Test
    public void testBuildCascadeTim_CLOSED() {
        // prepare
        CountyRoadSegment countyRoadSegment = new CountyRoadSegment(1, "", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, true, false, false, false);
        Milepost firstMilepost = new Milepost();
        Milepost lastMilepost = new Milepost();
        String clientId = "test";

        // execute
        WydotTim result = uut.buildCascadeTim(countyRoadSegment, firstMilepost, lastMilepost, clientId);

        // verify
        String expectedClientId = String.format(clientId + CascadeService.CASCADE_TIM_ID_DELIMITER + countyRoadSegment.getId());
        Coordinate expectedStartPoint = new Coordinate(firstMilepost.getLatitude(), firstMilepost.getLongitude());
        Coordinate expectedEndPoint = new Coordinate(lastMilepost.getLatitude(), lastMilepost.getLongitude());
        List<String> expectedItisCodes = new ArrayList<String>() {
            {
                add("769");
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
        Milepost firstMilepost = new Milepost();
        Milepost lastMilepost = new Milepost();
        String clientId = "test";

        // execute
        WydotTim result = uut.buildCascadeTim(countyRoadSegment, firstMilepost, lastMilepost, clientId);

        // verify
        String expectedClientId = String.format(clientId + CascadeService.CASCADE_TIM_ID_DELIMITER + countyRoadSegment.getId());
        Coordinate expectedStartPoint = new Coordinate(firstMilepost.getLatitude(), firstMilepost.getLongitude());
        Coordinate expectedEndPoint = new Coordinate(lastMilepost.getLatitude(), lastMilepost.getLongitude());
        List<String> expectedItisCodes = new ArrayList<String>() {
            {
                add("2569");
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
    public void testIsCascadeTim_TRUE() {
        // prepare
        WydotTim wydotTim = new WydotTim();
        wydotTim.setClientId("test" + CascadeService.CASCADE_TIM_ID_DELIMITER + countyRoadId);

        // execute
        boolean result = CascadeService.isCascadeTim(wydotTim);

        // verify
        Assertions.assertTrue(result);
    }

    @Test
    public void testIsCascadeTim_FALSE() {
        // prepare
        WydotTim wydotTim = new WydotTim();
        wydotTim.setClientId("test");

        // execute
        boolean result = CascadeService.isCascadeTim(wydotTim);

        // verify
        Assertions.assertFalse(result);
    }

    @Test
    public void testGetAllMilepostsFromCascadeTim_SUCCESS() {
        // prepare
        WydotTim wydotTim = new WydotTim();
        wydotTim.setClientId("test" + CascadeService.CASCADE_TIM_ID_DELIMITER + countyRoadId);

        mockConfig = Mockito.mock(CVRestServiceProps.class);
        mockRestTemplateProvider = Mockito.mock(RestTemplateProvider.class);
        mockRestTemplate = Mockito.mock(RestTemplate.class);
        mockResponseEntityMileposts = Mockito.mock(ResponseEntity.class);

        when(mockConfig.getCvRestService()).thenReturn(baseUrl);
        when(mockResponseEntityMileposts.getBody()).thenReturn(responseMilepostsPopulated);
        when(mockRestTemplate.exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(), Mockito.any(Class.class))).thenReturn(mockResponseEntityMileposts);
        when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

        uut.InjectDependencies(mockConfig, mockRestTemplateProvider);
        
        // execute
        List<Milepost> result = uut.getAllMilepostsFromCascadeTim(wydotTim);

        // verify
        verify(mockConfig).getCvRestService();
        verify(mockRestTemplateProvider).GetRestTemplate();
        verify(mockRestTemplate).exchange(Mockito.anyString(), Mockito.any(HttpMethod.class), Mockito.any(), Mockito.any(Class.class));
        verify(mockResponseEntityMileposts).getBody();
        Assertions.assertEquals(responseMilepostsPopulated.length, result.size());
    }

    @Test
    public void testGetAllMilepostsFromCascadeTim_FAILURE_ArrayOutOfBoundsException() {
        // prepare
        WydotTim wydotTim = new WydotTim();
        wydotTim.setClientId("test");

        // execute and verify exception occurred
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            uut.getAllMilepostsFromCascadeTim(wydotTim);
        });
    }

    @Test
    public void testGetAllMilepostsFromCascadeTim_FAILURE_NumberFormatException() {
        // prepare
        WydotTim wydotTim = new WydotTim();
        wydotTim.setClientId("test" + CascadeService.CASCADE_TIM_ID_DELIMITER + "test");

        // execute and verify exception occurred
        Assertions.assertThrows(NumberFormatException.class, () -> {
            uut.getAllMilepostsFromCascadeTim(wydotTim);
        });
    }

    @Test
    public void testGetSegmentIdFromClientId_SUCCESS() {
        // prepare
        int segmentId = 1;
        String clientId = "test" + CascadeService.CASCADE_TIM_ID_DELIMITER + segmentId + "-number";

        // execute
        int result = uut.getSegmentIdFromClientId(clientId);

        // verify
        Assertions.assertEquals(segmentId, result);
    }

    @Test
    public void testGetSegmentIdFromClientId_FAILURE_ArrayOutOfBoundsException() {
        // prepare
        String clientId = "test";

        // execute and verify exception occurred
        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            uut.getSegmentIdFromClientId(clientId);
        });
    }

    @Test
    public void testGetSegmentIdFromClientId_FAILURE_NumberFormatException() {
        // prepare
        String clientId = "test" + CascadeService.CASCADE_TIM_ID_DELIMITER + "test";

        // execute and verify exception occurred
        Assertions.assertThrows(NumberFormatException.class, () -> {
            uut.getSegmentIdFromClientId(clientId);
        });
    }
}