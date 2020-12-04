package com.trihydro.timrefresh;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.mail.MessagingException;

import com.google.gson.Gson;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.TimGenerationHelper;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Logging_TimUpdateModel;
import com.trihydro.library.model.ResubmitTimException;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.SdwService;
import com.trihydro.timrefresh.config.TimRefreshConfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;

@ExtendWith(MockitoExtension.class)
public class TimRefreshControllerTest {
    private long timID = 1l;

    @Mock
    TimRefreshConfiguration mockConfiguration;
    @Mock
    SdwService mockSdwService;
    @Mock
    Utility mockUtility;
    @Mock
    ActiveTimService mockActiveTimService;
    @Mock
    EmailHelper mockEmailHelper;
    @Mock
    TimGenerationHelper mockTimGenerationHelper;

    @InjectMocks
    private TimRefreshController controllerUnderTest;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        System.out.println("Executing " + testInfo.getTestMethod().get().getName());
    }

    @Test
    public void TestPerformTaskUsingCron_NoData() {
        // setup return
        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(new ArrayList<TimUpdateModel>());
        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify getExpiringActiveTims called once
        verify(mockActiveTimService).getExpiringActiveTims();
        // verify no further interactions on ActiveTimService
        verifyNoMoreInteractions(mockActiveTimService);
        // verify no emails sent
        verifyNoInteractions(mockEmailHelper);
    }

    @Test
    public void TestPerformTaskUsingCron_InvalidData_Milepost() throws MailException, MessagingException {
        // Arrange
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        TimUpdateModel tum = getRsuTim();
        tum.setStartPoint(new Coordinate());
        arrLst.add(tum);

        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);

        // Act
        controllerUnderTest.performTaskUsingCron();

        // Assert
        Gson gson = new Gson();
        String body = "The Tim Refresh application found invalid TIM(s) while attempting to refresh.";
        body += "<br/>";
        body += "The associated ActiveTim records are: <br/>";
        body += gson.toJson(new Logging_TimUpdateModel(tum));
        body += "<br/><br/>";
        verify(mockEmailHelper).SendEmail(mockConfiguration.getAlertAddresses(), null, "TIM Refresh Exceptions", body,
                mockConfiguration.getMailPort(), mockConfiguration.getMailHost(), mockConfiguration.getFromEmail());
        verifyNoInteractions(mockTimGenerationHelper);
    }

    @Test
    public void TestPerformTaskUsingCron_InvalidData_Direction() throws MailException, MessagingException {
        // Arrange
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        TimUpdateModel tum = getRsuTim();
        tum.setDirection("");
        arrLst.add(tum);

        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);

        // Act
        controllerUnderTest.performTaskUsingCron();

        // Assert
        Gson gson = new Gson();
        String body = "The Tim Refresh application found invalid TIM(s) while attempting to refresh.";
        body += "<br/>";
        body += "The associated ActiveTim records are: <br/>";
        body += gson.toJson(new Logging_TimUpdateModel(tum));
        body += "<br/><br/>";
        verify(mockEmailHelper).SendEmail(mockConfiguration.getAlertAddresses(), null, "TIM Refresh Exceptions", body,
                mockConfiguration.getMailPort(), mockConfiguration.getMailHost(), mockConfiguration.getFromEmail());
        verifyNoInteractions(mockTimGenerationHelper);
    }

    @Test
    public void TestPerformTaskUsingCron_InvalidData_Route() throws MailException, MessagingException {
        // Arrange
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        TimUpdateModel tum = getRsuTim();
        tum.setRoute("");
        arrLst.add(tum);

        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);

        // Act
        controllerUnderTest.performTaskUsingCron();

        // Assert
        Gson gson = new Gson();
        String body = "The Tim Refresh application found invalid TIM(s) while attempting to refresh.";
        body += "<br/>";
        body += "The associated ActiveTim records are: <br/>";
        body += gson.toJson(new Logging_TimUpdateModel(tum));
        body += "<br/><br/>";
        verify(mockEmailHelper).SendEmail(mockConfiguration.getAlertAddresses(), null, "TIM Refresh Exceptions", body,
                mockConfiguration.getMailPort(), mockConfiguration.getMailHost(), mockConfiguration.getFromEmail());
        verifyNoInteractions(mockTimGenerationHelper);
    }

    @Test
    public void TestPerformTaskUsingCron_RefreshException() throws MailException, MessagingException {
        // setup return
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        TimUpdateModel tum = getRsuTim();
        arrLst.add(tum);

        List<ResubmitTimException> resubExceptions = new ArrayList<>();
        resubExceptions.add(new ResubmitTimException(-1l, "exception message"));

        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(mockTimGenerationHelper.resubmitToOde(any())).thenReturn(resubExceptions);

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions were called
        verify(mockTimGenerationHelper).resubmitToOde(Collections.singletonList(tum.getActiveTimId()));
        var gson = new Gson();
        String body = "The TIM Refresh application ran into exceptions while attempting to resubmit TIMs. The following exceptions were found: ";
        body += "<br/>";
        body += gson.toJson(resubExceptions.get(0));
        body += "<br/>";
        verify(mockEmailHelper).SendEmail(mockConfiguration.getAlertAddresses(), null, "TIM Refresh Exceptions", body,
                mockConfiguration.getMailPort(), mockConfiguration.getMailHost(), mockConfiguration.getFromEmail());
    }

    @Test
    public void TestPerformTaskUsingCron_Success() throws MailException, MessagingException {
        // setup return
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        TimUpdateModel tum = getRsuTim();
        arrLst.add(tum);

        List<ResubmitTimException> resubExceptions = new ArrayList<>();

        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(mockTimGenerationHelper.resubmitToOde(any())).thenReturn(resubExceptions);

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions were called
        verify(mockTimGenerationHelper).resubmitToOde(Collections.singletonList(tum.getActiveTimId()));
        // verify no emails sent
        verifyNoInteractions(mockEmailHelper);
    }

    private TimUpdateModel getTumBase() {
        TimUpdateModel tum = new TimUpdateModel();
        tum.setRoute("I 80");
        tum.setDirection("i");
        tum.setStartPoint(new Coordinate(BigDecimal.valueOf(-1), BigDecimal.valueOf(-2)));
        tum.setEndPoint(new Coordinate(BigDecimal.valueOf(-3), BigDecimal.valueOf(-4)));
        tum.setClosedPath(false);
        return tum;
    }

    private TimUpdateModel getRsuTim() {
        TimUpdateModel tum = getTumBase();
        tum.setRoute("I 80");
        tum.setRsuTarget("DefaultTarget");
        tum.setClosedPath(false);
        tum.setTimId(timID);

        tum.setStartDate_Timestamp(new Timestamp(System.currentTimeMillis()));
        tum.setEndDate_Timestamp(new Timestamp(System.currentTimeMillis()));
        return tum;
    }

    // private void setupRoutes() {
    // String[] routes = new String[1];
    // routes[0] = "I 80";
    // doReturn(routes).when(mockConfiguration).getRsuRoutes();
    // }

    // private void setupMilePost() {
    // List<Milepost> mps = new ArrayList<Milepost>();
    // Milepost startMp = new Milepost();
    // startMp.setCommonName("route1");
    // startMp.setMilepost(250d);
    // startMp.setDirection("i");
    // startMp.setLatitude(BigDecimal.valueOf(105));
    // startMp.setLongitude(BigDecimal.valueOf(45d));

    // Milepost endMp = new Milepost();
    // endMp.setCommonName("route1");
    // endMp.setMilepost(255d);
    // endMp.setDirection("i");
    // endMp.setLatitude(BigDecimal.valueOf(105d));
    // endMp.setLongitude(BigDecimal.valueOf(45d));
    // mps.add(startMp);
    // mps.add(endMp);
    // doReturn(mps).when(mockMilepostService).getMilepostsByStartEndPointDirection(any());
    // doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(),
    // any());
    // }

    // @Test
    // public void TestPerformTaskUsingCron_Rsu_NotFound() {
    // // setup return
    // setupRoutes();
    // setupMilePost();
    // ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
    // ArrayList<WydotRsuTim> wydotRsuTims = new ArrayList<WydotRsuTim>();
    // ArrayList<WydotRsu> rsus = new ArrayList<WydotRsu>();
    // TimUpdateModel tum = getRsuTim();
    // arrLst.add(tum);
    // when(mockTimGenerationHelper.getTim(any(), any(), any())).thenReturn(new
    // OdeTravelerInformationMessage());
    // when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
    // when(mockRsuService.getFullRsusTimIsOn(isA(long.class))).thenReturn(wydotRsuTims);
    // doReturn(rsus).when(mockRsuService).getRsusByLatLong(anyString(), any(),
    // any(), anyString());

    // // call the function to test
    // controllerUnderTest.performTaskUsingCron();

    // // verify functions were called
    // verify(mockRsuService).getFullRsusTimIsOn(timID);
    // verify(mockRsuService).getRsusByLatLong(anyString(), any(), any(),
    // anyString());
    // verifyNoMoreInteractions(mockRsuService);
    // // verify no emails sent
    // verifyNoInteractions(mockEmailHelper);
    // }

    // @Test
    // public void TestPerformTaskUsingCron_Rsu() {
    // // setup return
    // setupRoutes();
    // setupMilePost();
    // ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
    // ArrayList<WydotRsuTim> wydotRsuTims = new ArrayList<WydotRsuTim>();

    // WydotRsuTim wydotRsuTim = new WydotRsuTim();
    // wydotRsuTim.setRsuIndex(1);
    // wydotRsuTim.setRsuTarget("0.0.0.0");
    // wydotRsuTim.setRsuUsername("user");
    // wydotRsuTim.setRsuPassword("pass");
    // wydotRsuTims.add(wydotRsuTim);

    // TimUpdateModel tum = getRsuTim();
    // arrLst.add(tum);

    // when(mockTimGenerationHelper.getTim(any(), any(),
    // any())).thenReturn(getTim());
    // when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
    // when(mockRsuService.getFullRsusTimIsOn(any(long.class))).thenReturn(wydotRsuTims);

    // // call the function to test
    // controllerUnderTest.performTaskUsingCron();

    // // verify static functions were called
    // verify(mockRsuService).getFullRsusTimIsOn(timID);
    // verify(mockOdeService).updateTimOnRsu(any(WydotTravelerInputData.class));

    // verifyNoMoreInteractions(mockRsuService);
    // // verify no emails sent
    // verifyNoInteractions(mockEmailHelper);
    // }

    // @Test
    // public void TestPerformTaskUsingCron_Sdw() {
    // // setup return
    // setupRoutes();
    // setupMilePost();
    // ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();

    // TimUpdateModel tum = getSdwTim();
    // arrLst.add(tum);

    // when(mockTimGenerationHelper.getTim(any(), any(), any())).thenReturn(new
    // OdeTravelerInformationMessage());
    // when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
    // when(mockSdwService.getSdwDataByRecordId(any(String.class))).thenReturn(getAdvisorySituationDataDeposit());

    // // call the function to test
    // controllerUnderTest.performTaskUsingCron();

    // // verify functions were called
    // verify(mockActiveTimService).getExpiringActiveTims();
    // verify(mockOdeService).getServiceRegion(any());
    // verify(mockOdeService).updateTimOnSdw(any());
    // verify(mockRsuService).getFullRsusTimIsOn(any());
    // verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());

    // verifyNoMoreInteractions(mockMilepostService);
    // verifyNoMoreInteractions(mockActiveTimService);
    // // verify no emails sent
    // verifyNoInteractions(mockEmailHelper);
    // }

    // private OdeTravelerInformationMessage getTim() {
    // var tim = new OdeTravelerInformationMessage();
    // tim.setDataframes(getDataFrames());

    // return tim;
    // }

    // private DataFrame[] getDataFrames() {
    // DataFrame df = new DataFrame();
    // df.setRegions(getRegions());
    // return new DataFrame[] { df };
    // }

    // private Region[] getRegions() {
    // var region = new Region();
    // return new Region[] { region };
    // }

    // private TimUpdateModel getSdwTim() {
    // TimUpdateModel tum = getTumBase();
    // tum.setSatRecordId("SatRecord");
    // return tum;
    // }

    // private AdvisorySituationDataDeposit getAdvisorySituationDataDeposit() {
    // AdvisorySituationDataDeposit asdd = new AdvisorySituationDataDeposit();
    // asdd.setTimeToLive(TimeToLive.Day);

    // return asdd;
    // }
}