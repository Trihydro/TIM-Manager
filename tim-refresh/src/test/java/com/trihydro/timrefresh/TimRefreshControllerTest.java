package com.trihydro.timrefresh;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.TimeToLive;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.timrefresh.config.TimRefreshConfiguration;
import com.trihydro.timrefresh.service.WydotTimService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Unit tests for TimRefreshController
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ActiveTimService.class, WydotTimService.class, RsuService.class, MilepostService.class,
        DataFrameService.class })
public class TimRefreshControllerTest {
    private long timID = 1l;

    @Rule
    public TestName name = new TestName();

    @Mock
    TimRefreshConfiguration mockConfiguration;
    @Mock
    SdwService mockSdwService;
    @Mock
    Utility mockUtility;
    @Mock
    OdeService mockOdeService;

    @InjectMocks
    private TimRefreshController controllerUnderTest;

    @Before
    public void setup() {
        // controllerUnderTest = new TimRefreshController(configuration,
        // mockSdwService);

        PowerMockito.mockStatic(ActiveTimService.class);
        PowerMockito.mockStatic(WydotTimService.class);
        PowerMockito.mockStatic(RsuService.class);
        PowerMockito.mockStatic(MilepostService.class);
        PowerMockito.mockStatic(DataFrameService.class);

        setupMilePost();
        setupDataFrameService();
        String[] routes = new String[1];
        routes[0] = "I 80";
        doReturn(routes).when(mockConfiguration).getRsuRoutes();
        System.out.println("Executing " + name.getMethodName());
    }

    private void setupDataFrameService() {
        String[] itisCodes = new String[1];
        itisCodes[0] = "1";
        Mockito.when(DataFrameService.getItisCodesForDataFrameId(isA(Integer.class))).thenReturn(itisCodes);
    }

    private void setupMilePost() {
        List<Milepost> mps = new ArrayList<Milepost>();
        Milepost startMp = new Milepost();
        startMp.setCommonName("route1");
        startMp.setMilepost(250d);
        startMp.setDirection("i");
        startMp.setLatitude(105d);
        startMp.setLongitude(45d);
        // startMp.setBearing(22d);

        Milepost endMp = new Milepost();
        endMp.setCommonName("route1");
        endMp.setMilepost(255d);
        endMp.setDirection("i");
        endMp.setLatitude(105d);
        endMp.setLongitude(45d);
        // endMp.setBearing(59d);
        mps.add(startMp);
        mps.add(endMp);
        Mockito.when(MilepostService.selectMilepostRange(isA(String.class), isA(String.class), isA(Double.class),
                isA(Double.class))).thenReturn(mps);
    }

    @Test
    public void TestPerformTaskUsingCron_NoData() {
        // setup return
        Mockito.when(ActiveTimService.getExpiringActiveTims()).thenReturn(new ArrayList<TimUpdateModel>());
        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions, called once
        PowerMockito.verifyStatic(ActiveTimService.class, VerificationModeFactory.times(1));
        // verify getExpiringActiveTims called once
        ActiveTimService.getExpiringActiveTims();

        // verify no further interactions on ActiveTimService
        PowerMockito.verifyNoMoreInteractions(ActiveTimService.class);

        // verify nothing on WyDotTimService was called
        PowerMockito.verifyZeroInteractions(WydotTimService.class);
    }

    @Test
    public void TestPerformTaskUsingCron_Rsu_NotFound() {
        // setup return
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        ArrayList<WydotRsuTim> wydotRsuTims = new ArrayList<WydotRsuTim>();
        ArrayList<WydotRsu> rsus = new ArrayList<WydotRsu>();
        TimUpdateModel tum = getRsuTim();
        arrLst.add(tum);
        when(ActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(RsuService.getFullRsusTimIsOn(isA(long.class))).thenReturn(wydotRsuTims);
        when(mockUtility.getRsusInBuffer(isA(String.class), isA(double.class), isA(double.class), isA(String.class)))
                .thenReturn(rsus);

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions were called
        PowerMockito.verifyStatic(RsuService.class);
        RsuService.getFullRsusTimIsOn(timID);

        PowerMockito.verifyNoMoreInteractions(RsuService.class);
        PowerMockito.verifyNoMoreInteractions(WydotTimService.class);
    }

    @Test
    public void TestPerformTaskUsingCron_Rsu() {
        // setup return
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        ArrayList<WydotRsuTim> wydotRsuTims = new ArrayList<WydotRsuTim>();

        WydotRsuTim wydotRsuTim = new WydotRsuTim();
        wydotRsuTim.setRsuIndex(1);
        wydotRsuTim.setRsuTarget("0.0.0.0");
        wydotRsuTim.setRsuUsername("user");
        wydotRsuTim.setRsuPassword("pass");
        wydotRsuTims.add(wydotRsuTim);

        TimUpdateModel tum = getRsuTim();
        arrLst.add(tum);

        when(ActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(RsuService.getFullRsusTimIsOn(any(long.class))).thenReturn(wydotRsuTims);

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions were called
        PowerMockito.verifyStatic(RsuService.class);
        RsuService.getFullRsusTimIsOn(timID);

        PowerMockito.verifyStatic(WydotTimService.class);
        WydotTimService.updateTimOnRsu(any(WydotTravelerInputData.class));

        PowerMockito.verifyNoMoreInteractions(RsuService.class);
        PowerMockito.verifyNoMoreInteractions(WydotTimService.class);
    }

    @Test
    public void TestPerformTaskUsingCron_Sdw() {
        // setup return
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();

        TimUpdateModel tum = getSdwTim();
        arrLst.add(tum);

        when(ActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(mockSdwService.getSdwDataByRecordId(any(String.class))).thenReturn(getAdvisorySituationDataDeposit());

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions were called
        PowerMockito.verifyStatic(ActiveTimService.class);
        ActiveTimService.getExpiringActiveTims();

        PowerMockito.verifyStatic(MilepostService.class);
        MilepostService.selectMilepostRange(any(), any(), any(), any());

        PowerMockito.verifyStatic(WydotTimService.class);
        WydotTimService.getServiceRegion(any());

        PowerMockito.verifyStatic(WydotTimService.class);
        WydotTimService.updateTimOnSdw(any());

        // verify static functions were called
        PowerMockito.verifyStatic(RsuService.class);
        RsuService.getFullRsusTimIsOn(any());

        PowerMockito.verifyNoMoreInteractions(MilepostService.class);
        PowerMockito.verifyNoMoreInteractions(ActiveTimService.class);
        PowerMockito.verifyNoMoreInteractions(WydotTimService.class);
    }

    private TimUpdateModel getTumBase() {
        TimUpdateModel tum = new TimUpdateModel();
        tum.setRoute("I 80");
        tum.setDirection("i");
        tum.setMilepostStart(1d);
        tum.setMilepostStop(2d);
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

    private TimUpdateModel getSdwTim() {
        TimUpdateModel tum = getTumBase();
        tum.setSatRecordId("SatRecord");
        return tum;
    }

    private AdvisorySituationDataDeposit getAdvisorySituationDataDeposit() {
        AdvisorySituationDataDeposit asdd = new AdvisorySituationDataDeposit();
        asdd.setTimeToLive(TimeToLive.Day);

        return asdd;
    }
}