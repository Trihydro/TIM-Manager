package com.trihydro.library.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.EmailProps;
import com.trihydro.library.model.OdeProps;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.model.WydotRsu;

@ExtendWith(MockitoExtension.class)
public class WydotTimServiceTest {

    @Mock
    EmailProps mockEmailProps;
    @Mock
    OdeProps mockOdeProps;
    @Mock
    TimGenerationProps mockGenProps;
    @Mock
    EmailHelper mockEmailHelper;
    @Mock
    TimTypeService mockTimTypeService;
    @Mock
    SdwService mockSdwService;
    @Mock
    Utility mockUtility;
    @Mock
    OdeService mockOdeService;
    @Mock
    RestTemplate restTemplate;
    @Mock
    ActiveTimService mockActiveTimService;
    @Mock
    TimRsuService mockTimRsuService;
    @Mock
    RestTemplateProvider mockRestTemplateProvider;
    @Mock
    RestTemplate mockRestTemplate;
    @Mock
    RsuService mockRsuService;
    @Mock
    TimService mockTimService;

    @InjectMocks
    WydotTimService uut;

    public void setupAlertAddresses() {
        String[] addresses = new String[1];
        addresses[0] = "unit@test.com";
        when(mockEmailProps.getAlertAddresses()).thenReturn(addresses);
    }

