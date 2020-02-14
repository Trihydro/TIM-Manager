package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotTim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ActiveTimService.class, RestTemplateProvider.class })
public class ActiveTimServiceTest {
    @Mock
    private RestTemplate mockRestTemplate;
    @Mock
    private ResponseEntity<TimUpdateModel[]> mockResponseEntity;
    @Mock
    private ResponseEntity<Boolean> mockResponseEntityBoolean;
    @Mock
    private ResponseEntity<ActiveTim[]> mockResponseEntityActiveTims;

    private Long timTypeId = -1l;
    private List<WydotTim> wydotTims;

    @Before
    public void setup() throws SQLException {
        PowerMockito.mockStatic(RestTemplateProvider.class);
        when(RestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);
        doReturn(true).when(mockResponseEntityBoolean).getBody();
    }

    private void setupWydotTims() {
        wydotTims = new ArrayList<>();
        WydotTim wydotTim = new WydotTim();
        wydotTim.setDirection("westbound");
        wydotTim.setClientId("unit_test_id1");
        wydotTims.add(wydotTim);
        wydotTim = new WydotTim();
        wydotTim.setDirection("eastbound");
        wydotTim.setClientId("unit_test_id2");
        wydotTims.add(wydotTim);
    }

    @Test
    public void getActiveTimsMissingItisCodes() throws SQLException {
        // Arrange
        TimUpdateModel[] tums = new TimUpdateModel[1];
        TimUpdateModel tum = new TimUpdateModel();
        tum.setTimId(1l);
        tum.setMilepostStart(1d);
        tum.setMilepostStop(2d);
        tum.setDirection("both");
        tum.setRoute("I 80");
        tum.setClientId("123");
        tum.setSatRecordId("HEX");
        tum.setActiveTimId(1l);
        tums[0] = tum;

        when(mockRestTemplate.getForEntity("null/active-tim/missing-itis", TimUpdateModel[].class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(tums);
        // Act
        List<ActiveTim> ats = ActiveTimService.getActiveTimsMissingItisCodes();

        // Assert
        assertEquals(1, ats.size());
        ActiveTim tim = ats.get(0);
        assertEquals(tum, tim);
    }

    @Test
    public void getActiveTimsNotSent() {
        // Arrange
        TimUpdateModel[] tums = new TimUpdateModel[1];
        TimUpdateModel tum = new TimUpdateModel();
        tum.setTimId(1l);
        tum.setMilepostStart(1d);
        tum.setMilepostStop(2d);
        tum.setDirection("both");
        tum.setRoute("I 80");
        tum.setClientId("123");
        tum.setSatRecordId("HEX");
        tum.setActiveTimId(1l);
        tums[0] = tum;
        when(mockRestTemplate.getForEntity("null/active-tim/not-sent", TimUpdateModel[].class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(tums);

        // Act
        List<ActiveTim> ats = ActiveTimService.getActiveTimsNotSent();

        // Assert
        assertEquals(1, ats.size());
        ActiveTim tim = ats.get(0);
        assertEquals(tum, tim);
    }

    private HttpHeaders getDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    public void deleteActiveTimsById() throws SQLException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1l);
        activeTimIds.add(-2l);
        HttpEntity<List<Long>> entity = new HttpEntity<List<Long>>(activeTimIds, getDefaultHeaders());
        when(mockRestTemplate.exchange("null/active-tim/delete-ids", HttpMethod.DELETE, entity, Boolean.class))
                .thenReturn(mockResponseEntityBoolean);

        // Act
        boolean success = ActiveTimService.deleteActiveTimsById(activeTimIds);

        // Assert
        assertTrue(success);
    }

    @Test
    public void getActiveTimsByWydotTim() throws SQLException {
        // Arrange
        setupWydotTims();
        HttpEntity<List<WydotTim>> entity = new HttpEntity<List<WydotTim>>(wydotTims, getDefaultHeaders());
        when(mockRestTemplate.exchange("null/active-tim/get-by-wydot-tim/" + timTypeId, HttpMethod.PUT, entity,
                ActiveTim[].class)).thenReturn(mockResponseEntityActiveTims);
        ActiveTim[] aTims = new ActiveTim[1];
        ActiveTim aTim = new ActiveTim();
        aTim.setActiveTimId(-1l);
        aTims[0] = aTim;
        doReturn(aTims).when(mockResponseEntityActiveTims).getBody();

        // Act
        List<ActiveTim> data = ActiveTimService.getActiveTimsByWydotTim(wydotTims, timTypeId);

        // Assert
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals(aTim, data.get(0));
    }
}