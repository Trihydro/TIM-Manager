package com.trihydro.library.helpers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.ResubmitTimException;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.TimeToLive;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;
import com.trihydro.library.service.ActiveTimHoldingService;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.DataFrameService;
import com.trihydro.library.service.MilepostService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.PathNodeXYService;
import com.trihydro.library.service.RegionService;
import com.trihydro.library.service.RsuService;
import com.trihydro.library.service.SdwService;
import com.trihydro.library.service.TimGenerationProps;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TimGenerationHelperTest {
    @Mock
    private Utility mockUtility;
    @Mock
    private DataFrameService mockDataFrameService;
    @Mock
    private PathNodeXYService mockPathNodeXYService;
    @Mock
    private ActiveTimService mockActiveTimService;
    @Mock
    private MilepostService mockMilepostService;
    @Mock
    private MilepostReduction mockMilepostReduction;
    @Mock
    private RegionService mockRegionService;
    @Mock
    private RsuService mockRsuService;
    @Mock
    private TimGenerationProps mockConfig;
    @Mock
    private OdeService mockOdeService;
    @Mock
    private ActiveTimHoldingService mockActiveTimHoldingService;
    @Mock
    private SdwService mockSdwService;

    private static Long activeTimId = -1l;
    private TimUpdateModel tum;

    @InjectMocks
    private TimGenerationHelper uut;

    @Test
    public void resubmitToOde_EmptyList() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
    }

    @Test
    public void resubmitToOde_NullList() {
        // Arrange

        // Act
        var exceptions = uut.resubmitToOde(null);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
    }

    @Test
    public void resubmitToOde_NullTum() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        doReturn(null).when(mockActiveTimService).getUpdateModelFromActiveTimId(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        verifyNoInteractions(mockDataFrameService, mockPathNodeXYService, mockMilepostService, mockMilepostReduction,
                mockRegionService, mockRsuService, mockConfig, mockOdeService, mockActiveTimHoldingService,
                mockSdwService);
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = "Failed to get Update Model from active tim";
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);
    }

    private void setupActiveTimModel() {
        tum = new TimUpdateModel();
        tum.setActiveTimId(activeTimId);
        tum.setStartPoint(new Coordinate(BigDecimal.valueOf(-1l), BigDecimal.valueOf(-2l)));
        tum.setEndPoint(new Coordinate(BigDecimal.valueOf(-3l), BigDecimal.valueOf(-4l)));

        // TIM Props
        tum.setMsgCnt(1);// int
        tum.setUrlB("urlb");// String
        tum.setStartDate_Timestamp(Timestamp.from(Instant.now()));// Timestamp
        tum.setEndDate_Timestamp(Timestamp.from(Instant.now()));// Timestamp
        tum.setPacketId("asdf");// String

        // Tim Type properties
        tum.setTimTypeName("timType");// String
        tum.setTimTypeDescription("descrip");// String

        // Region properties
        tum.setRegionId(-1);// Integer
        tum.setRegionName("name");// String
        tum.setRegionDescription("descrip");// String
        tum.setLaneWidth(BigDecimal.valueOf(50l));// BigDecimal
        tum.setAnchorLat(BigDecimal.valueOf(-1l));// BigDecimal
        tum.setAnchorLong(BigDecimal.valueOf(-2l));// BigDecimal
        tum.setRegionDirection("I");// String

        tum.setClosedPath(false);

        doReturn(tum).when(mockActiveTimService).getUpdateModelFromActiveTimId(any());
    }

    private void setupMilepostReturn() {
        List<Milepost> allMps = new ArrayList<>();
        var latitude = BigDecimal.valueOf(-1l);
        var longitude = BigDecimal.valueOf(-2l);
        var mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setDirection("I");
        mp.setLatitude(latitude);
        mp.setLongitude(longitude);
        allMps.add(mp);

        var mp2= new Milepost();
        mp.setCommonName("I 80");
        mp.setDirection("D");
        mp.setLatitude(latitude);
        mp.setLongitude(longitude);
        allMps.add(mp2);

        doReturn(allMps).when(mockMilepostService).getMilepostsByStartEndPointDirection(any());
    }

    @Test
    public void resubmitToOde_NoMileposts() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();

        List<Milepost> mps = new ArrayList<Milepost>();
        doReturn(mps).when(mockMilepostService).getMilepostsByStartEndPointDirection(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = String.format(
                "Unable to resubmit TIM, no mileposts found to determine service area for Active_Tim %d",
                activeTimId);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);
        verifyNoInteractions(mockDataFrameService, mockPathNodeXYService, mockRegionService, mockRsuService,
                mockOdeService, mockActiveTimHoldingService, mockSdwService);
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction);
    }

    @Test
    public void resubmitToOde_DataFrameException() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = String.format("Failed to instantiate TIM for active_tim_id %s", activeTimId);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);

        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockOdeService, mockActiveTimHoldingService,
                mockSdwService, mockRsuService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService);
    }

    @Test
    public void resubmitToOde_RsuException() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        String[] rsuRoutes = new String[] { "I 80" };
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        doReturn(new String[] { "1234" }).when(mockDataFrameService).getItisCodesForDataFrameId(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = "No possible RSUs found for active_tim_id " + activeTimId;
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);

        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockOdeService, mockActiveTimHoldingService,
                mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService, mockRsuService);
    }

    @Test
    public void resubmitToOde_RsuExistingSuccess() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        String[] rsuRoutes = new String[] { "I 80" };
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        List<WydotRsuTim> wydotRsus = new ArrayList<>();
        var wydotRsuTim = new WydotRsuTim();
        wydotRsuTim.setIndex(-1);
        wydotRsus.add(wydotRsuTim);
        doReturn(wydotRsus).when(mockRsuService).getFullRsusTimIsOn(any());
        doReturn(new String[] { "1234" }).when(mockDataFrameService).getItisCodesForDataFrameId(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).updateTimOnRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockActiveTimHoldingService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService, mockRsuService,
                mockOdeService);
    }

    @Test
    public void resubmitToOde_RsuNewFailTimQuery() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        String[] rsuRoutes = new String[] { "I 80" };
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        doReturn(new String[] { "1234" }).when(mockDataFrameService).getItisCodesForDataFrameId(any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(null);

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Gson gson = new Gson();
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        var exMsg = "Returning without sending TIM to RSU. submitTimQuery failed for RSU " + gson.toJson(rsu);
        exMsg += "\n";
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);

        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verify(mockActiveTimHoldingService).getActiveTimHoldingForRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService, mockRsuService,
                mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_RsuNewFailIndices() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        String[] rsuRoutes = new String[] { "I 80" };
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        doReturn(new String[] { "1234" }).when(mockDataFrameService).getItisCodesForDataFrameId(any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(new TimQuery());
        when(mockOdeService.findFirstAvailableIndexWithRsuIndex(any())).thenReturn(null);

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Gson gson = new Gson();
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        var exMsg = "Unable to find an available index for RSU " + gson.toJson(rsu);
        exMsg += "\n";
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);

        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verify(mockActiveTimHoldingService).getActiveTimHoldingForRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService, mockRsuService,
                mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_RsuNewInsertFail() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        String[] rsuRoutes = new String[] { "I 80" };
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        doReturn(new String[] { "1234" }).when(mockDataFrameService).getItisCodesForDataFrameId(any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(new TimQuery());
        when(mockOdeService.findFirstAvailableIndexWithRsuIndex(any())).thenReturn(1);
        doReturn("exception").when(mockOdeService).sendNewTimToRsu(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        var exMsg = "exception";
        exMsg += "\n";
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);

        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verify(mockActiveTimHoldingService).getActiveTimHoldingForRsu(any());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockOdeService).sendNewTimToRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService, mockRsuService,
                mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_RsuNewSuccess() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        String[] rsuRoutes = new String[] { "I 80" };
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        doReturn(new String[] { "1234" }).when(mockDataFrameService).getItisCodesForDataFrameId(any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(new TimQuery());
        when(mockOdeService.findFirstAvailableIndexWithRsuIndex(any())).thenReturn(1);

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verify(mockActiveTimHoldingService).getActiveTimHoldingForRsu(any());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockOdeService).sendNewTimToRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService, mockRsuService,
                mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_SdxNewFail() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        String[] rsuRoutes = new String[] { "I 25" };
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        doReturn(new String[] { "1234" }).when(mockDataFrameService).getItisCodesForDataFrameId(any());
        doReturn("exception").when(mockOdeService).sendNewTimToSdw(any(), any(), any());
        doReturn("recId").when(mockSdwService).getNewRecordId();

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, "exception"), ex);

        verify(mockRegionService).updateRegionName(any(), any());
        verify(mockActiveTimService).updateActiveTim_SatRecordId(any(), any());
        verify(mockSdwService).getSdwDataByRecordId(any());
        verify(mockSdwService).getNewRecordId();
        verifyNoInteractions(mockPathNodeXYService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService, mockRsuService,
                mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_SdxNewSuccess() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        String[] rsuRoutes = new String[] { "I 25" };
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        doReturn(new String[] { "1234" }).when(mockDataFrameService).getItisCodesForDataFrameId(any());
        doReturn("").when(mockOdeService).sendNewTimToSdw(any(), any(), any());
        doReturn("recId").when(mockSdwService).getNewRecordId();

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verify(mockRegionService).updateRegionName(any(), any());
        verify(mockActiveTimService).updateActiveTim_SatRecordId(any(), any());
        verify(mockSdwService).getSdwDataByRecordId(any());
        verify(mockSdwService).getNewRecordId();
        verifyNoInteractions(mockPathNodeXYService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService, mockRsuService,
                mockOdeService, mockActiveTimHoldingService);

    }

    @Test
    public void resubmitToOde_SdxExistingFail() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        String[] rsuRoutes = new String[] { "I 25" };
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        doReturn(new String[] { "1234" }).when(mockDataFrameService).getItisCodesForDataFrameId(any());
        doReturn("exception").when(mockOdeService).updateTimOnSdw(any());

        var asdd = new AdvisorySituationDataDeposit();
        asdd.setTimeToLive(TimeToLive.Day);
        doReturn(asdd).when(mockSdwService).getSdwDataByRecordId(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, "exception"), ex);
        verify(mockSdwService).getSdwDataByRecordId(any());
        verifyNoInteractions(mockPathNodeXYService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService, mockRsuService,
                mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_SdxExistingSuccess() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<Long>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");

        List<Milepost> mps = new ArrayList<Milepost>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        String[] rsuRoutes = new String[] { "I 25" };
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        doReturn(new String[] { "1234" }).when(mockDataFrameService).getItisCodesForDataFrameId(any());
        doReturn("").when(mockOdeService).updateTimOnSdw(any());

        var asdd = new AdvisorySituationDataDeposit();
        asdd.setTimeToLive(TimeToLive.Day);
        doReturn(asdd).when(mockSdwService).getSdwDataByRecordId(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verify(mockSdwService).getSdwDataByRecordId(any());
        verifyNoInteractions(mockPathNodeXYService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService, mockRsuService,
                mockOdeService, mockActiveTimHoldingService);
    }

}
