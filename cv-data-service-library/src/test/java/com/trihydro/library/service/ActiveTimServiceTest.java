package com.trihydro.library.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotTim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RunWith(PowerMockRunner.class)
public class ActiveTimServiceTest extends BaseServiceTest {
    @Mock
    private ResponseEntity<TimUpdateModel[]> mockResponseEntity;
    @Mock
    private ResponseEntity<Boolean> mockResponseEntityBoolean;
    @Mock
    private ResponseEntity<ActiveTim[]> mockResponseEntityActiveTims;
    @Mock
    private ResponseEntity<Integer[]> mockResponseEntityIntegerArray;
    @Mock
    private ResponseEntity<TimUpdateModel[]> mockResponseEntityTimUpdateModelArray;
    @Mock
    private ResponseEntity<ActiveTim> mockResponseEntityActiveTim;

    private Long timTypeId = -1l;
    private List<WydotTim> wydotTims;
    private ActiveTim[] aTims;
    private ActiveTim aTim;
    private TimUpdateModel[] tumArr;

    @Before
    public void setupSubTest() throws SQLException {
        doReturn(true).when(mockResponseEntityBoolean).getBody();
        Integer[] intArray = new Integer[3];
        intArray[0] = 0;
        intArray[1] = 1;
        intArray[2] = 2;
        when(mockResponseEntityIntegerArray.getBody()).thenReturn(intArray);

        aTims = new ActiveTim[1];
        aTim = new ActiveTim();
        aTim.setActiveTimId(-1l);
        aTims[0] = aTim;
        doReturn(aTims).when(mockResponseEntityActiveTims).getBody();
        doReturn(aTim).when(mockResponseEntityActiveTim).getBody();

        tumArr = new TimUpdateModel[1];
        TimUpdateModel tum = new TimUpdateModel();
        tum.setActiveTimId(-1l);
        tum.setClientId("testClient");
        tumArr[0] = tum;
        when(mockResponseEntityTimUpdateModelArray.getBody()).thenReturn(tumArr);
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
    public void insertActiveTim() {
        // Arrange
        ActiveTim activeTim = new ActiveTim();
        HttpEntity<ActiveTim> entity = getEntity(activeTim, ActiveTim.class);
        String url = "null/active-tim/add";
        when(mockRestTemplate.exchange(url, HttpMethod.POST, entity, Long.class)).thenReturn(mockResponseEntityLong);

        // Act
        Long data = ActiveTimService.insertActiveTim(activeTim);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, Long.class);
        assertEquals(new Long(1), data);
    }

    @Test
    public void updateActiveTim_SatRecordId() {
        // Arrange
        Long activeTimId = -1l;
        String satRecordId = "asdf";
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("null/active-tim/update-sat-record-id/%d/%s", activeTimId, satRecordId);
        when(mockRestTemplate.exchange(url, HttpMethod.PUT, entity, Boolean.class))
                .thenReturn(mockResponseEntityBoolean);

        // Act
        Boolean data = ActiveTimService.updateActiveTim_SatRecordId(activeTimId, satRecordId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.PUT, entity, Boolean.class);
        assertTrue("Update failed when should have succeeded", data);
    }

    @Test
    public void addItisCodesToActiveTim() {
        // Arrange
        ActiveTim activeTim = new ActiveTim();
        activeTim.setActiveTimId(-1l);
        String url = String.format("null/active-tim/itis-codes/%d", activeTim.getActiveTimId());
        when(mockRestTemplate.getForEntity(url, Integer[].class)).thenReturn(mockResponseEntityIntegerArray);

        // Act
        ActiveTimService.addItisCodesToActiveTim(activeTim);

        // Assert
        verify(mockRestTemplate).getForEntity(url, Integer[].class);
        assertEquals(3, activeTim.getItisCodes().size());
        assertEquals(new Integer(0), activeTim.getItisCodes().get(0));
        assertEquals(new Integer(1), activeTim.getItisCodes().get(1));
        assertEquals(new Integer(2), activeTim.getItisCodes().get(2));
    }

