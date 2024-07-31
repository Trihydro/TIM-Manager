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
import com.trihydro.library.helpers.RegionNameTrimmer;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.EmailProps;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.OdeProps;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTravelerInputData;

import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

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
    @Mock
    ActiveTimHoldingService mockActiveTimHoldingService;
    @Mock
    RegionNameTrimmer mockRegionNameTrimmer;

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

    private OdeTravelerInformationMessage getMockOdeTravelerInformationMessage() {
        String timJson = "{\"msgCnt\":\"1\",\"timeStamp\":\"2017-08-03T22:25:36.297Z\",\"urlB\":\"null\",\"packetID\":\"EC9C236B0000000000\",\"dataframes\":[{\"startDateTime\":\"2017-08-02T22:25:00.000Z\",\"durationTime\":1,\"sspTimRights\":\"0\",\"frameType\":\"advisory\",\"msgId\":{\"roadSignID\":{\"position\":{\"latitude\":\"41.678473\",\"longitude\":\"-108.782775\",\"elevation\":\"917.1432\"},\"viewAngle\":\"1010101010101010\",\"mutcdCode\":\"warning\",\"crc\":\"0000\"}},\"priority\":\"0\",\"sspLocationRights\":\"3\",\"regions\":[{\"name\":\"Testing TIM\",\"regulatorID\":\"0\",\"segmentID\":\"33\",\"anchorPosition\":{\"latitude\":\"41.2500807\",\"longitude\":\"-111.0093847\",\"elevation\":\"2020.6969900289998\"},\"laneWidth\":\"7\",\"directionality\":\"3\",\"closedPath\":\"false\",\"description\":\"path\",\"path\":{\"scale\":\"0\",\"type\":\"ll\",\"nodes\":[{\"nodeLong\":\"0.0030982\",\"nodeLat\":\"0.0014562\",\"delta\":\"node-LL3\"},{\"nodeLong\":\"-111.0093847\",\"nodeLat\":\"41.2500807\",\"delta\":\"node-LatLon\"}]},\"direction\":\"0000000000001010\"}],\"sspMsgTypes\":\"2\",\"sspMsgContent\":\"3\",\"content\":\"Advisory\",\"items\":[\"125\",\"some text\",\"250\",\"'98765\"],\"url\":\"null\"}]}";
        Gson gson = new Gson();
        OdeTravelerInformationMessage mockOdeTravelerInformationMessage = gson.fromJson(timJson, OdeTravelerInformationMessage.class);
        return mockOdeTravelerInformationMessage;
    }

    private WydotOdeTravelerInformationMessage getMockWydotOdeTravelerInformationMessage() {
        OdeTravelerInformationMessage mockOdeTravelerInformationMessage = getMockOdeTravelerInformationMessage();
        WydotOdeTravelerInformationMessage mockWydotOdeTravelerInformationMessage = new WydotOdeTravelerInformationMessage();
        mockWydotOdeTravelerInformationMessage.setMsgCnt(mockOdeTravelerInformationMessage.getMsgCnt());
        mockWydotOdeTravelerInformationMessage.setTimeStamp(mockOdeTravelerInformationMessage.getTimeStamp());
        mockWydotOdeTravelerInformationMessage.setPacketID(mockOdeTravelerInformationMessage.getPacketID());
        mockWydotOdeTravelerInformationMessage.setDataframes(mockOdeTravelerInformationMessage.getDataframes());
        mockWydotOdeTravelerInformationMessage.setUrlB(mockOdeTravelerInformationMessage.getUrlB());
        mockWydotOdeTravelerInformationMessage.setAsnDataFrames(mockOdeTravelerInformationMessage.getAsnDataFrames());
        mockWydotOdeTravelerInformationMessage.setRsuIndex(0);
        return mockWydotOdeTravelerInformationMessage;
    }

    private List<Milepost> getMockMileposts() {
        List<Milepost> mileposts = new ArrayList<>();
        
        Milepost milepost1 = new Milepost();
        milepost1.setCommonName("I-80");
        milepost1.setMilepost(0.0);
        milepost1.setDirection("D");
        milepost1.setLatitude(BigDecimal.valueOf(41.678473));
        milepost1.setLongitude(BigDecimal.valueOf(-108.782775));
        mileposts.add(milepost1);

        Milepost milepost2 = new Milepost();
        milepost2.setCommonName("I-80");
        milepost2.setMilepost(1.0);
        milepost2.setDirection("D");
        milepost2.setLatitude(BigDecimal.valueOf(41.678473));
        milepost2.setLongitude(BigDecimal.valueOf(-108.782775));
        mileposts.add(milepost2);

        return mileposts;
    }

    public void setupRestTemplate() {
        doReturn(mockRestTemplate).when(mockRestTemplateProvider).GetRestTemplate_NoErrors();
    }

    @Test
    public void sendTimToSDW_MultipleActiveSatTimsFound() {
        // Arrange
        List<ActiveTim> activeSatTims = getActiveTims(true);
        when(mockActiveTimService.getActiveTimsByClientIdDirection(any(), any(), any())).thenReturn(activeSatTims);
        WydotTim wydotTim = new WydotTim();
        wydotTim.setClientId("testclientid");
        wydotTim.setDirection("D");
        WydotTravelerInputData timToSend = new WydotTravelerInputData();
        timToSend.setTim(getMockOdeTravelerInformationMessage());
        ServiceRequest serviceRequest = new ServiceRequest();
        timToSend.setRequest(serviceRequest);
        String regionNamePrev = "regionNamePrev";
        TimType timType = new TimType();
        Integer pk = 0;
        Coordinate endPoint = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        List<Milepost> reducedMileposts = getMockMileposts();
        WydotOdeTravelerInformationMessage mockExistingSatTim = getMockWydotOdeTravelerInformationMessage();
        when(mockTimService.getTim(any())).thenReturn(mockExistingSatTim);
        RestTemplate mockRestTemplate = new RestTemplate();
        when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

        // Act
        uut.sendTimToSDW(wydotTim, timToSend, regionNamePrev, timType, pk, endPoint, reducedMileposts);

        // Assert
        verify(mockUtility).logWithDate("Multiple active SAT TIMs found for client testclientid and direction D. Expected zero or one. Using the first one found.");
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