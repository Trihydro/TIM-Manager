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
import com.trihydro.library.model.CountyRoadSegment;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TriggerRoad;

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
}