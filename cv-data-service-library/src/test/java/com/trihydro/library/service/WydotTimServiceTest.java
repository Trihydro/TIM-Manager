package com.trihydro.library.service;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
import com.trihydro.library.helpers.CreateBaseTimUtil;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.RegionNameTrimmer;
import com.trihydro.library.helpers.SnmpHelper;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ContentEnum;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.EmailProps;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.MilepostBuffer;
import com.trihydro.library.model.OdeProps;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTravelerInputData;

import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.FrameType.TravelerInfoType;

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
    CreateBaseTimUtil mockCreateBaseTimUtil;
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
    MilepostService mockMilepostService;
    @Mock
    TimGenerationHelper mockTimGenerationHelper;
    @Mock
    RegionNameTrimmer mockRegionNameTrimmer;
    @Mock
    SnmpHelper mockSnmpHelper;
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
        String timJson = "{\"msgCnt\":\"1\",\"timeStamp\":\"2017-08-03T22:25:36.297Z\",\"urlB\":\"null\",\"packetID\":\"EC9C236B0000000000\",\"dataframes\":[{\"startDateTime\":\"2017-08-02T22:25:00.000Z\",\"durationTime\":1,\"doNotUse1\":\"0\",\"frameType\":\"advisory\",\"msgId\":{\"roadSignID\":{\"position\":{\"latitude\":\"41.678473\",\"longitude\":\"-108.782775\",\"elevation\":\"917.1432\"},\"viewAngle\":\"1010101010101010\",\"mutcdCode\":\"warning\",\"crc\":\"0000\"}},\"priority\":\"0\",\"doNotUse2\":\"3\",\"regions\":[{\"name\":\"Testing TIM\",\"regulatorID\":\"0\",\"segmentID\":\"33\",\"anchorPosition\":{\"latitude\":\"41.2500807\",\"longitude\":\"-111.0093847\",\"elevation\":\"2020.6969900289998\"},\"laneWidth\":\"7\",\"directionality\":\"3\",\"closedPath\":\"false\",\"description\":\"path\",\"path\":{\"scale\":\"0\",\"type\":\"ll\",\"nodes\":[{\"nodeLong\":\"0.0030982\",\"nodeLat\":\"0.0014562\",\"delta\":\"node-LL3\"},{\"nodeLong\":\"-111.0093847\",\"nodeLat\":\"41.2500807\",\"delta\":\"node-LatLon\"}]},\"direction\":\"0000000000001010\"}],\"doNotUse4\":\"2\",\"doNotUse3\":\"3\",\"content\":\"Advisory\",\"items\":[\"125\",\"some text\",\"250\",\"'98765\"],\"url\":\"null\"}]}";
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

    private WydotTim getMockWydotTim() {
        WydotTim wydotTim = new WydotTim();
        wydotTim.setClientId("testclientid");
        wydotTim.setDirection("D");
        return wydotTim;
    }

    private WydotTravelerInputData getMockWydotTravelerInputDataWithServiceRequest() {
        WydotTravelerInputData wydotTravelerInputData = new WydotTravelerInputData();
        wydotTravelerInputData.setTim(getMockOdeTravelerInformationMessage());
        ServiceRequest serviceRequest = new ServiceRequest();
        wydotTravelerInputData.setRequest(serviceRequest);
        return wydotTravelerInputData;
    }

    private WydotTravelerInputData getMockWydotTravelerInputDataWithDataFrame() {
        WydotTravelerInputData wydotTravelerInputData = new WydotTravelerInputData();
        OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();
        DataFrame df = new DataFrame();
        df.setStartDateTime("2022-01-01T00:00:00.000Z");
        tim.setDataframes(new DataFrame[]{df});
        wydotTravelerInputData.setTim(tim);
        return wydotTravelerInputData;
    }

    private TimType getMockTimType(String timTypeStr) {
        TimType timType = new TimType();
        timType.setType(timTypeStr);
        timType.setTimTypeId(1L);
        return timType;
    }

    public void setupRestTemplate() {
        doReturn(mockRestTemplate).when(mockRestTemplateProvider).GetRestTemplate_NoErrors();
    }

    @Test
    public void createTim_ReturnsNull_WhenBuildTimReturnsNull() {
        // Arrange
        String timTypeStr = "A";
        String startDateTime = "2023-01-01T00:00:00.000Z";
        String endDateTime = "2023-01-01T01:00:00.000Z";
        ContentEnum content = ContentEnum.workZone;
        TravelerInfoType frameType = TravelerInfoType.advisory;
        List<Milepost> allMileposts = new ArrayList<>();
        List<Milepost> reducedMileposts = new ArrayList<>();
        Milepost anchor = new Milepost();

        when(mockCreateBaseTimUtil.buildTim(any(), any(), any(), any(), any(), any(), any())).thenReturn(null);

        // Act
        WydotTravelerInputData result = uut.createTim(new WydotTim(), timTypeStr, startDateTime, endDateTime, content, frameType, allMileposts, reducedMileposts, anchor);

        // Assert
        assertNull(result);
    }

    @Test
    public void createTim_SetsStartDateTime_WhenProvided() {
        // Arrange
        String timTypeStr = "A";
        String startDateTime = "2023-01-01T00:00:00.000Z";
        String endDateTime = "2023-01-01T01:00:00.000Z";
        ContentEnum content = ContentEnum.workZone;
        TravelerInfoType frameType = TravelerInfoType.advisory;
        List<Milepost> allMileposts = new ArrayList<>();
        List<Milepost> reducedMileposts = new ArrayList<>();
        Milepost anchor = new Milepost();

        WydotTravelerInputData timToSend = getMockWydotTravelerInputDataWithDataFrame();

        when(mockCreateBaseTimUtil.buildTim(any(), any(), any(), any(), any(), any(), any())).thenReturn(timToSend);

        // Act
        WydotTravelerInputData result = uut.createTim(new WydotTim(), timTypeStr, startDateTime, endDateTime, content, frameType, allMileposts, reducedMileposts, anchor);

        // Assert
        assertEquals(startDateTime, result.getTim().getDataframes()[0].getStartDateTime());
    }

    @Test
    public void createTim_SetsDurationTime_WhenEndDateTimeProvided() {
        // Arrange
        String timTypeStr = "A";
        String startDateTime = "2023-01-01T00:00:00.000Z";
        String endDateTime = "2023-01-01T01:00:00.000Z";
        ContentEnum content = ContentEnum.workZone;
        TravelerInfoType frameType = TravelerInfoType.advisory;
        List<Milepost> allMileposts = new ArrayList<>();
        List<Milepost> reducedMileposts = new ArrayList<>();
        Milepost anchor = new Milepost();

        WydotTravelerInputData timToSend = getMockWydotTravelerInputDataWithDataFrame();

        when(mockCreateBaseTimUtil.buildTim(any(), any(), any(), any(), any(), any(), any())).thenReturn(timToSend);
        when(mockUtility.getMinutesDurationBetweenTwoDates(anyString(), anyString())).thenReturn(60);

        // Act
        WydotTravelerInputData result = uut.createTim(new WydotTim(), timTypeStr, startDateTime, endDateTime, content, frameType, allMileposts, reducedMileposts, anchor);

        // Assert
        assertEquals(60, result.getTim().getDataframes()[0].getDurationTime());
    }

    @Test
    public void createTim_SetsDurationTimeTo120_ForParkingTim() {
        // Arrange
        String timTypeStr = "P";
        String startDateTime = "2023-01-01T00:00:00.000Z";
        String endDateTime = "2023-01-01T01:00:00.000Z";
        ContentEnum content = ContentEnum.workZone;
        TravelerInfoType frameType = TravelerInfoType.advisory;
        List<Milepost> allMileposts = new ArrayList<>();
        List<Milepost> reducedMileposts = new ArrayList<>();
        Milepost anchor = new Milepost();

        WydotTravelerInputData timToSend = getMockWydotTravelerInputDataWithDataFrame();

        when(mockCreateBaseTimUtil.buildTim(any(), any(), any(), any(), any(), any(), any())).thenReturn(timToSend);

        // Act
        WydotTravelerInputData result = uut.createTim(new WydotTim(), timTypeStr, startDateTime, endDateTime, content, frameType, allMileposts, reducedMileposts, anchor);

        // Assert
        assertEquals(120, result.getTim().getDataframes()[0].getDurationTime());
    }

    @Test
    public void createTim_SetsRandomPacketId() {
        // Arrange
        String timTypeStr = "A";
        String startDateTime = "2023-01-01T00:00:00.000Z";
        String endDateTime = "2023-01-01T01:00:00.000Z";
        ContentEnum content = ContentEnum.workZone;
        TravelerInfoType frameType = TravelerInfoType.advisory;
        List<Milepost> allMileposts = new ArrayList<>();
        List<Milepost> reducedMileposts = new ArrayList<>();
        Milepost anchor = new Milepost();

        WydotTravelerInputData timToSend = getMockWydotTravelerInputDataWithDataFrame();

        when(mockCreateBaseTimUtil.buildTim(any(), any(), any(), any(), any(), any(), any())).thenReturn(timToSend);

        // Act
        WydotTravelerInputData result = uut.createTim(new WydotTim(), timTypeStr, startDateTime, endDateTime, content, frameType, allMileposts, reducedMileposts, anchor);

        // Assert
        assertNotNull(result.getTim().getPacketID());
        assertEquals(18, result.getTim().getPacketID().length());
        assertTrue(result.getTim().getPacketID().matches("[0-9A-F]+"));
    }

    @Test
    public void getAllMilepostsForTim_EndPointNotNull() {
        // Arrange
        WydotTim wydotTim = new WydotTim();
        wydotTim.setEndPoint(new Coordinate(BigDecimal.valueOf(41.678473), BigDecimal.valueOf(-108.782775)));
        List<Milepost> expectedMileposts = getMockMileposts();

        when(mockMilepostService.getMilepostsByStartEndPointDirection(wydotTim)).thenReturn(expectedMileposts);

        // Act
        List<Milepost> result = uut.getAllMilepostsForTim(wydotTim);

        // Assert
        assertEquals(expectedMileposts, result);
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(wydotTim);
    }

    @Test
    public void getAllMilepostsForTim_EndPointNull() {
        // Arrange
        WydotTim wydotTim = getMockWydotTim();
        Coordinate startPoint = new Coordinate(BigDecimal.valueOf(41.678473), BigDecimal.valueOf(-108.782775));
        wydotTim.setStartPoint(startPoint);
        wydotTim.setRoute("I-80");
        List<Milepost> expectedMileposts = getMockMileposts();

        MilepostBuffer mpb = new MilepostBuffer();
        mpb.setBufferMiles(mockGenProps.getPointIncidentBufferMiles());
        mpb.setCommonName(wydotTim.getRoute());
        mpb.setDirection(wydotTim.getDirection());
        mpb.setPoint(wydotTim.getStartPoint());

        when(mockMilepostService.getMilepostsByPointWithBuffer(any())).thenReturn(expectedMileposts);

        // Act
        List<Milepost> result = uut.getAllMilepostsForTim(wydotTim);

        // Assert
        assertEquals(expectedMileposts, result);
        verify(mockMilepostService).getMilepostsByPointWithBuffer(any());
    }

    @Test
    public void sendTimToSDW_MultipleActiveSatTimsFound() {
        // Arrange
        List<ActiveTim> activeSatTims = getActiveTims(true);
        when(mockActiveTimService.getActiveTimsByClientIdDirection(any(), any(), any())).thenReturn(activeSatTims);
        WydotTim wydotTim = getMockWydotTim();
        WydotTravelerInputData timToSend = getMockWydotTravelerInputDataWithServiceRequest();
        String regionNamePrev = "regionNamePrev";
        TimType timType = new TimType();

        Coordinate endPoint = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        List<Milepost> reducedMileposts = getMockMileposts();
        WydotOdeTravelerInformationMessage mockExistingSatTim = getMockWydotOdeTravelerInformationMessage();
        when(mockTimService.getTim(any())).thenReturn(mockExistingSatTim);
        RestTemplate mockRestTemplate = new RestTemplate();
        when(mockRestTemplateProvider.GetRestTemplate()).thenReturn(mockRestTemplate);

        // Act
        uut.sendTimToSDW(wydotTim, timToSend, regionNamePrev, timType, 0, endPoint, reducedMileposts);

        // Assert
        verify(mockUtility).logWithDate("Multiple active SAT TIMs found for client testclientid and direction D. Expected zero or one. Using the first one found.");
    }

    @Test
    public void sendTimToSDW_NoActiveSatTimsFound() {
        // Arrange
        when(mockActiveTimService.getActiveTimsByClientIdDirection(any(), any(), any())).thenReturn(new ArrayList<>());
        WydotTim wydotTim = getMockWydotTim();
        WydotTravelerInputData timToSend = getMockWydotTravelerInputDataWithServiceRequest();
        String regionNamePrev = "regionNamePrev";
        TimType timType = new TimType();
        Coordinate endPoint = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        List<Milepost> reducedMileposts = getMockMileposts();
        when(mockSdwService.getNewRecordId()).thenReturn("newRecordId");

        // Act
        uut.sendTimToSDW(wydotTim, timToSend, regionNamePrev, timType, 0, endPoint, reducedMileposts);

        // Assert
        verify(mockSdwService).getNewRecordId();
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockUtility, never()).logWithDate("Multiple active SAT TIMs found for client testclientid and direction D. Expected zero or one. Using the first one found.");
    }

    @Test
    public void sendTimToSDW_SingleActiveSatTimFound() {
        // Arrange
        List<ActiveTim> activeSatTims = getActiveTims(true).subList(0, 1);
        when(mockActiveTimService.getActiveTimsByClientIdDirection(any(), any(), any())).thenReturn(activeSatTims);
        WydotTim wydotTim = getMockWydotTim();
        WydotTravelerInputData timToSend = getMockWydotTravelerInputDataWithServiceRequest();
        String regionNamePrev = "regionNamePrev";
        TimType timType = new TimType();
        Coordinate endPoint = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        List<Milepost> reducedMileposts = getMockMileposts();
        WydotOdeTravelerInformationMessage mockExistingSatTim = getMockWydotOdeTravelerInformationMessage();
        when(mockTimService.getTim(any())).thenReturn(mockExistingSatTim);

        // Act
        uut.sendTimToSDW(wydotTim, timToSend, regionNamePrev, timType, 0, endPoint, reducedMileposts);

        // Assert
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockActiveTimService).resetActiveTimsExpirationDate(any());
        verify(mockTimService).getTim(any());
        verify(mockUtility, never()).logWithDate("Multiple active SAT TIMs found for client testclientid and direction D. Expected zero or one. Using the first one found.");
    }

    @Test
    public void sendTimToRsus_NoRsusFound() {
        // Arrange
        when(mockRsuService.getRsusByLatLong(any(), any(), any(), any())).thenReturn(new ArrayList<>());

        WydotTim wydotTim = getMockWydotTim();
        WydotTravelerInputData timToSend = getMockWydotTravelerInputDataWithServiceRequest();
        String regionNamePrev = "regionNamePrev";
        TimType timType = new TimType();
        String endDateTime = "2024-08-29 14:45:30";
        Coordinate endPoint = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));


        // Act
        uut.sendTimToRsus(wydotTim, timToSend, regionNamePrev, timType, 0, endDateTime, endPoint);

        // Assert
        verify(mockUtility).logWithDate("No RSUs found to place TIM on, returning");
    }

    @Test
    public void sendTimToRsus_RsusFound_NoActiveTIMs() {
        // Arrange
        List<WydotRsu> rsus = new ArrayList<>();
        WydotRsu rsu = new WydotRsu();
        rsu.setRsuId(1);
        rsu.setRsuIndex(1);
        rsu.setRsuTarget("target");
        rsus.add(rsu);
        when(mockRsuService.getRsusByLatLong(any(), any(), any(), any())).thenReturn(rsus);
        when(mockActiveTimService.getActiveRsuTim(any())).thenReturn(null);
        when(mockOdeService.submitTimQuery(any(), anyInt())).thenReturn(new TimQuery());
        when(mockActiveTimHoldingService.getActiveTimHoldingForRsu(any())).thenReturn(new ArrayList<>());
        when(mockRsuService.getActiveRsuTimIndexes(any())).thenReturn(new ArrayList<>());
        when(mockOdeService.findFirstAvailableIndexWithRsuIndex(any())).thenReturn(1);

        WydotTim wydotTim = getMockWydotTim();
        WydotTravelerInputData timToSend = getMockWydotTravelerInputDataWithServiceRequest();
        String regionNamePrev = "regionNamePrev";
        TimType timType = new TimType();

        String endDateTime = "2024-08-29 14:45:30";
        Coordinate endPoint = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));

        // Act
        uut.sendTimToRsus(wydotTim, timToSend, regionNamePrev, timType, 0, endDateTime, endPoint);

        // Assert
        verify(mockOdeService).sendNewTimToRsu(any());
    }

    @Test
    public void sendTimToRsus_RsusFound_ActiveTIMsExist() {
        // Arrange
        List<WydotRsu> rsus = new ArrayList<>();
        WydotRsu rsu = new WydotRsu();
        rsu.setRsuId(1);
        rsu.setRsuIndex(1);
        rsu.setRsuTarget("target");
        rsus.add(rsu);
        when(mockRsuService.getRsusByLatLong(any(), any(), any(), any())).thenReturn(rsus);
        ActiveTim activeTim = new ActiveTim();
        activeTim.setActiveTimId(1L);
        activeTim.setTimId(1L);
        when(mockActiveTimService.getActiveRsuTim(any())).thenReturn(activeTim);
        when(mockTimService.getTim(any())).thenReturn(new WydotOdeTravelerInformationMessage());

        WydotTim wydotTim = getMockWydotTim();
        WydotTravelerInputData timToSend = getMockWydotTravelerInputDataWithServiceRequest();
        String regionNamePrev = "regionNamePrev";
        TimType timType = new TimType();

        String endDateTime = "2024-08-29 14:45:30";
        Coordinate endPoint = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));

        TimRsu timRsu = new TimRsu();
        timRsu.setRsuId(1L);
        timRsu.setRsuIndex(1);
        when(mockTimRsuService.getTimRsu(any(), any())).thenReturn(timRsu);
        when(mockRsuService.selectAll()).thenReturn(rsus);
        ResponseEntity<String> mockResponse = new ResponseEntity<>(HttpStatus.OK);
        when(mockRestTemplateProvider.GetRestTemplate_NoErrors()).thenReturn(mockRestTemplate);
        when(mockRestTemplate.exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponse);

        // Act
        uut.sendTimToRsus(wydotTim, timToSend, regionNamePrev, timType, 0, endDateTime, endPoint);

        // Assert
        verify(mockUtility).logWithDate("Deleting TIM on index " + rsu.getRsuIndex() + " from rsu " + rsu.getRsuTarget());
        verify(mockOdeService).sendNewTimToRsu(any());
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
    public void clearTimsById_NoBuffers() {
        // Arrange
        String timTypeStr = "testType";
        String clientId = "testClient";
        String direction = "D";
        TimType timType = getMockTimType(timTypeStr);
        List<ActiveTim> activeTims = Arrays.asList(new ActiveTim(), new ActiveTim());
        List<Long> activeTimIds = activeTims.stream().map(ActiveTim::getActiveTimId).collect(Collectors.toList());

        when(mockActiveTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(), direction)).thenReturn(activeTims);
        when(mockTimTypeService.selectAll()).thenReturn(Arrays.asList(timType));
        // Act
        boolean result = uut.clearTimsById(timTypeStr, clientId, direction);

        // Assert
        assertTrue(result);
        verify(mockTimGenerationHelper).expireTimAndResubmitToOde(activeTimIds);
        verify(mockActiveTimService, never()).getBufferTimsByClientId(clientId);
    }

    @Test
    public void clearTimsById_WithBuffers() {
        // Arrange
        String timTypeStr = "testType";
        String clientId = "testClient";
        String direction = "D";
        TimType timType = getMockTimType(timTypeStr);
        List<ActiveTim> activeTims = Arrays.asList(new ActiveTim(), new ActiveTim());
        List<Long> activeTimIds = activeTims.stream().map(ActiveTim::getActiveTimId).collect(Collectors.toList());

        when(mockActiveTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(), direction)).thenReturn(activeTims);
        when(mockTimTypeService.selectAll()).thenReturn(Arrays.asList(timType));

        // Act
        boolean result = uut.clearTimsById(timTypeStr, clientId, direction, true);

        // Assert
        assertTrue(result);
        verify(mockTimGenerationHelper).expireTimAndResubmitToOde(activeTimIds);
        verify(mockActiveTimService).getBufferTimsByClientId(clientId);
    }

    @Test
    public void deleteWydotTimsByType_CallsGetActiveTimsByWydotTim_WithCorrectParameters() {
        // Arrange
        List<WydotTim> wydotTims = new ArrayList<>();
        WydotTim wydotTim = new WydotTim();
        wydotTims.add(wydotTim);
        String timTypeStr = "A";
        TimType timType = getMockTimType(timTypeStr);

        when(mockTimTypeService.selectAll()).thenReturn(Arrays.asList(timType));
        when(mockActiveTimService.getActiveTimsByWydotTim(anyList(), eq(1L))).thenReturn(new ArrayList<>());

        // Act
        boolean result = uut.deleteWydotTimsByType(wydotTims, timTypeStr);

        // Assert
        verify(mockActiveTimService).getActiveTimsByWydotTim(wydotTims, 1L);
        assertTrue(result);
    }

    @Test
    public void deleteWydotTimsByType_CallsDeleteTimsFromRsusAndSdx_WithCorrectParameters() {
        // Arrange
        List<WydotTim> wydotTims = new ArrayList<>();
        WydotTim wydotTim = new WydotTim();
        wydotTims.add(wydotTim);
        String timTypeStr = "A";
        TimType timType = getMockTimType(timTypeStr);

        List<ActiveTim> activeTims = new ArrayList<>();
        ActiveTim activeTim = new ActiveTim();
        activeTims.add(activeTim);

        when(mockTimTypeService.selectAll()).thenReturn(Arrays.asList(timType));
        when(mockActiveTimService.getActiveTimsByWydotTim(anyList(), eq(1L))).thenReturn(activeTims);

        // Act
        boolean result = uut.deleteWydotTimsByType(wydotTims, timTypeStr);

        // Assert
        verify(mockActiveTimService).getActiveTimsByWydotTim(wydotTims, 1L);
        verify(mockActiveTimService).deleteActiveTim(activeTim.getActiveTimId());
        assertTrue(result);
    }

    @Test
    public void selectTimByClientId_ReturnsActiveTims_WhenFound() {
        // Arrange
        String timTypeStr = "A";
        String clientId = "client123";
        TimType timType = getMockTimType(timTypeStr);
        List<ActiveTim> expectedActiveTims = Arrays.asList(new ActiveTim(), new ActiveTim());

        when(mockTimTypeService.selectAll()).thenReturn(Arrays.asList(timType));
        when(mockActiveTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(), null))
            .thenReturn(expectedActiveTims);

        // Act
        List<ActiveTim> result = uut.selectTimByClientId(timTypeStr, clientId);

        // Assert
        assertEquals(expectedActiveTims, result);
    }

    @Test
    public void selectTimByClientId_ReturnsEmptyList_WhenNoActiveTimsFound() {
        // Arrange
        String timTypeStr = "A";
        String clientId = "client123";
        TimType timType = getMockTimType(timTypeStr);
        List<ActiveTim> expectedActiveTims = new ArrayList<>();

        when(mockTimTypeService.selectAll()).thenReturn(Arrays.asList(timType));
        when(mockActiveTimService.getActiveTimsByClientIdDirection(clientId, timType.getTimTypeId(), null))
            .thenReturn(expectedActiveTims);

        // Act
        List<ActiveTim> result = uut.selectTimByClientId(timTypeStr, clientId);

        // Assert
        assertEquals(expectedActiveTims, result);
    }

    @Test
    public void selectTimsByType_ReturnsActiveTims_WhenFound() {
        // Arrange
        String timTypeStr = "A";
        TimType timType = getMockTimType(timTypeStr);
        List<ActiveTim> expectedActiveTims = Arrays.asList(new ActiveTim(), new ActiveTim());

        when(mockTimTypeService.selectAll()).thenReturn(Arrays.asList(timType));
        when(mockActiveTimService.getActivesTimByType(timType.getTimTypeId())).thenReturn(expectedActiveTims);

        // Act
        List<ActiveTim> result = uut.selectTimsByType(timTypeStr);

        // Assert
        assertEquals(expectedActiveTims, result);
    }

    @Test
    public void selectTimsByType_ReturnsEmptyList_WhenNoActiveTimsFound() {
        // Arrange
        String timTypeStr = "A";
        TimType timType = getMockTimType(timTypeStr);
        List<ActiveTim> expectedActiveTims = new ArrayList<>();

        when(mockTimTypeService.selectAll()).thenReturn(Arrays.asList(timType));
        when(mockActiveTimService.getActivesTimByType(timType.getTimTypeId())).thenReturn(expectedActiveTims);

        // Act
        List<ActiveTim> result = uut.selectTimsByType(timTypeStr);

        // Assert
        assertEquals(expectedActiveTims, result);
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