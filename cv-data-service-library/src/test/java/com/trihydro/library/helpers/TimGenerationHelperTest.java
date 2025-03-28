package com.trihydro.library.helpers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.trihydro.library.exceptionhandlers.IdenticalPointsExceptionHandler;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimError;
import com.trihydro.library.model.ActiveTimErrorType;
import com.trihydro.library.model.ActiveTimValidationResult;
import com.trihydro.library.model.AdvisorySituationDataDeposit;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.ResubmitTimException;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.TimUpdateModel;
import com.trihydro.library.model.TimeToLive;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;
import com.trihydro.library.model.WydotTravelerInputData;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimGenerationHelperTest {
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
    @Mock
    private SnmpHelper mockSnmpHelper;
    @Mock
    private RegionNameTrimmer mockRegionNameTrimmer;
    @Mock
    private IdenticalPointsExceptionHandler mockIdenticalPointsExceptionHandler;

    private static final Long activeTimId = -1L;
    private TimUpdateModel tum;

    @InjectMocks
    private TimGenerationHelper uut;

    @Captor
    private ArgumentCaptor<WydotTravelerInputData> timCaptor;

    @BeforeEach
    public void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterEach
    public void teardown() {
        TimeZone.setDefault(TimeZone.getTimeZone(java.time.ZoneId.systemDefault()));
    }

    @Test
    public void resubmitToOde_EmptyList() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();

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
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        doReturn(null).when(mockActiveTimService).getUpdateModelFromActiveTimId(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        verifyNoInteractions(mockDataFrameService, mockPathNodeXYService, mockMilepostService,
            mockMilepostReduction, mockRegionService, mockRsuService, mockConfig, mockOdeService,
            mockActiveTimHoldingService, mockSdwService);
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = "Failed to get Update Model from active tim";
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);
    }

    @Test
    public void resubmitToOde_NoMileposts() {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();

        List<Milepost> mps = new ArrayList<>();
        doReturn(mps).when(mockMilepostService).getMilepostsByStartEndPointDirection(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg =
            String.format("Unable to resubmit TIM, less than 2 mileposts found for Active_Tim %d",
                activeTimId);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);
        verifyNoInteractions(mockDataFrameService, mockPathNodeXYService, mockRegionService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService, mockSdwService);
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction);
    }

    @Test
    public void resubmitToOde_DataFrameException() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = String.format("Failed to instantiate TIM for active_tim_id %s", activeTimId);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);

        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockOdeService,
            mockActiveTimHoldingService, mockSdwService, mockRsuService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService);
    }

    @Test
    public void resubmitToOde_RsuException() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        String[] rsuRoutes = new String[] {"I 80"};
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

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
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockOdeService,
            mockActiveTimHoldingService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService);
    }

    @Test
    public void resubmitToOde_RsuExistingSuccess() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        String[] rsuRoutes = new String[] {"I 80"};
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        List<WydotRsuTim> wydotRsus = new ArrayList<>();
        var wydotRsuTim = new WydotRsuTim();
        wydotRsuTim.setIndex(-1);
        wydotRsus.add(wydotRsuTim);
        doReturn(wydotRsus).when(mockRsuService).getFullRsusTimIsOn(any());
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).deleteTimFromRsu(any(), any());
        verify(mockOdeService).sendNewTimToRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockActiveTimHoldingService,
            mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService);
    }

    @Test
    public void resubmitToOde_RsuNewFailTimQuery() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        String[] rsuRoutes = new String[] {"I 80"};
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(
            null);

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Gson gson = new Gson();
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        var exMsg = "Returning without sending TIM to RSU. submitTimQuery failed for RSU " +
            gson.toJson(rsu);
        exMsg += "\n";
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);

        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_RsuNewFailIndices() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        String[] rsuRoutes = new String[] {"I 80"};
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(
            new TimQuery());
        when(mockOdeService.findFirstAvailableIndexWithRsuIndex(any())).thenReturn(null);

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

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
        verify(mockRsuService).getActiveRsuTimIndexes(any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verify(mockActiveTimHoldingService).getActiveTimHoldingForRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_RsuNewInsertFail() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        String[] rsuRoutes = new String[] {"I 80"};
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(
            new TimQuery());
        when(mockOdeService.findFirstAvailableIndexWithRsuIndex(any())).thenReturn(1);
        doReturn("exception").when(mockOdeService).sendNewTimToRsu(any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

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
        verify(mockRsuService).getActiveRsuTimIndexes(any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verify(mockActiveTimHoldingService).getActiveTimHoldingForRsu(any());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockOdeService).sendNewTimToRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_RsuNewSuccess() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        String[] rsuRoutes = new String[] {"I 80"};
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(
            new TimQuery());
        when(mockOdeService.findFirstAvailableIndexWithRsuIndex(any())).thenReturn(1);

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        verify(mockRsuService).getActiveRsuTimIndexes(any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verify(mockActiveTimHoldingService).getActiveTimHoldingForRsu(any());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockOdeService).sendNewTimToRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_SdxNewFail() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("AA123456");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());

        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        doReturn("exception").when(mockOdeService).updateTimOnSdw(any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, "exception"), ex);
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verifyNoInteractions(mockPathNodeXYService);
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_SdxNewSuccess() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());

        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        doReturn("").when(mockOdeService).updateTimOnSdw(any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verifyNoInteractions(mockPathNodeXYService);
        verify(mockOdeService).updateTimOnSdw(any());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);

    }

    @Test
    public void resetTimStartTimAndResubmitToOde_updatesStartTime()
        throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");
        tum.setStartDateTime("");

        // Given a TIM with a durationTime of 32000 and a startTime 1 second ago
        tum.setDurationTime(32000);
        var oldStartTime = Instant.now().minusSeconds(1);
        tum.setStartDate_Timestamp(new Timestamp(oldStartTime.toEpochMilli()));

        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        doReturn("").when(mockOdeService).updateTimOnSdw(any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        uut.resetTimStartTimeAndResubmitToOde(activeTimIds);

        // Assert
        verify(mockOdeService).updateTimOnSdw(timCaptor.capture());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        var timSent = timCaptor.getValue();
        var dataFrame = timSent.getTim().getDataframes()[0];

        Assertions.assertNotNull(dataFrame.getStartDateTime());
        var newStartTime = Instant.parse(dataFrame.getStartDateTime());

        // A newer startTime (now) should have been used
        Assertions.assertTrue(newStartTime.getEpochSecond() > oldStartTime.getEpochSecond());
        // Duration Time should still be 32000 since no end was specified
        Assertions.assertEquals(32000, dataFrame.getDurationTime());
    }

    @Test
    public void resubmitToOde_usesOldStartTime() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");
        tum.setStartDateTime("");
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        // Given a TIM with a durationTime of an hour
        var originalStartTime = Instant.parse("2021-01-01T00:00:00.000Z");
        tum.setDurationTime(60);
        tum.setEndDateTime("2021-01-01T01:00:00.000Z");
        tum.setStartDate_Timestamp(new Timestamp(originalStartTime.toEpochMilli()));

        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        doReturn("").when(mockOdeService).updateTimOnSdw(any());
        doReturn(60).when(mockUtility).getMinutesDurationBetweenTwoDates(anyString(), anyString());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        uut.resubmitToOde(activeTimIds);

        // Assert
        verify(mockOdeService).updateTimOnSdw(timCaptor.capture());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        var timSent = timCaptor.getValue();
        var dataFrame = timSent.getTim().getDataframes()[0];

        Assertions.assertNotNull(dataFrame.getStartDateTime());
        var newStartTime = Instant.parse(dataFrame.getStartDateTime());

        // The original startTime was used, and the TIM still has a duration of 60 minutes
        Assertions.assertEquals(originalStartTime.getEpochSecond(), newStartTime.getEpochSecond());
        Assertions.assertEquals(60, dataFrame.getDurationTime());
    }

    @Test
    public void c_updatesDurationTimeToFiveMinutes() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");
        tum.setStartDateTime("");

        // Given a TIM with a durationTime of an hour
        var originalStartTime = Instant.parse("2021-01-01T00:00:00.000Z");
        tum.setDurationTime(60);
        tum.setEndDateTime("2021-01-01T01:00:00.000Z");
        tum.setStartDate_Timestamp(new Timestamp(originalStartTime.toEpochMilli()));

        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        doReturn("").when(mockOdeService).updateTimOnSdw(any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        uut.expireTimAndResubmitToOde(activeTimIds);

        // Assert
        verify(mockOdeService).updateTimOnSdw(timCaptor.capture());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        var timSent = timCaptor.getValue();
        var dataFrame = timSent.getTim().getDataframes()[0];

        Assertions.assertNotNull(dataFrame.getDurationTime());

        // Duration Time should be 5 since resetExpirationTime is set to True
        Assertions.assertEquals(5, dataFrame.getDurationTime());
    }

    @Test
    public void resubmitToOde_SdxExistingFail() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());

        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        doReturn("exception").when(mockOdeService).updateTimOnSdw(any());

        var asdd = new AdvisorySituationDataDeposit();
        asdd.setTimeToLive(TimeToLive.Day);

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, "exception"), ex);
        verifyNoInteractions(mockPathNodeXYService);
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_SdxExistingSuccess() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());

        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        doReturn("").when(mockOdeService).updateTimOnSdw(any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verifyNoInteractions(mockPathNodeXYService);
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_IdenticalPointsException_SuccessfulRecovery() throws Utility.IdenticalPointsException {
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");
        tum.setSatRecordId("satRecordId");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());

        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());
        doReturn("").when(mockOdeService).updateTimOnSdw(any());

        doThrow(new Utility.IdenticalPointsException()).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        Milepost anchor = new Milepost();
        doReturn(anchor).when(mockIdenticalPointsExceptionHandler).recoverFromIdenticalPointsException(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verifyNoInteractions(mockPathNodeXYService);
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void resubmitToOde_IdenticalPointsException_FailureToRecover() throws Utility.IdenticalPointsException { // TODO: fix unit test failing after introduction of IdenticalPointsExceptionHandler class
        // Arrange
        List<Long> activeTimIds = new ArrayList<>();
        activeTimIds.add(-1L);
        setupActiveTimModel();
        setupMilepostReturn();

        doThrow(new Utility.IdenticalPointsException()).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        doReturn(null).when(mockIdenticalPointsExceptionHandler).recoverFromIdenticalPointsException(any());

        // Act
        var exceptions = uut.resubmitToOde(activeTimIds);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, String.format("Unable to resubmit TIM, identical points found while calculating anchor point for Active_Tim %d", activeTimId)), ex);
        verifyNoInteractions(mockPathNodeXYService);
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void updateAndResubmitToOde_nullValidationResults() {
        // Arrange

        // Act
        var exceptions = uut.updateAndResubmitToOde(null);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verifyNoInteractions(mockDataFrameService, mockPathNodeXYService, mockRegionService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService, mockSdwService,
            mockMilepostService, mockMilepostReduction, mockActiveTimService);
    }

    @Test
    public void updateAndResubmitToOde_emptyValidationResults() {
        // Arrange

        // Act
        var exceptions = uut.updateAndResubmitToOde(new ArrayList<>());

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verifyNoInteractions(mockDataFrameService, mockPathNodeXYService, mockRegionService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService, mockSdwService,
            mockMilepostService, mockMilepostReduction, mockActiveTimService);
    }

    @Test
    public void updateAndResubmitToOde_nullTum() {
        // Arrange
        doReturn(null).when(mockActiveTimService).getUpdateModelFromActiveTimId(any());

        // Act
        var exceptions = uut.updateAndResubmitToOde(getValidationResults());

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = "Failed to get Update Model from active tim";
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);
        verify(mockActiveTimService).getUpdateModelFromActiveTimId(any());
        verifyNoInteractions(mockDataFrameService, mockPathNodeXYService, mockRegionService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService, mockSdwService,
            mockMilepostService, mockMilepostReduction);

    }

    @Test
    public void updateAndResubmitToOde_noMileposts() {
        // Arrange
        setupActiveTimModel();
        List<Milepost> mps = new ArrayList<>();
        doReturn(mps).when(mockMilepostService).getMilepostsByStartEndPointDirection(any());

        // Act
        var exceptions = uut.updateAndResubmitToOde(getValidationResults());

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = String.format(
            "Unable to resubmit TIM, less than 2 mileposts found to determine service area for Active_Tim %d",
            activeTimId);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);
        verifyNoInteractions(mockDataFrameService, mockPathNodeXYService, mockRegionService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService, mockSdwService);
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction);

    }

    @Test
    public void updateAndResubmitToOde_RsuNewTimFail_EndPointMps()
        throws Utility.IdenticalPointsException {
        // Arrange
        setupActiveTimModel();
        setupMilepostReturnSecondFail();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());

        var validationResults = getValidationResults();
        var errors = new ArrayList<ActiveTimError>();
        Coordinate c = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        var gson = new Gson();
        errors.add(new ActiveTimError(ActiveTimErrorType.endPoint, "timValue", gson.toJson(c)));
        validationResults.get(0).setErrors(errors);

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.updateAndResubmitToOde(validationResults);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = String.format(
            "Unable to resubmit TIM, less than 2 mileposts found to determine service area for Active_Tim %d",
            activeTimId);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockMilepostService, times(2)).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService);
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService,
            mockOdeService, mockActiveTimHoldingService, mockRsuService);
    }

    @Test
    public void updateAndResubmitToOde_RsuNewTimFail_EndTimeParse()
        throws Utility.IdenticalPointsException {
        // Arrange
        setupActiveTimModel();
        setupMilepostReturnSecondFail();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());

        var validationResults = getValidationResults();
        var errors = new ArrayList<ActiveTimError>();
        errors.add(new ActiveTimError(ActiveTimErrorType.endTime, "timValue", "badTimeValue"));
        validationResults.get(0).setErrors(errors);

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.updateAndResubmitToOde(validationResults);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = "Failed to parse associated FEU date: badTimeValue";
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService);
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService,
            mockOdeService, mockActiveTimHoldingService, mockRsuService);

    }

    @Test
    public void updateAndResubmitToOde_RsuNewTimFail_StartPointMps()
        throws Utility.IdenticalPointsException {
        // Arrange
        setupActiveTimModel();
        setupMilepostReturnSecondFail();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());

        var validationResults = getValidationResults();
        var errors = new ArrayList<ActiveTimError>();
        Coordinate c = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        var gson = new Gson();
        errors.add(new ActiveTimError(ActiveTimErrorType.startPoint, "timValue", gson.toJson(c)));
        validationResults.get(0).setErrors(errors);

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.updateAndResubmitToOde(validationResults);

        // Assert
        Assertions.assertEquals(1, exceptions.size());
        var ex = exceptions.get(0);
        String exMsg = String.format(
            "Unable to resubmit TIM, less than 2 mileposts found to determine service area for Active_Tim %d",
            activeTimId);
        Assertions.assertEquals(new ResubmitTimException(activeTimId, exMsg), ex);
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockMilepostService, times(2)).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService);
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService,
            mockOdeService, mockActiveTimHoldingService, mockRsuService);
    }

    @Test
    public void updateAndResubmitToOde_RsuNewTimSuccess_StartPoint()
        throws Utility.IdenticalPointsException {
        // Arrange
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        String[] rsuRoutes = new String[] {"I 80"};
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        var validationResults = getValidationResults();
        var errors = new ArrayList<ActiveTimError>();
        Coordinate c = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        var gson = new Gson();
        errors.add(new ActiveTimError(ActiveTimErrorType.startPoint, "timValue", gson.toJson(c)));
        validationResults.get(0).setErrors(errors);

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(
            new TimQuery());
        when(mockOdeService.findFirstAvailableIndexWithRsuIndex(any())).thenReturn(1);

        // Act
        var exceptions = uut.updateAndResubmitToOde(validationResults);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        verify(mockRsuService).getActiveRsuTimIndexes(any());
        verify(mockDataFrameService, times(2)).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verify(mockActiveTimHoldingService).getActiveTimHoldingForRsu(any());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockOdeService).sendNewTimToRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService, times(2)).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction, times(2)).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void updateAndResubmitToOde_RsuNewTimSuccess_EndPoint()
        throws Utility.IdenticalPointsException {
        // Arrange
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        String[] rsuRoutes = new String[] {"I 80"};
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());

        var validationResults = getValidationResults();
        var errors = new ArrayList<ActiveTimError>();
        Coordinate c = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2));
        var gson = new Gson();
        errors.add(new ActiveTimError(ActiveTimErrorType.endPoint, "timValue", gson.toJson(c)));
        validationResults.get(0).setErrors(errors);

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(
            new TimQuery());
        when(mockOdeService.findFirstAvailableIndexWithRsuIndex(any())).thenReturn(1);

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.updateAndResubmitToOde(validationResults);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        verify(mockRsuService).getActiveRsuTimIndexes(any());
        verify(mockDataFrameService, times(2)).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verify(mockActiveTimHoldingService).getActiveTimHoldingForRsu(any());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockOdeService).sendNewTimToRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService, times(2)).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction, times(2)).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void updateAndResubmitToOde_RsuNewTimSuccess_EndTime()
        throws Utility.IdenticalPointsException {
        // Arrange
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        String[] rsuRoutes = new String[] {"I 80"};
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());

        var validationResults = getValidationResults();
        var errors = new ArrayList<ActiveTimError>();
        errors.add(
            new ActiveTimError(ActiveTimErrorType.endTime, "timValue", "2020-12-08 09:31:00"));
        validationResults.get(0).setErrors(errors);

        List<WydotRsu> dbRsus = new ArrayList<>();
        var rsu = new WydotRsu();
        rsu.setRsuTarget("10.10.10.10");
        dbRsus.add(rsu);
        doReturn(dbRsus).when(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class))).thenReturn(
            new TimQuery());
        when(mockOdeService.findFirstAvailableIndexWithRsuIndex(any())).thenReturn(1);

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.updateAndResubmitToOde(validationResults);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockRsuService).getRsusByLatLong(any(), any(), any(), any());
        verify(mockRsuService).getActiveRsuTimIndexes(any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).submitTimQuery(isA(WydotRsu.class), isA(Integer.class));
        verify(mockActiveTimHoldingService).getActiveTimHoldingForRsu(any());
        verify(mockActiveTimHoldingService).insertActiveTimHolding(any());
        verify(mockOdeService).sendNewTimToRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void updateAndResubmitToOde_RsuUpdateTimSuccess_ItisCodes()
        throws Utility.IdenticalPointsException {
        // Arrange
        setupActiveTimModel();
        setupMilepostReturn();
        tum.setRoute("I 80");

        List<Milepost> mps = new ArrayList<>();
        mps.add(new Milepost());
        doReturn(mps).when(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        String[] rsuRoutes = new String[] {"I 80"};
        doReturn(rsuRoutes).when(mockConfig).getRsuRoutes();
        doReturn(new String[] {"1234"}).when(mockDataFrameService)
            .getItisCodesForDataFrameId(any());

        var validationResults = getValidationResults();
        var errors = new ArrayList<ActiveTimError>();
        errors.add(new ActiveTimError(ActiveTimErrorType.itisCodes, "timValue", "{1234,4321}"));
        validationResults.get(0).setErrors(errors);

        List<WydotRsuTim> wydotRsus = new ArrayList<>();
        var wydotRsuTim = new WydotRsuTim();
        wydotRsuTim.setIndex(-1);
        wydotRsus.add(wydotRsuTim);
        doReturn(wydotRsus).when(mockRsuService).getFullRsusTimIsOn(any());

        doReturn(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(2))).when(mockUtility)
            .calculateAnchorCoordinate(any(), any());

        // Act
        var exceptions = uut.updateAndResubmitToOde(validationResults);

        // Assert
        Assertions.assertEquals(0, exceptions.size());
        verify(mockRsuService).getFullRsusTimIsOn(any());
        verify(mockDataFrameService).getItisCodesForDataFrameId(any());
        verify(mockOdeService).deleteTimFromRsu(any(), any());
        verify(mockOdeService).sendNewTimToRsu(any());
        verifyNoInteractions(mockPathNodeXYService, mockRegionService, mockSdwService);

        verify(mockMilepostService).getMilepostsByStartEndPointDirection(any());
        verify(mockMilepostReduction).applyMilepostReductionAlgorithm(any(), any());
        verifyNoMoreInteractions(mockMilepostService, mockMilepostReduction, mockDataFrameService,
            mockRsuService, mockOdeService, mockActiveTimHoldingService);
    }

    @Test
    public void isValidTim_TRUE() {
        // Arrange
        var tum = new TimUpdateModel();
        tum.setStartPoint(new Coordinate(BigDecimal.valueOf(1L), BigDecimal.valueOf(2L)));
        tum.setDirection("I");
        tum.setRoute("I 80");

        // Act
        var success = uut.isValidTim(tum);

        // Assert
        Assertions.assertTrue(success);
    }

    @Test
    public void isValidTim_FALSE_StartPoint() {
        // Arrange
        var tum = new TimUpdateModel();
        tum.setDirection("I");
        tum.setRoute("I 80");

        // Act
        var success = uut.isValidTim(tum);

        // Assert
        Assertions.assertFalse(success);
    }

    @Test
    public void isValidTim_FALSE_Direction() {
        // Arrange
        var tum = new TimUpdateModel();
        tum.setStartPoint(new Coordinate(BigDecimal.valueOf(1L), BigDecimal.valueOf(2L)));
        tum.setRoute("I 80");

        // Act
        var success = uut.isValidTim(tum);

        // Assert
        Assertions.assertFalse(success);
    }

    @Test
    public void isValidTim_FALSE_Route() {
        // Arrange
        var tum = new TimUpdateModel();
        tum.setStartPoint(new Coordinate(BigDecimal.valueOf(1L), BigDecimal.valueOf(2L)));
        tum.setDirection("I");

        // Act
        var success = uut.isValidTim(tum);

        // Assert
        Assertions.assertFalse(success);
    }

    private void setupActiveTimModel() {
        tum = new TimUpdateModel();
        tum.setActiveTimId(activeTimId);
        tum.setStartPoint(new Coordinate(BigDecimal.valueOf(-1L), BigDecimal.valueOf(-2L)));
        tum.setEndPoint(new Coordinate(BigDecimal.valueOf(-3L), BigDecimal.valueOf(-4L)));

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
        tum.setRegionDescription("descrip");// String
        tum.setLaneWidth(BigDecimal.valueOf(50L));// BigDecimal
        tum.setAnchorLat(BigDecimal.valueOf(-1L));// BigDecimal
        tum.setAnchorLong(BigDecimal.valueOf(-2L));// BigDecimal
        tum.setRegionDirection("I");// String

        tum.setClosedPath(false);
        tum.setRoute("I 80");
        tum.setDirection("I");

        doReturn(tum).when(mockActiveTimService).getUpdateModelFromActiveTimId(any());
    }

    private List<Milepost> getAllMps() {
        List<Milepost> allMps = new ArrayList<>();
        var latitude = BigDecimal.valueOf(-1L);
        var longitude = BigDecimal.valueOf(-2L);
        var mp = new Milepost();
        mp.setCommonName("I 80");
        mp.setDirection("I");
        mp.setLatitude(latitude);
        mp.setLongitude(longitude);
        allMps.add(mp);

        var mp2 = new Milepost();
        mp.setCommonName("I 80");
        mp.setDirection("D");
        mp.setLatitude(latitude);
        mp.setLongitude(longitude);
        allMps.add(mp2);

        return allMps;
    }

    private void setupMilepostReturn() {
        doReturn(getAllMps()).when(mockMilepostService).getMilepostsByStartEndPointDirection(any());
    }

    private void setupMilepostReturnSecondFail() {
        when(mockMilepostService.getMilepostsByStartEndPointDirection(any())).thenReturn(
            getAllMps()).thenReturn(new ArrayList<>());
    }

    private List<ActiveTimValidationResult> getValidationResults() {
        ActiveTimValidationResult validationResult = new ActiveTimValidationResult();
        validationResult.setActiveTim(getActiveTim());

        return Collections.singletonList(validationResult);
    }

    private ActiveTim getActiveTim() {
        var tim = new ActiveTim();
        tim.setActiveTimId(-1L);
        return tim;
    }
}