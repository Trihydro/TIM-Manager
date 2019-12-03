package com.trihydro.timrefresh;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
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
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.timrefresh.config.BasicConfiguration;
import com.trihydro.timrefresh.service.WydotTimService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
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
        SdwService.class, DataFrameService.class, Utility.class })
public class TimRefreshControllerTest {
    private long timID = 1l;

    @Rule
    public TestName name = new TestName();

    @Mock
    BasicConfiguration configuration;

    // @InjectMocks
    private TimRefreshController controllerUnderTest;

    @Before
    public void setup() {
        controllerUnderTest = new TimRefreshController(configuration);

        PowerMockito.mockStatic(ActiveTimService.class);
        PowerMockito.mockStatic(WydotTimService.class);
        PowerMockito.mockStatic(RsuService.class);
        PowerMockito.mockStatic(MilepostService.class);
        PowerMockito.mockStatic(SdwService.class);
        PowerMockito.mockStatic(DataFrameService.class);
        PowerMockito.mockStatic(Utility.class);

        setupMilePost();
        setupDataFrameService();
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
        startMp.setMilepostId(-1);
        startMp.setRoute("route1");
        startMp.setMilepost(250d);
        startMp.setDirection("eastward");
        startMp.setLatitude(105d);
        startMp.setLongitude(45d);
        startMp.setElevation(100d);
        startMp.setBearing(22d);

        Milepost endMp = new Milepost();
        endMp.setMilepostId(-2);
        endMp.setRoute("route1");
        endMp.setMilepost(255d);
        endMp.setDirection("eastward");
        endMp.setLatitude(105d);
        endMp.setLongitude(45d);
        endMp.setElevation(100d);
        endMp.setBearing(59d);
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
        PowerMockito.verifyStatic(VerificationModeFactory.times(1));
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
        when(Utility.getRsusInBuffer(isA(String.class), isA(double.class), isA(double.class), isA(String.class)))
                .thenReturn(rsus);

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions were called
        PowerMockito.verifyStatic();
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
        PowerMockito.verifyStatic();
        RsuService.getFullRsusTimIsOn(timID);

        PowerMockito.verifyStatic();
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
        when(SdwService.getSdwDataByRecordId(any(String.class))).thenReturn(getAdvisorySituationDataDeposit());

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions were called
        PowerMockito.verifyStatic();
        ActiveTimService.getExpiringActiveTims();

        PowerMockito.verifyStatic();
        MilepostService.selectMilepostRange(any(), any(), any(), any());

        PowerMockito.verifyStatic();
        WydotTimService.getServiceRegion(any());

        PowerMockito.verifyStatic();
        WydotTimService.updateTimOnSdw(any());

        // verify static functions were called
        PowerMockito.verifyStatic();
        RsuService.getFullRsusTimIsOn(any());

        PowerMockito.verifyNoMoreInteractions(MilepostService.class);
        PowerMockito.verifyNoMoreInteractions(ActiveTimService.class);
        PowerMockito.verifyNoMoreInteractions(WydotTimService.class);
    }

    private TimUpdateModel getTumBase() {
        TimUpdateModel tum = new TimUpdateModel();
        tum.setRoute("UnitTestRoute");
        tum.setDirection("eastward");
        tum.setMilepostStart(1d);
        tum.setMilepostStop(2d);
        tum.setClosedPath(false);
        return tum;
    }

    private TimUpdateModel getRsuTim() {
        TimUpdateModel tum = getTumBase();
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