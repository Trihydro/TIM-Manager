package com.trihydro.library.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trihydro.library.model.ActiveRsuTimQueryModel;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.CVRestServiceProps;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.WydotTim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

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
    @Mock
    private CVRestServiceProps mockConfig;

    private Long timTypeId = -1l;
    private List<WydotTim> wydotTims;
    private ActiveTim[] aTims;
    private ActiveTim aTim;
    private TimUpdateModel[] tumArr;
    private String baseUrl = "baseUrl";

    @InjectMocks
    private ActiveTimService uut;

    @BeforeEach
    public void setupSubTest() throws SQLException {
        doReturn(baseUrl).when(mockConfig).getCvRestService();
    }

    private void setupBooleanReturn() {
        doReturn(true).when(mockResponseEntityBoolean).getBody();
    }

    private void setupIntegerArrayReturn() {
        Integer[] intArray = new Integer[3];
        intArray[0] = 0;
        intArray[1] = 1;
        intArray[2] = 2;
        when(mockResponseEntityIntegerArray.getBody()).thenReturn(intArray);
    }

    private void setupActiveTimArrayReturn() {
        aTims = new ActiveTim[1];
        aTim = new ActiveTim();
        aTim.setActiveTimId(-1l);
        aTims[0] = aTim;
        doReturn(aTims).when(mockResponseEntityActiveTims).getBody();
    }

    private void setupActiveTimReturn() {
        aTim = new ActiveTim();
        aTim.setActiveTimId(-1l);
        doReturn(aTim).when(mockResponseEntityActiveTim).getBody();
    }

    private void setupTUMReturn() {
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
        wydotTim.setDirection("d");
        wydotTim.setClientId("unit_test_id1");
        wydotTims.add(wydotTim);
        wydotTim = new WydotTim();
        wydotTim.setDirection("i");
        wydotTim.setClientId("unit_test_id2");
        wydotTims.add(wydotTim);
    }

    @Test
    public void updateActiveTim_SatRecordId() {
        // Arrange
        setupBooleanReturn();
        Long activeTimId = -1l;
        String satRecordId = "asdf";
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("%s/active-tim/update-sat-record-id/%d/%s", baseUrl, activeTimId, satRecordId);
        when(mockRestTemplate.exchange(url, HttpMethod.PUT, entity, Boolean.class))
                .thenReturn(mockResponseEntityBoolean);

        // Act
        Boolean data = uut.updateActiveTim_SatRecordId(activeTimId, satRecordId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.PUT, entity, Boolean.class);
        Assertions.assertTrue(data, "Update failed when should have succeeded");
    }

    @Test
    public void addItisCodesToActiveTim() {
        // Arrange
        setupIntegerArrayReturn();
        ActiveTim activeTim = new ActiveTim();
        activeTim.setActiveTimId(-1l);
        String url = String.format("%s/active-tim/itis-codes/%d", baseUrl, activeTim.getActiveTimId());
        when(mockRestTemplate.getForEntity(url, Integer[].class)).thenReturn(mockResponseEntityIntegerArray);

        // Act
        uut.addItisCodesToActiveTim(activeTim);

        // Assert
        verify(mockRestTemplate).getForEntity(url, Integer[].class);
        Assertions.assertEquals(3, activeTim.getItisCodes().size());
        Assertions.assertEquals(Integer.valueOf(0), activeTim.getItisCodes().get(0));
        Assertions.assertEquals(Integer.valueOf(1), activeTim.getItisCodes().get(1));
        Assertions.assertEquals(Integer.valueOf(2), activeTim.getItisCodes().get(2));
    }

    @Test
    public void deleteActiveTim() {
        // Arrange
        setupBooleanReturn();
        Long activeTimId = -1l;
        String url = String.format("%s/active-tim/delete-id/%d", baseUrl, activeTimId);
        HttpEntity<String> entity = getEntity(null, String.class);
        when(mockRestTemplate.exchange(url, HttpMethod.DELETE, entity, Boolean.class))
                .thenReturn(mockResponseEntityBoolean);

        // Act
        boolean data = uut.deleteActiveTim(activeTimId);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.DELETE, entity, Boolean.class);
        Assertions.assertTrue(data, "Reported failure when success");
    }

    @Test
    public void deleteActiveTimsById() throws SQLException {
        // Arrange
        setupBooleanReturn();
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1l);
        activeTimIds.add(-2l);
        HttpEntity<List<Long>> entity = new HttpEntity<List<Long>>(activeTimIds, getDefaultHeaders());
        when(mockRestTemplate.exchange(baseUrl + "/active-tim/delete-ids", HttpMethod.DELETE, entity, Boolean.class))
                .thenReturn(mockResponseEntityBoolean);

        // Act
        boolean success = uut.deleteActiveTimsById(activeTimIds);

        // Assert
        Assertions.assertTrue(success);
    }

    @Test
    public void getActiveTimIndicesByRsu() {
        // Arrange
        setupIntegerArrayReturn();
        String rsuTarget = "10.10.10.10";
        String url = String.format("%s/active-tim/indices-rsu/%s", baseUrl, rsuTarget);
        when(mockRestTemplate.getForEntity(url, Integer[].class)).thenReturn(mockResponseEntityIntegerArray);

        // Act
        List<Integer> data = uut.getActiveTimIndicesByRsu(rsuTarget);

        // Assert
        verify(mockRestTemplate).getForEntity(url, Integer[].class);
        Assertions.assertEquals(3, data.size());
        Assertions.assertEquals(Integer.valueOf(0), data.get(0));
        Assertions.assertEquals(Integer.valueOf(1), data.get(1));
        Assertions.assertEquals(Integer.valueOf(2), data.get(2));
    }

    @Test
    public void getActiveTimsByWydotTim() throws SQLException {
        // Arrange
        setupWydotTims();
        setupActiveTimArrayReturn();
        HttpEntity<List<WydotTim>> entity = new HttpEntity<List<WydotTim>>(wydotTims, getDefaultHeaders());
        when(mockRestTemplate.exchange(baseUrl + "/active-tim/get-by-wydot-tim/" + timTypeId, HttpMethod.POST, entity,
                ActiveTim[].class)).thenReturn(mockResponseEntityActiveTims);

        // Act
        List<ActiveTim> data = uut.getActiveTimsByWydotTim(wydotTims, timTypeId);

        // Assert
        Assertions.assertNotNull(data);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(aTim, data.get(0));
    }

    @Test
    public void getActiveTimsByClientIdDirection() {
        // Arrange
        setupActiveTimArrayReturn();
        String clientId = "clientId";
        String direction = "westward";
        String url = String.format("%s/active-tim/client-id-direction/%s/%d/%s", baseUrl, clientId, timTypeId,
                direction);
        when(mockRestTemplate.getForEntity(url, ActiveTim[].class)).thenReturn(mockResponseEntityActiveTims);

        // Act
        List<ActiveTim> data = uut.getActiveTimsByClientIdDirection(clientId, timTypeId, direction);

        // Assert
        verify(mockRestTemplate).getForEntity(url, ActiveTim[].class);
        Assertions.assertEquals(Arrays.asList(aTims), data);
    }

    @Test
    public void getExpiredActiveTims() {
        // Arrange
        setupActiveTimArrayReturn();
        String url = String.format("%s/active-tim/expired", baseUrl);
        when(mockRestTemplate.getForEntity(url, ActiveTim[].class)).thenReturn(mockResponseEntityActiveTims);

        // Act
        List<ActiveTim> data = uut.getExpiredActiveTims();

        // Assert
        verify(mockRestTemplate).getForEntity(url, ActiveTim[].class);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(Arrays.asList(aTims), data);
    }

    @Test
    public void getActivesTimByType() {
        // Arrange
        setupActiveTimArrayReturn();
        String url = String.format("%s/active-tim/tim-type-id/%d", baseUrl, timTypeId);
        when(mockRestTemplate.getForEntity(url, ActiveTim[].class)).thenReturn(mockResponseEntityActiveTims);

        // Act
        List<ActiveTim> data = uut.getActivesTimByType(timTypeId);

        // Assert
        verify(mockRestTemplate).getForEntity(url, ActiveTim[].class);
        Assertions.assertEquals(Arrays.asList(aTims), data);
    }

    @Test
    public void getActiveRsuTim() {
        // Arrange
        setupActiveTimReturn();
        String clientId = "clientId";
        String direction = "westward";
        String ipv4Address = "10.10.10.10";
        String url = String.format("%s/active-tim/active-rsu-tim", baseUrl);
        ActiveRsuTimQueryModel artqm = new ActiveRsuTimQueryModel(direction, clientId, ipv4Address);
        HttpEntity<ActiveRsuTimQueryModel> entity = getEntity(artqm, ActiveRsuTimQueryModel.class);
        when(mockRestTemplate.exchange(url, HttpMethod.POST, entity, ActiveTim.class))
                .thenReturn(mockResponseEntityActiveTim);

        // Act
        ActiveTim data = uut.getActiveRsuTim(artqm);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.POST, entity, ActiveTim.class);
        Assertions.assertEquals(aTim, data);
    }

    @Test
    public void getExpiringActiveTims() {
        // Arrange
        setupTUMReturn();
        String url = String.format("%s/active-tim/expiring", baseUrl);
        when(mockRestTemplate.getForEntity(url, TimUpdateModel[].class))
                .thenReturn(mockResponseEntityTimUpdateModelArray);

        // Act
        List<TimUpdateModel> data = uut.getExpiringActiveTims();

        // Assert
        verify(mockRestTemplate).getForEntity(url, TimUpdateModel[].class);
        Assertions.assertEquals(1, data.size());
        Assertions.assertEquals(Arrays.asList(tumArr), data);
    }

    @Test
    public void getActiveTimsMissingItisCodes() throws SQLException {
        // Arrange
        TimUpdateModel[] tums = new TimUpdateModel[1];
        TimUpdateModel tum = new TimUpdateModel();
        tum.setTimId(1l);
        tum.setDirection("both");
        tum.setRoute("I 80");
        tum.setClientId("123");
        tum.setSatRecordId("HEX");
        tum.setActiveTimId(1l);
        tums[0] = tum;

        when(mockRestTemplate.getForEntity(baseUrl + "/active-tim/missing-itis", TimUpdateModel[].class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(tums);
        // Act
        List<ActiveTim> ats = uut.getActiveTimsMissingItisCodes();

        // Assert
        Assertions.assertEquals(1, ats.size());
        ActiveTim tim = ats.get(0);
        Assertions.assertEquals(tum, tim);
    }

    @Test
    public void getActiveTimsNotSent() {
        // Arrange
        TimUpdateModel[] tums = new TimUpdateModel[1];
        TimUpdateModel tum = new TimUpdateModel();
        tum.setTimId(1l);
        tum.setDirection("both");
        tum.setRoute("I 80");
        tum.setClientId("123");
        tum.setSatRecordId("HEX");
        tum.setActiveTimId(1l);
        tums[0] = tum;
        when(mockRestTemplate.getForEntity(baseUrl + "/active-tim/not-sent", TimUpdateModel[].class))
                .thenReturn(mockResponseEntity);
        when(mockResponseEntity.getBody()).thenReturn(tums);

        // Act
        List<ActiveTim> ats = uut.getActiveTimsNotSent();

        // Assert
        Assertions.assertEquals(1, ats.size());
        ActiveTim tim = ats.get(0);
        Assertions.assertEquals(tum, tim);
    }

    @Test
    public void getActiveTimsForSDX_success() {
        // Arrange
        setupActiveTimArrayReturn();
        when(mockRestTemplate.getForEntity(baseUrl + "/active-tim/all-sdx", ActiveTim[].class))
                .thenReturn(mockResponseEntityActiveTims);

        // Act
        List<ActiveTim> result = uut.getActiveTimsForSDX();

        // Assert
        Assertions.assertEquals(aTims.length, result.size());
        Assertions.assertEquals(aTim, result.get(0));
    }

    @Test
    public void getActiveTimsForSDX_throwsError() {
        // Arrange
        when(mockRestTemplate.getForEntity(baseUrl + "/active-tim/all-sdx", ActiveTim[].class))
                .thenThrow(new RestClientException("timeout"));

        // Act
        Assertions.assertThrows(RestClientException.class, () -> {
            uut.getActiveTimsForSDX();
        });
    }

    @Test
    public void getActiveTimsWithItisCodesWithExclusions_success() {
        // Arrange
        setupActiveTimArrayReturn();
        when(mockRestTemplate.getForEntity(baseUrl + "/active-tim/all-with-itis?excludeVslAndParking=true",
                ActiveTim[].class)).thenReturn(mockResponseEntityActiveTims);

        // Act
        List<ActiveTim> result = uut.getActiveTimsWithItisCodes(true);

        // Assert
        Assertions.assertEquals(aTims.length, result.size());
        Assertions.assertEquals(aTim, result.get(0));
    }

    @Test
    public void getActiveTimsWithItisCodes_success() {
        // Arrange
        setupActiveTimArrayReturn();
        when(mockRestTemplate.getForEntity(baseUrl + "/active-tim/all-with-itis?excludeVslAndParking=false",
                ActiveTim[].class)).thenReturn(mockResponseEntityActiveTims);

        // Act
        List<ActiveTim> result = uut.getActiveTimsWithItisCodes(false);

        // Assert
        Assertions.assertEquals(aTims.length, result.size());
        Assertions.assertEquals(aTim, result.get(0));
    }

    @Test
    public void getActiveTimsWithItisCodes_throwsError() {
        // Arrange
        when(mockRestTemplate.getForEntity(anyString(), eq(ActiveTim[].class)))
                .thenThrow(new RestClientException("timeout"));

        // Act
        Assertions.assertThrows(RestClientException.class, () -> {
            uut.getActiveTimsWithItisCodes(true);
        });
    }

    @Test
    public void getActiveTimByPacketIdStartDate_SUCCESS() {
        // Arrange
        setupActiveTimReturn();
        String packetID = "packetId";
        String date = "2020-10-20T16:26:07.000Z";
        String url = String.format("%s/active-tim/packetid-startdate/%s/%s", baseUrl, packetID, date);

        when(mockRestTemplate.getForEntity(url, ActiveTim.class)).thenReturn(mockResponseEntityActiveTim);

        // Act
        ActiveTim data = uut.getActiveTimByPacketIdStartDate(packetID, date);

        // Assert
        verify(mockRestTemplate).getForEntity(url, ActiveTim.class);
        Assertions.assertEquals(aTim, data);
    }

    @Test
    public void updateActiveTimExpiration_SUCCESS() {
        // Arrange
        setupBooleanReturn();
        Long activeTimId = -1l;
        String expDate = "2020-10-20T16:26:07.000Z";
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("%s/active-tim/update-expiration/%d/%s", baseUrl, activeTimId, expDate);
        when(mockRestTemplate.exchange(url, HttpMethod.PUT, entity, Boolean.class))
                .thenReturn(mockResponseEntityBoolean);

        // Act
        Boolean data = uut.updateActiveTimExpiration(activeTimId, expDate);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.PUT, entity, Boolean.class);
        Assertions.assertTrue(data, "Update failed when should have succeeded");
    }

    @Test
    public void updateActiveTimExpiration_FAIL() {
        // Arrange
        doReturn(false).when(mockResponseEntityBoolean).getBody();
        Long activeTimId = -1l;
        String expDate = "2020-10-20T16:26:07.000Z";
        HttpEntity<String> entity = getEntity(null, String.class);
        String url = String.format("%s/active-tim/update-expiration/%d/%s", baseUrl, activeTimId, expDate);
        when(mockRestTemplate.exchange(url, HttpMethod.PUT, entity, Boolean.class))
                .thenReturn(mockResponseEntityBoolean);

        // Act
        Boolean data = uut.updateActiveTimExpiration(activeTimId, expDate);

        // Assert
        verify(mockRestTemplate).exchange(url, HttpMethod.PUT, entity, Boolean.class);
        Assertions.assertFalse(data, "Update succeeded when should have failed");
    }
}