    private List<ActiveTim> getActiveTims(boolean isSat) {
        List<ActiveTim> activeTims = new ArrayList<ActiveTim>();
        ActiveTim aTim = new ActiveTim();
        ActiveTim aTim2 = new ActiveTim();
        aTim.setActiveTimId(-1l);
        aTim2.setActiveTimId(-2l);
        if (isSat) {
            aTim.setSatRecordId("C27CBB9F");
            aTim2.setSatRecordId("86E03786");
        } else {
            aTim.setStartPoint(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2)));
            aTim.setEndPoint(new Coordinate(BigDecimal.valueOf(3), BigDecimal.valueOf(4)));
            aTim.setTimId(-10l);
            aTim2.setStartPoint(new Coordinate(BigDecimal.valueOf(5), BigDecimal.valueOf(6)));
            aTim2.setEndPoint(new Coordinate(BigDecimal.valueOf(7), BigDecimal.valueOf(8)));
            aTim2.setTimId(-20l);
        }
        activeTims.add(aTim);
        activeTims.add(aTim2);

        return activeTims;
    }


    public void setupRestTemplate() {
        doReturn(mockRestTemplate).when(mockRestTemplateProvider).GetRestTemplate_NoErrors();
    }

    @Test
    public void deleteTimsFromRsusAndSdx_Rsu() {
        // Arrange
        List<ActiveTim> activeTims = getActiveTims(false);
        List<TimRsu> timRsus = new ArrayList<>();
        TimRsu timRsu = new TimRsu();
        timRsu.setRsuId(-10l);
        timRsu.setRsuIndex(-1);
        timRsus.add(timRsu);
        when(mockTimRsuService.getTimRsusByTimId(-10l)).thenReturn(new ArrayList<>());
        when(mockTimRsuService.getTimRsusByTimId(-20l)).thenReturn(timRsus);
        ArrayList<WydotRsu> allRsus = new ArrayList<>();
        WydotRsu wydotRsu = new WydotRsu();
        wydotRsu.setRsuId(-10);
        wydotRsu.setRsuIndex(-1);
        allRsus.add(wydotRsu);
        when(mockRsuService.selectAll()).thenReturn(allRsus);
        doReturn(true).when(mockActiveTimService).deleteActiveTim(any());
        setupRestTemplate();
        doReturn(new ResponseEntity<String>("ok", HttpStatus.OK)).when(mockRestTemplate).exchange(any(String.class),
                any(HttpMethod.class), Mockito.<HttpEntity<?>>any(), Mockito.<Class<String>>any());

        // Act
        var result = uut.deleteTimsFromRsusAndSdx(activeTims);

        // Assert
        verify(mockActiveTimService).deleteActiveTim(-1l);
        verify(mockActiveTimService).deleteActiveTim(-2l);
        Assertions.assertEquals(result.getSuccessfulRsuDeletions().size(), 2);
    }

    @Test
    public void deleteTimsFromRsusAndSdx_Sdx() throws MailException, MessagingException {
        // Arrange
        setupAlertAddresses();
        List<ActiveTim> activeTims = getActiveTims(true);
        HashMap<Integer, Boolean> sdxDelResults = new HashMap<>();
        sdxDelResults.put(-1032012897, false);
        sdxDelResults.put(-2032126074, true);
        String subject = "SDX Delete Fail";
        String body = "The following recordIds failed to delete from the SDX: -1032012897";
        when(mockSdwService.deleteSdxDataBySatRecordId(anyList())).thenReturn(sdxDelResults);
        doReturn(true).when(mockActiveTimService).deleteActiveTimsById(any());

        // Act
        var result = uut.deleteTimsFromRsusAndSdx(activeTims);

        // Assert
        verify(mockEmailHelper).SendEmail(mockEmailProps.getAlertAddresses(), subject, body);
        List<Long> delIds = new ArrayList<Long>();
        delIds.add(-2l);
        verify(mockActiveTimService).deleteActiveTimsById(delIds);
        verify(mockTimRsuService, never()).getTimRsusByTimId(isA(Long.class));
        Assertions.assertEquals(1, result.getSuccessfulSatelliteDeletions().size());
        Assertions.assertEquals(body, result.getSatelliteErrorSummary());
    }

    @Test
    public void deleteTimsFromRsusAndSdx_SdxNullValueInMap() throws MailException, MessagingException {
        // Arrange
        List<ActiveTim> activeTims = getActiveTims(true);
        HashMap<Integer, Boolean> sdxDelResults = new HashMap<>();
        sdxDelResults.put(-1032012897, null);
        when(mockSdwService.deleteSdxDataBySatRecordId(anyList())).thenReturn(sdxDelResults);

        // Act
        uut.deleteTimsFromRsusAndSdx(activeTims);

        // Assert
        List<Long> delIds = new ArrayList<Long>();
        delIds.add(-1l);
        delIds.add(-2l);
        verify(mockActiveTimService).deleteActiveTimsById(delIds);
    }

    @Test
    public void deleteTimsFromRsusAndSdx_Exceptions() {
        // Arrange
        List<ActiveTim> activeTims = getActiveTims(false);
        List<TimRsu> timRsus = new ArrayList<>();
        TimRsu timRsu = new TimRsu();
        timRsu.setRsuId(-10l);
        timRsu.setRsuIndex(-1);
        timRsus.add(timRsu);
        when(mockTimRsuService.getTimRsusByTimId(-10l)).thenReturn(new ArrayList<>());
        when(mockTimRsuService.getTimRsusByTimId(-20l)).thenReturn(timRsus);
        ArrayList<WydotRsu> allRsus = new ArrayList<>();
        WydotRsu wydotRsu = new WydotRsu();
        wydotRsu.setRsuId(-10);
        wydotRsu.setRsuIndex(-1);
        allRsus.add(wydotRsu);
        when(mockRsuService.selectAll()).thenReturn(allRsus);
        doReturn(true).when(mockActiveTimService).deleteActiveTim(any());
        setupRestTemplate();
        doReturn(new ResponseEntity<String>("fail", HttpStatus.I_AM_A_TEAPOT)).when(mockRestTemplate).exchange(
                any(String.class), any(HttpMethod.class), Mockito.<HttpEntity<?>>any(), Mockito.<Class<String>>any());

        // Act
        var result = uut.deleteTimsFromRsusAndSdx(activeTims);

        // Assert
        verify(mockActiveTimService).deleteActiveTim(-1l);
        verify(mockActiveTimService).deleteActiveTim(-2l);
        List<String> timRsuJson = new ArrayList<String>();
        Gson gson = new Gson();
        for (TimRsu tr : timRsus) {
            timRsuJson.add(gson.toJson(tr));
        }
        String jsonVal = String.join(",", timRsuJson);
        Assertions.assertEquals(jsonVal, result.getRsuErrorSummary());
    }

    @Test
    public void setBufferItisCodes_leftClosed() {
        // Arrange
        String action = "leftClosed";

        // Act
        Integer[] result = uut.setBufferItisCodes(action);

        // Assert
        Assertions.assertEquals(2, result.length);
        Assertions.assertEquals(8195, result[0]);
        Assertions.assertEquals(771, result[1]);
    }

    @Test
    public void setBufferItisCodes_rightClosed() {
        // Arrange
        String action = "rightClosed";

        // Act
        Integer[] result = uut.setBufferItisCodes(action);

        // Assert
        Assertions.assertEquals(2, result.length);
        Assertions.assertEquals(8196, result[0]);
        Assertions.assertEquals(771, result[1]);
    }

    @Test
    public void setBufferItisCodes_workers() {
        // Arrange
        String action = "workers";

        // Act
        Integer[] result = uut.setBufferItisCodes(action);

        // Assert
        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(6952, result[0]);
    }

    @Test
    public void setBufferItisCodes_surfaceGravel() {
        // Arrange
        String action = "surfaceGravel";

        // Act
        Integer[] result = uut.setBufferItisCodes(action);

        // Assert
        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(5933, result[0]);
    }

    @Test
    public void setBufferItisCodes_surfaceMilled() {
        // Arrange
        String action = "surfaceMilled";

        // Act
        Integer[] result = uut.setBufferItisCodes(action);

        // Assert
        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(6017, result[0]);
    }

    @Test
    public void setBufferItisCodes_surfaceDirt() {
        // Arrange
        String action = "surfaceDirt";

        // Act
        Integer[] result = uut.setBufferItisCodes(action);

        // Assert
        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(6016, result[0]);
    }

    @Test
    public void setBufferItisCodes_delay_x() {
        // Arrange
        String action = "delay_10";

        // Act
        Integer[] result = uut.setBufferItisCodes(action);

        // Assert
        Assertions.assertEquals(3, result.length);
        Assertions.assertEquals(1537, result[0]);
        Assertions.assertEquals(12554, result[1]);
        Assertions.assertEquals(8728, result[2]);
    }

    @Test
    public void setBufferItisCodes_prepareStop() {
        // Arrange
        String action = "prepareStop";

        // Act
        Integer[] result = uut.setBufferItisCodes(action);

        // Assert
        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(7186, result[0]);
    }

    @Test
    public void setBufferItisCodes_reduceSpeed_x() {
        // Arrange
        String action = "reduceSpeed_20";

        // Act
        Integer[] result = uut.setBufferItisCodes(action);

        // Assert
        Assertions.assertEquals(3, result.length);
        Assertions.assertEquals(7443, result[0]);
        Assertions.assertEquals(12564, result[1]);
        Assertions.assertEquals(8720, result[2]);
    }

    @Test
    public void setBufferItisCodes_unrecognizedAction() {
        // Arrange
        String action = "banana";

        // Act
        Integer[] result = uut.setBufferItisCodes(action);

        // Assert
        Assertions.assertEquals(null, result);
    }

}