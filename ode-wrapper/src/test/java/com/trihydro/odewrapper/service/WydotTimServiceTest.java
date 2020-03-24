package com.trihydro.odewrapper.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.mail.MessagingException;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.RestTemplateProvider;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.TimRsuService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mail.MailException;
import org.springframework.web.client.RestTemplate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ActiveTimService.class, TimRsuService.class, RsuService.class, RestTemplateProvider.class })
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

    @InjectMocks
    WydotTimService uut;

    @Before
    public void setup() {
        PowerMockito.mockStatic(ActiveTimService.class);
        PowerMockito.mockStatic(TimRsuService.class);
        PowerMockito.mockStatic(RsuService.class);
        PowerMockito.mockStatic(RestTemplateProvider.class);

        when(RestTemplateProvider.GetRestTemplate()).thenReturn(restTemplate);

        String[] addresses = new String[1];
        addresses[0] = "unit@test.com";
        Mockito.when(mockBasicConfiguration.getAlertAddresses()).thenReturn(addresses);
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
            aTim.setMilepostStart(1d);
            aTim.setMilepostStop(2d);
            aTim.setTimId(-10l);
            aTim2.setMilepostStart(3d);
            aTim2.setMilepostStop(4d);
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
        when(TimRsuService.getTimRsusByTimId(-10l)).thenReturn(new ArrayList<>());
        when(TimRsuService.getTimRsusByTimId(-20l)).thenReturn(timRsus);
        ArrayList<WydotRsu> allRsus = new ArrayList<>();
        WydotRsu wydotRsu = new WydotRsu();
        wydotRsu.setRsuId(-10);
        wydotRsu.setRsuIndex(-1);
        allRsus.add(wydotRsu);
        Mockito.when(RsuService.selectAll()).thenReturn(allRsus);

        // Act
        uut.deleteTimsFromRsusAndSdx(activeTims);

        // Assert
        PowerMockito.verifyStatic(ActiveTimService.class);
        ActiveTimService.deleteActiveTim(-1l);
        PowerMockito.verifyStatic(ActiveTimService.class);
        ActiveTimService.deleteActiveTim(-2l);
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
        Mockito.when(mockSdwService.deleteSdxDataBySatRecordId(anyList())).thenReturn(sdxDelResults);

        // Act
        uut.deleteTimsFromRsusAndSdx(activeTims);

        // Assert
        verify(mockEmailHelper).SendEmail(mockBasicConfiguration.getAlertAddresses(), null, subject, body,
                mockBasicConfiguration.getMailPort(), mockBasicConfiguration.getMailHost(),
                mockBasicConfiguration.getFromEmail());
        PowerMockito.verifyStatic(ActiveTimService.class);
        List<Long> delIds = new ArrayList<Long>();
        delIds.add(-2l);
        ActiveTimService.deleteActiveTimsById(delIds);
        PowerMockito.verifyStatic(TimRsuService.class, never());
        TimRsuService.getTimRsusByTimId(isA(Long.class));
    }

    @Test
    public void deleteTimsFromRsusAndSdx_SdxNullValueInMap() throws MailException, MessagingException {
        // Arrange
        List<ActiveTim> activeTims = getActiveTims(true);
        HashMap<Integer, Boolean> sdxDelResults = new HashMap<>();
        sdxDelResults.put(-1032012897, null);
        Mockito.when(mockSdwService.deleteSdxDataBySatRecordId(anyList())).thenReturn(sdxDelResults);

        // Act
        uut.deleteTimsFromRsusAndSdx(activeTims);

        // Assert
        PowerMockito.verifyStatic(ActiveTimService.class);
        List<Long> delIds = new ArrayList<Long>();
        delIds.add(-1l);
        delIds.add(-2l);
        ActiveTimService.deleteActiveTimsById(delIds);
        PowerMockito.verifyStatic(TimRsuService.class, never());
        TimRsuService.getTimRsusByTimId(isA(Long.class));
    }
}