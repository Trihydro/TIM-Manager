package com.trihydro.timrefresh;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import static org.mockito.Mockito.when;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.any;

import java.sql.Timestamp;
import java.util.ArrayList;

import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.TimeToLive;
import com.trihydro.library.model.WydotRsuTim;
import com.trihydro.library.model.WydotTravelerInputData;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.timrefresh.service.WydotTimService;

/**
 * Unit tests for TimRefreshController
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ ActiveTimService.class, WydotTimService.class, RsuService.class, MilepostService.class,
        SdwService.class, DataFrameService.class })
public class TimRefreshControllerTest {
    private long timID = 1l;

    @Rule
    public TestName name = new TestName();

    @InjectMocks
    TimRefreshController controllerUnderTest;

    @Before
    public void setup() {
        PowerMockito.mockStatic(ActiveTimService.class);
        PowerMockito.mockStatic(WydotTimService.class);
        PowerMockito.mockStatic(RsuService.class);
        PowerMockito.mockStatic(MilepostService.class);
        PowerMockito.mockStatic(SdwService.class);
        PowerMockito.mockStatic(DataFrameService.class);

        System.out.println("Executing " + name.getMethodName());
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
        TimUpdateModel tum = getRsuTim();
        arrLst.add(tum);
        when(ActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(RsuService.getFullRsusTimIsOn(isA(long.class))).thenReturn(wydotRsuTims);

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

        PowerMockito.verifyZeroInteractions(RsuService.class);
        PowerMockito.verifyNoMoreInteractions(MilepostService.class);
        PowerMockito.verifyNoMoreInteractions(ActiveTimService.class);
        PowerMockito.verifyNoMoreInteractions(WydotTimService.class);
    }

    private TimUpdateModel getRsuTim() {
        TimUpdateModel tum = new TimUpdateModel();
        tum.setRsuTarget("DefaultTarget");
        tum.setClosedPath(false);
        tum.setTimId(timID);

        tum.setStartDate_Timestamp(new Timestamp(System.currentTimeMillis()));
        tum.setEndDate_Timestamp(new Timestamp(System.currentTimeMillis()));
        return tum;
    }

    private TimUpdateModel getSdwTim() {
        TimUpdateModel tum = new TimUpdateModel();
        tum.setSatRecordId("SatRecord");
        tum.setClosedPath(false);
        return tum;
    }

    private AdvisorySituationDataDeposit getAdvisorySituationDataDeposit() {
        AdvisorySituationDataDeposit asdd = new AdvisorySituationDataDeposit();
        asdd.setTimeToLive(TimeToLive.Day);

        return asdd;
    }
}