package com.trihydro.timrefresh;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.MilepostReduction;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.Coordinate;
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
import com.trihydro.library.service.RegionService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.timrefresh.config.TimRefreshConfiguration;
import com.trihydro.timrefresh.service.WydotTimService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    OdeService mockOdeService;
    @Mock
    MilepostService mockMilepostService;
    @Mock
    ActiveTimService mockActiveTimService;
    @Mock
    DataFrameService mockDataFrameService;
    @Mock
    RegionService mockRegionService;
    @Mock
    RsuService mockRsuService;
    @Mock
    WydotTimService mockWydotTimService;
    @Mock
    MilepostReduction mockMilepostReduction;

    @InjectMocks
    private TimRefreshController controllerUnderTest;

    @BeforeEach
    public void setup(TestInfo testInfo) {
        System.out.println("Executing " + testInfo.getTestMethod().get().getName());
    }

    private void setupRoutes(){
        String[] routes = new String[1];
        routes[0] = "I 80";
        doReturn(routes).when(mockConfiguration).getRsuRoutes();
    }

    private void setupDataFrameService() {
        String[] itisCodes = new String[1];
        itisCodes[0] = "1";
        when(mockDataFrameService.getItisCodesForDataFrameId(isA(Integer.class))).thenReturn(itisCodes);
    }

    private void setupMilePost() {
        List<Milepost> mps = new ArrayList<Milepost>();
        Milepost startMp = new Milepost();
        startMp.setCommonName("route1");
        startMp.setMilepost(250d);
        startMp.setDirection("i");
        startMp.setLatitude(BigDecimal.valueOf(105));
        startMp.setLongitude(BigDecimal.valueOf(45d));

        Milepost endMp = new Milepost();
        endMp.setCommonName("route1");
        endMp.setMilepost(255d);
        endMp.setDirection("i");
        endMp.setLatitude(BigDecimal.valueOf(105d));
        endMp.setLongitude(BigDecimal.valueOf(45d));
        mps.add(startMp);
        mps.add(endMp);
        doReturn(mps).when(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
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
        // verify nothing on WyDotTimService was called
        verifyNoInteractions(mockWydotTimService);
    }

    @Test
    public void TestPerformTaskUsingCron_Rsu_NotFound() {
        // setup return
        setupRoutes();
        setupMilePost();
        setupDataFrameService();
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();
        ArrayList<WydotRsuTim> wydotRsuTims = new ArrayList<WydotRsuTim>();
        ArrayList<WydotRsu> rsus = new ArrayList<WydotRsu>();
        TimUpdateModel tum = getRsuTim();
        arrLst.add(tum);
        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(mockRsuService.getFullRsusTimIsOn(isA(long.class))).thenReturn(wydotRsuTims);
        doReturn(rsus).when(mockRsuService).getRsusByLatLong(anyString(), any(), any(), anyString());

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify functions were called
        verify(mockRsuService).getFullRsusTimIsOn(timID);
        verify(mockRsuService).getRsusByLatLong(anyString(), any(), any(), anyString());
        verifyNoMoreInteractions(mockRsuService);
        verifyNoMoreInteractions(mockWydotTimService);
    }

    @Test
    public void TestPerformTaskUsingCron_Rsu() {
        // setup return
        setupRoutes();
        setupMilePost();
        setupDataFrameService();
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

        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(mockRsuService.getFullRsusTimIsOn(any(long.class))).thenReturn(wydotRsuTims);

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify static functions were called
        verify(mockRsuService).getFullRsusTimIsOn(timID);
        verify(mockWydotTimService).updateTimOnRsu(any(WydotTravelerInputData.class));

        verifyNoMoreInteractions(mockRsuService);
        verifyNoMoreInteractions(mockWydotTimService);
    }

    @Test
    public void TestPerformTaskUsingCron_Sdw() {
        // setup return
        setupRoutes();
        setupMilePost();
        setupDataFrameService();
        ArrayList<TimUpdateModel> arrLst = new ArrayList<TimUpdateModel>();

        TimUpdateModel tum = getSdwTim();
        arrLst.add(tum);

        when(mockActiveTimService.getExpiringActiveTims()).thenReturn(arrLst);
        when(mockSdwService.getSdwDataByRecordId(any(String.class))).thenReturn(getAdvisorySituationDataDeposit());

        // call the function to test
        controllerUnderTest.performTaskUsingCron();

        // verify functions were called
        verify(mockActiveTimService).getExpiringActiveTims();
        verify(mockWydotTimService).getServiceRegion(any());
        verify(mockWydotTimService).updateTimOnSdw(any());
        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());

        verifyNoMoreInteractions(mockMilepostService);
        verifyNoMoreInteractions(mockActiveTimService);
        verifyNoMoreInteractions(mockWydotTimService);
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