    @Test
    public void deleteActiveTim() {
        // Arrange
        Long activeTimId = -1l;
        String url = String.format("null/active-tim/delete-id/%d", activeTimId);
        HttpEntity<String> entity = getEntity(null, String.class);
        when(mockRestTemplate.exchange(url, HttpMethod.DELETE, entity, Boolean.class))
                .thenReturn(mockResponseEntityBoolean);

        // Act
        boolean data = ActiveTimService.deleteActiveTim(activeTimId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.DELETE, entity, Boolean.class);
        assertTrue("Reported failure when success", data);
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
    public void getActiveTimIndicesByRsu() {
        // Arrange
        String rsuTarget = "10.10.10.10";
        String url = String.format("null/active-tim/indices-rsu/%s", rsuTarget);
        when(mockRestTemplate.getForEntity(url, Integer[].class)).thenReturn(mockResponseEntityIntegerArray);

        // Act
        List<Integer> data = ActiveTimService.getActiveTimIndicesByRsu(rsuTarget);

        // Assert
        verify(mockRestTemplate).getForEntity(url, Integer[].class);
        assertEquals(3, data.size());
        assertEquals(new Integer(0), data.get(0));
        assertEquals(new Integer(1), data.get(1));
        assertEquals(new Integer(2), data.get(2));
    }

    @Test
    public void getActiveTimsByWydotTim() throws SQLException {
        // Arrange
        setupWydotTims();
        HttpEntity<List<WydotTim>> entity = new HttpEntity<List<WydotTim>>(wydotTims, getDefaultHeaders());
        when(mockRestTemplate.exchange("null/active-tim/get-by-wydot-tim/" + timTypeId, HttpMethod.PUT, entity,
                ActiveTim[].class)).thenReturn(mockResponseEntityActiveTims);

        // Act
        List<ActiveTim> data = ActiveTimService.getActiveTimsByWydotTim(wydotTims, timTypeId);

        // Assert
        assertNotNull(data);
        assertEquals(1, data.size());
        assertEquals(aTim, data.get(0));
    }

    @Test
    public void getActiveTimsByClientIdDirection() {
        // Arrange
        String clientId = "clientId";
        String direction = "westward";
        String url = String.format("null/active-tim/client-id-direction/%s/%d/%s", clientId, timTypeId, direction);
        when(mockRestTemplate.getForEntity(url, ActiveTim[].class)).thenReturn(mockResponseEntityActiveTims);

        // Act
        List<ActiveTim> data = ActiveTimService.getActiveTimsByClientIdDirection(clientId, timTypeId, direction);

        // Assert
        verify(mockRestTemplate).getForEntity(url, ActiveTim[].class);
        assertEquals(Arrays.asList(aTims), data);
    }

    @Test
    public void getExpiredActiveTims() {
        // Arrange
        String url = String.format("null/active-tim/expired");
        when(mockRestTemplate.getForEntity(url, TimUpdateModel[].class))
                .thenReturn(mockResponseEntityTimUpdateModelArray);

        // Act
        List<ActiveTim> data = ActiveTimService.getExpiredActiveTims();

        // Assert
        verify(mockRestTemplate).getForEntity(url, TimUpdateModel[].class);
        assertEquals(1, data.size());
        assertEquals(Arrays.asList(tumArr), data);
    }

    @Test
    public void getActivesTimByType() {
        // Arrange
        String url = String.format("null/active-tim/tim-type-id/%d", timTypeId);
        when(mockRestTemplate.getForEntity(url, ActiveTim[].class)).thenReturn(mockResponseEntityActiveTims);

        // Act
        List<ActiveTim> data = ActiveTimService.getActivesTimByType(timTypeId);

        // Assert
        verify(mockRestTemplate).getForEntity(url, ActiveTim[].class);
        assertEquals(Arrays.asList(aTims), data);
    }

    @Test
    public void getActiveRsuTim() {
        // Arrange
        String clientId = "clientId";
        String direction = "westward";
        String ipv4Address = "10.10.10.10";
        String url = String.format("null/active-tim/active-rsu-tim/%s/%s/%s", clientId, direction, ipv4Address);
        when(mockRestTemplate.getForEntity(url, ActiveTim.class)).thenReturn(mockResponseEntityActiveTim);

        // Act
        ActiveTim data = ActiveTimService.getActiveRsuTim(clientId, direction, ipv4Address);

        // Assert
        verify(mockRestTemplate).getForEntity(url, ActiveTim.class);
        assertEquals(aTim, data);
    }

    @Test
    public void getExpiringActiveTims() {
        // Arrange
        String url = String.format("null/active-tim/expiring");
        when(mockRestTemplate.getForEntity(url, TimUpdateModel[].class))
                .thenReturn(mockResponseEntityTimUpdateModelArray);

        // Act
        List<TimUpdateModel> data = ActiveTimService.getExpiringActiveTims();

        // Assert
        verify(mockRestTemplate).getForEntity(url, TimUpdateModel[].class);
        assertEquals(1, data.size());
        assertEquals(Arrays.asList(tumArr), data);
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
}