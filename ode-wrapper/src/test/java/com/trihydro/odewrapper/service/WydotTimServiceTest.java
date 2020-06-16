package com.trihydro.odewrapper.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.mail.MailException;
import org.springframework.web.client.RestTemplate;

@RunWith(StrictStubs.class)
public class WydotTimServiceTest {

    @Mock
    BasicConfiguration mockBasicConfiguration;
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
    RsuService mockRsuService;
    @Mock
    TimService mockTimService;

    @InjectMocks
    WydotTimService uut;

    @Before
    public void setup() {
        String[] addresses = new String[1];
        addresses[0] = "unit@test.com";
        when(mockBasicConfiguration.getAlertAddresses()).thenReturn(addresses);
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

        // Act
        var result = uut.deleteTimsFromRsusAndSdx(activeTims);

        // Assert
        verify(mockActiveTimService).deleteActiveTim(-1l);
        verify(mockActiveTimService).deleteActiveTim(-2l);
        assertEquals(result.getSuccessfulRsuDeletions().size(), 2);
    }

    @Test
    public void deleteTimsFromRsusAndSdx_Sdx() throws MailException, MessagingException {
        // Arrange
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
        verify(mockEmailHelper).SendEmail(mockBasicConfiguration.getAlertAddresses(), null, subject, body,
                mockBasicConfiguration.getMailPort(), mockBasicConfiguration.getMailHost(),
                mockBasicConfiguration.getFromEmail());
        List<Long> delIds = new ArrayList<Long>();
        delIds.add(-2l);
        verify(mockActiveTimService).deleteActiveTimsById(delIds);
        verify(mockTimRsuService, never()).getTimRsusByTimId(isA(Long.class));
        assertEquals(1, result.getSuccessfulSatelliteDeletions().size());
        assertEquals(body, result.getSatelliteErrorSummary());
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
}