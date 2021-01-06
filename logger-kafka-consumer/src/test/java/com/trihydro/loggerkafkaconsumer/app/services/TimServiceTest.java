package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.model.CertExpirationModel;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeDataType;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeLogMsgMetadataLocation;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.OdeRequestMsgMetadata;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.SerialId;
import us.dot.its.jpo.ode.plugin.RoadSideUnit.RSU;
import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Geometry;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Path;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

public class TimServiceTest extends TestBase<TimService> {

    @Mock
    private ActiveTimService mockActiveTimService;
    @Spy
    private TimOracleTables mockTimOracleTables = new TimOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Mock
    PathService mockPathService;
    @Mock
    RegionService mockRegionService;
    @Mock
    DataFrameService mockDataFrameService;
    @Mock
    RsuService mockRsuService;
    @Mock
    TimTypeService mockTts;
    @Mock
    ItisCodeService mockItisCodesService;
    @Mock
    TimRsuService mockTimRsuService;
    @Mock
    DataFrameItisCodeService mockDataFrameItisCodeService;
    @Mock
    PathNodeXYService mockPathNodeXYService;
    @Mock
    NodeXYService mockNodeXYService;
    @Mock
    private Utility mockUtility;
    @Mock
    private ActiveTimHoldingService mockActiveTimHoldingService;
    @Mock
    private NodeLLService mockNodeLLService;
    @Mock
    private PathNodeLLService mockPathNodeLLService;

    private WydotRsu rsu;
    private Long pathId = -99l;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockActiveTimService, mockTimOracleTables, mockSqlNullHandler, mockPathService,
                mockRegionService, mockDataFrameService, mockRsuService, mockTts, mockItisCodesService,
                mockTimRsuService, mockDataFrameItisCodeService, mockPathNodeXYService, mockNodeXYService, mockUtility,
                mockActiveTimHoldingService, mockPathNodeLLService, mockNodeLLService);

        ArrayList<WydotRsu> rsus = new ArrayList<>();
        rsu = new WydotRsu();
        rsu.setRsuId(-1);
        rsu.setRsuIndex(99);
        rsu.setLatitude(BigDecimal.valueOf(-1));
        rsu.setLongitude(BigDecimal.valueOf(-2));
        rsu.setMilepost(99d);
        rsu.setRsuTarget("rsuTarget");
        rsus.add(rsu);
        lenient().doReturn(rsus).when(mockRsuService).getRsus();

        lenient().doReturn(pathId).when(mockPathService).InsertPath();
    }

    @Test
    public void addActiveTimToOracleDB_newRsuTimSUCCESS() {
        // Arrange
        OdeData odeData = getOdeData_requestMsgData();
        ActiveTim aTim = getActiveTim();
        Long timId = -1l;
        Long dataFrameId = -2l;
        DataFrame[] dFrames = ((OdeTimPayload) odeData.getPayload()).getTim().getDataframes();

        doReturn(aTim).when(uut).setActiveTimByRegionName(anyString());
        doReturn(null).when(uut).getTimId(nullable(String.class), any());
        doReturn(timId).when(uut).AddTim(odeData.getMetadata(), null, ((OdeTimPayload) odeData.getPayload()).getTim(),
                null, null, null, aTim.getSatRecordId(), dFrames[0].getRegions()[0].getName());
        doReturn(dataFrameId).when(mockDataFrameService).AddDataFrame(dFrames[0], timId);
        doNothing().when(uut).addRegion(any(), any());
        doNothing().when(uut).addDataFrameItis(any(), any());
        doReturn(getActiveTimHolding()).when(mockActiveTimHoldingService).getRsuActiveTimHolding(anyString(),
                anyString(), anyString());

        // Act
        uut.addActiveTimToOracleDB(odeData);

        // Assert
        verify(uut).AddTim(odeData.getMetadata(), null, ((OdeTimPayload) odeData.getPayload()).getTim(), null, null,
                null, aTim.getSatRecordId(), dFrames[0].getRegions()[0].getName());
        verify(mockDataFrameService).AddDataFrame(dFrames[0], timId);
        verify(uut).addRegion(dFrames[0], dataFrameId);
        verify(uut).addDataFrameItis(dFrames[0], dataFrameId);
        verify(mockActiveTimHoldingService).getRsuActiveTimHolding(aTim.getClientId(), aTim.getDirection(),
                aTim.getRsuTarget());
        verify(mockActiveTimService).insertActiveTim(any());
        verify(mockActiveTimHoldingService).deleteActiveTimHolding(-1l);
    }

    @Test
    public void addActiveTimToOracleDB_existingRsuTimSUCCESS() {
        // Arrange
        OdeData odeData = getOdeData_requestMsgData();
        ActiveTim aTim = getActiveTim();

        var ath = getActiveTimHolding();
        doReturn(aTim).when(uut).setActiveTimByRegionName(anyString());
        doReturn(-1l).when(uut).getTimId(nullable(String.class), any());
        doReturn(ath).when(mockActiveTimHoldingService).getRsuActiveTimHolding(anyString(),
                anyString(), anyString());

        // Act
        uut.addActiveTimToOracleDB(odeData);

        // Assert
        verify(uut, never()).AddTim(any(), any(), any(), any(), any(), any(), any(), any());
        verify(mockDataFrameService, never()).AddDataFrame(any(), any());
        verify(uut, never()).addRegion(any(), any());
        verify(uut, never()).addDataFrameItis(any(), any());
        verify(mockActiveTimHoldingService).getRsuActiveTimHolding(aTim.getClientId(), aTim.getDirection(),
                aTim.getRsuTarget());
        verify(mockActiveTimHoldingService).deleteActiveTimHolding(-1l);

        var captor = ArgumentCaptor.forClass(ActiveTim.class);
        verify(mockActiveTimService).insertActiveTim(captor.capture());

        var insertedRecord = captor.getValue();
        assertEquals(ath.getStartPoint(), insertedRecord.getStartPoint());
        assertEquals(ath.getEndPoint(), insertedRecord.getEndPoint());
        assertEquals(ath.getProjectKey(), insertedRecord.getProjectKey());
    }

    @Test
    public void addActiveTimToOracleDB_newSDXTimSUCCESS() {
        // Arrange
        OdeData odeData = getOdeData_requestMsgData();
        ActiveTim aTim = getActiveTim();
        aTim.setSatRecordId("satRecordId");
        aTim.setRsuTarget(null);
        ((OdeRequestMsgMetadata) odeData.getMetadata()).getRequest().setRsus(new RSU[0]);
        Long timId = -1l;
        Long dataFrameId = -2l;
        DataFrame[] dFrames = ((OdeTimPayload) odeData.getPayload()).getTim().getDataframes();

        doReturn(aTim).when(uut).setActiveTimByRegionName(anyString());
        doReturn(null).when(uut).getTimId(nullable(String.class), any());
        doReturn(timId).when(uut).AddTim(odeData.getMetadata(), null, ((OdeTimPayload) odeData.getPayload()).getTim(),
                null, null, null, aTim.getSatRecordId(), dFrames[0].getRegions()[0].getName());
        doReturn(dataFrameId).when(mockDataFrameService).AddDataFrame(dFrames[0], timId);
        doNothing().when(uut).addRegion(any(), any());
        doNothing().when(uut).addDataFrameItis(any(), any());
        doReturn(false).when(uut).updateTimSatRecordId(anyLong(), anyString());
        doReturn(getActiveTimHolding()).when(mockActiveTimHoldingService).getSdxActiveTimHolding(anyString(),
                anyString(), anyString());

        // Act
        uut.addActiveTimToOracleDB(odeData);

        // Assert
        verify(uut).AddTim(odeData.getMetadata(), null, ((OdeTimPayload) odeData.getPayload()).getTim(), null, null,
                null, aTim.getSatRecordId(), dFrames[0].getRegions()[0].getName());
        verify(mockDataFrameService).AddDataFrame(dFrames[0], timId);
        verify(uut).addRegion(dFrames[0], dataFrameId);
        verify(uut).addDataFrameItis(dFrames[0], dataFrameId);
        verify(uut).updateTimSatRecordId(anyLong(), anyString());
        verify(mockActiveTimHoldingService).getSdxActiveTimHolding(aTim.getClientId(), aTim.getDirection(),
                aTim.getSatRecordId());
        verify(mockActiveTimService).insertActiveTim(any());
        verify(mockActiveTimHoldingService).deleteActiveTimHolding(-1l);
    }

    @Test
    public void addActiveTimToOracleDB_existingSDXTimSUCCESS() {
        // Arrange
        OdeData odeData = getOdeData_requestMsgData();
        ActiveTim aTim = getActiveTim();
        aTim.setSatRecordId("satRecordId");
        aTim.setRsuTarget(null);
        ((OdeRequestMsgMetadata) odeData.getMetadata()).getRequest().setRsus(new RSU[0]);

        var ath = getActiveTimHolding();

        doReturn(aTim).when(uut).setActiveTimByRegionName(anyString());
        doReturn(-1l).when(uut).getTimId(nullable(String.class), any());
        doReturn(false).when(uut).updateTimSatRecordId(anyLong(), anyString());
        doReturn(ath).when(mockActiveTimHoldingService).getSdxActiveTimHolding(anyString(),
                anyString(), anyString());

        // Act
        uut.addActiveTimToOracleDB(odeData);

        // Assert
        verify(uut, never()).AddTim(any(), any(), any(), any(), any(), any(), any(), any());
        verify(mockDataFrameService, never()).AddDataFrame(any(), any());
        verify(uut, never()).addRegion(any(), any());
        verify(uut, never()).addDataFrameItis(any(), any());
        verify(mockActiveTimHoldingService).getSdxActiveTimHolding(aTim.getClientId(), aTim.getDirection(),
                aTim.getSatRecordId());
        verify(mockActiveTimHoldingService).deleteActiveTimHolding(-1l);

        var captor = ArgumentCaptor.forClass(ActiveTim.class);
        verify(mockActiveTimService).insertActiveTim(captor.capture());

        var insertedRecord = captor.getValue();
        assertEquals(ath.getStartPoint(), insertedRecord.getStartPoint());
        assertEquals(ath.getEndPoint(), insertedRecord.getEndPoint());
        assertEquals(ath.getProjectKey(), insertedRecord.getProjectKey());
    }

    @Test
    public void addActiveTimToOracleDB_existingRsuTimNoATH() {
        // Arrange
        OdeData odeData = getOdeData_requestMsgData();
        ActiveTim aTim = getActiveTim();

        ActiveTim activeTimDb = new ActiveTim();
        var start = new Coordinate(BigDecimal.valueOf(0), BigDecimal.valueOf(0));
        var end = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(1));
        activeTimDb.setStartPoint(start);
        activeTimDb.setEndPoint(end);
        activeTimDb.setProjectKey(1234);

        doReturn(aTim).when(uut).setActiveTimByRegionName(anyString());
        doReturn(-1l).when(uut).getTimId(nullable(String.class), any());
        doReturn(null).when(mockActiveTimHoldingService).getRsuActiveTimHolding(anyString(),
                anyString(), anyString());
        doReturn(activeTimDb).when(mockActiveTimService).getActiveRsuTim(any(), any(), any());

        // Act
        uut.addActiveTimToOracleDB(odeData);

        // Assert
        verify(uut, never()).AddTim(any(), any(), any(), any(), any(), any(), any(), any());
        verify(mockDataFrameService, never()).AddDataFrame(any(), any());
        verify(uut, never()).addRegion(any(), any());
        verify(uut, never()).addDataFrameItis(any(), any());

        var captor = ArgumentCaptor.forClass(ActiveTim.class);
        verify(mockActiveTimService).updateActiveTim(captor.capture());

        var insertedRecord = captor.getValue();
        assertEquals(start, insertedRecord.getStartPoint());
        assertEquals(end, insertedRecord.getEndPoint());
        assertEquals(1234, (int)insertedRecord.getProjectKey());
    }

    @Test
    public void addActiveTimToOracleDB_existingSDXTimNoATH() {
        // Arrange
        OdeData odeData = getOdeData_requestMsgData();
        ActiveTim aTim = getActiveTim();
        aTim.setSatRecordId("satRecordId");
        aTim.setRsuTarget(null);
        ((OdeRequestMsgMetadata) odeData.getMetadata()).getRequest().setRsus(new RSU[0]);

        ActiveTim activeTimDb = new ActiveTim();
        var start = new Coordinate(BigDecimal.valueOf(0), BigDecimal.valueOf(0));
        var end = new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(1));
        activeTimDb.setStartPoint(start);
        activeTimDb.setEndPoint(end);
        activeTimDb.setProjectKey(1234);

        doReturn(aTim).when(uut).setActiveTimByRegionName(anyString());
        doReturn(-1l).when(uut).getTimId(nullable(String.class), any());
        doReturn(false).when(uut).updateTimSatRecordId(anyLong(), anyString());
        // No ActiveTimHolding record
        doReturn(null).when(mockActiveTimHoldingService).getSdxActiveTimHolding(anyString(),
                anyString(), anyString());
        // But there is an existing ActiveTIm
        doReturn(activeTimDb).when(mockActiveTimService).getActiveSatTim(any(), any());

        // Act
        uut.addActiveTimToOracleDB(odeData);

        // Assert
        verify(uut, never()).AddTim(any(), any(), any(), any(), any(), any(), any(), any());
        verify(mockDataFrameService, never()).AddDataFrame(any(), any());
        verify(uut, never()).addRegion(any(), any());
        verify(uut, never()).addDataFrameItis(any(), any());
        verify(mockActiveTimHoldingService).getSdxActiveTimHolding(aTim.getClientId(), aTim.getDirection(),
                aTim.getSatRecordId());

        var captor = ArgumentCaptor.forClass(ActiveTim.class);
        verify(mockActiveTimService).updateActiveTim(captor.capture());

        var insertedRecord = captor.getValue();
        assertEquals(start, insertedRecord.getStartPoint());
        assertEquals(end, insertedRecord.getEndPoint());
        assertEquals(1234, (int)insertedRecord.getProjectKey());
    }

    @Test
    public void addTimToOracleDB_addTimFAIL() {
        // Arrange
        OdeData odeData = getOdeData();
        doReturn(null).when(uut).AddTim(odeData.getMetadata(),
                ((OdeLogMetadata) odeData.getMetadata()).getReceivedMessageDetails(),
                ((OdeTimPayload) odeData.getPayload()).getTim(),
                ((OdeLogMetadata) odeData.getMetadata()).getRecordType(),
                ((OdeLogMetadata) odeData.getMetadata()).getLogFileName(),
                ((OdeLogMetadata) odeData.getMetadata()).getSecurityResultCode(), null, null);

        // Act
        uut.addTimToOracleDB(odeData);

        // Assert
        verifyNoInteractions(mockDataFrameService);
        verifyNoInteractions(mockRegionService);
        verifyNoInteractions(mockTimRsuService);
        verifyNoInteractions(mockDataFrameItisCodeService);
        // verify only these were called on the uut
        verify(uut).InjectDependencies(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any());
        verify(uut).InjectBaseDependencies(any(), any());
        verify(uut).addTimToOracleDB(odeData);
        verify(uut).AddTim(any(), any(), any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(uut);
    }

    @Test
    public void addTimToOracleDB_SUCCESS() {
        // Arrange
        OdeData odeData = getOdeData();
        Long timId = -1l;
        Long dataFrameId = -2l;
        DataFrame[] dFrames = ((OdeTimPayload) odeData.getPayload()).getTim().getDataframes();
        doReturn(timId).when(uut).AddTim(odeData.getMetadata(),
                ((OdeLogMetadata) odeData.getMetadata()).getReceivedMessageDetails(),
                ((OdeTimPayload) odeData.getPayload()).getTim(),
                ((OdeLogMetadata) odeData.getMetadata()).getRecordType(),
                ((OdeLogMetadata) odeData.getMetadata()).getLogFileName(),
                ((OdeLogMetadata) odeData.getMetadata()).getSecurityResultCode(), null, null);
        doReturn(dataFrameId).when(mockDataFrameService).AddDataFrame(dFrames[0], timId);
        doReturn(pathId).when(mockPathService).InsertPath();
        doReturn(getActiveTim()).when(uut).setActiveTimByRegionName(isA(String.class));

        // Act
        uut.addTimToOracleDB(odeData);

        // Assert
        verify(uut).AddTim(any(), any(), any(), any(), any(), any(), any(), any());
        verify(mockDataFrameService).AddDataFrame(dFrames[0], timId);
        verify(mockPathService).InsertPath();
        verify(mockRegionService).AddRegion(dataFrameId, pathId, dFrames[0].getRegions()[0]);
        verify(mockTimRsuService).AddTimRsu(timId, rsu.getRsuId(), rsu.getRsuIndex());
        verify(mockDataFrameItisCodeService).insertDataFrameItisCode(dataFrameId, dFrames[0].getItems()[0]);
    }

    @Test
    public void addRegion_pathXYSUCCESS() {
        // Arrange
        DataFrame dataFrame = getDataFrames()[0];
        Path path = dataFrame.getRegions()[0].getPath();
        Long dataFrameId = -1l;
        Long nodeXYId = -2l;
        doReturn(pathId).when(mockPathService).InsertPath();
        doReturn(nodeXYId).when(mockNodeXYService).AddNodeXY(isA(OdeTravelerInformationMessage.NodeXY.class));

        // Act
        uut.addRegion(dataFrame, dataFrameId);

        // Assert
        verify(mockPathService).InsertPath();
        verify(mockRegionService).AddRegion(dataFrameId, pathId, dataFrame.getRegions()[0]);
        verify(mockNodeXYService).AddNodeXY(path.getNodes()[0]);
        verify(mockPathNodeXYService).insertPathNodeXY(nodeXYId, pathId);
        verifyNoMoreInteractions(mockRegionService);
    }

    @Test
    public void addRegion_pathLLSUCCESS() {
        // Arrange
        DataFrame dataFrame = getDataFrames()[0];
        Path path = dataFrame.getRegions()[0].getPath();

        // set node-LL
        for (int i = 0; i < path.getNodes().length; i++) {
            path.getNodes()[i].setDelta("node-LL");
        }
        Long dataFrameId = -1l;
        Long nodeLLId = -2l;
        doReturn(pathId).when(mockPathService).InsertPath();
        doReturn(nodeLLId).when(mockNodeLLService).AddNodeLL(isA(OdeTravelerInformationMessage.NodeXY.class));

        // Act
        uut.addRegion(dataFrame, dataFrameId);

        // Assert
        verify(mockPathService).InsertPath();
        verify(mockRegionService).AddRegion(dataFrameId, pathId, dataFrame.getRegions()[0]);
        verify(mockNodeLLService).AddNodeLL(path.getNodes()[0]);
        verify(mockPathNodeLLService).insertPathNodeLL(nodeLLId, pathId);
        verifyNoMoreInteractions(mockRegionService);
    }

    @Test
    public void addRegion_geometrySUCCESS() {
        // Arrange
        DataFrame dataFrame = getDataFrames()[0];
        dataFrame.getRegions()[0].setPath(null);
        dataFrame.getRegions()[0].setGeometry(new Geometry());
        Long dataFrameId = -1l;

        // Act
        uut.addRegion(dataFrame, dataFrameId);

        // Assert
        verify(mockPathService, never()).InsertPath();
        verify(mockRegionService, never()).AddRegion(dataFrameId, pathId, dataFrame.getRegions()[0]);
        verify(mockNodeXYService, never()).AddNodeXY(isA(NodeXY.class));
        verify(mockPathNodeXYService, never()).insertPathNodeXY(any(), any());
        verify(mockRegionService).AddRegion(dataFrameId, null, dataFrame.getRegions()[0]);
        verifyNoMoreInteractions(mockRegionService);
    }

    @Test
    public void addDataFrameItis_noItemsFAIL() {
        // Arrange
        Long dataFrameId = -1l;
        DataFrame dataFrame = new DataFrame();
        // Act
        uut.addDataFrameItis(dataFrame, dataFrameId);

        // Assert
        verifyNoInteractions(mockDataFrameItisCodeService);
    }

    @Test
    public void addDataFrameItis_nonNumericSUCCESS() {
        // Arrange
        Long dataFrameId = -1l;
        DataFrame dataFrame = getDataFrames()[0];
        // Act
        uut.addDataFrameItis(dataFrame, dataFrameId);

        // Assert
        verify(mockDataFrameItisCodeService).insertDataFrameItisCode(dataFrameId, dataFrame.getItems()[0]);
    }

    @Test
    public void addDataFrameItis_numericSUCCESS() {
        // Arrange
        Long dataFrameId = -1l;
        DataFrame dataFrame = getDataFrames()[0];
        dataFrame.setItems(new String[] { "1234" });
        doReturn("test").when(uut).getItisCodeId("1234");

        // Act
        uut.addDataFrameItis(dataFrame, dataFrameId);

        // Assert
        verify(uut).getItisCodeId("1234");
        verify(mockDataFrameItisCodeService).insertDataFrameItisCode(dataFrameId, "test");
    }

    @Test
    public void addDataFrameItis_numericFAIL() {
        // Arrange
        Long dataFrameId = -1l;
        DataFrame dataFrame = getDataFrames()[0];
        dataFrame.setItems(new String[] { "1234" });
        doReturn(null).when(uut).getItisCodeId("1234");

        // Act
        uut.addDataFrameItis(dataFrame, dataFrameId);

        // Assert
        verify(uut).getItisCodeId("1234");
        verifyNoMoreInteractions(mockDataFrameItisCodeService);
    }

    @Test
    public void updateTimSatRecordId_FAIL() throws SQLException {
        // Arrange
        Long timId = -1l;
        String satRecordId = "asdf";
        doThrow(new SQLException()).when(mockPreparedStatement).setString(1, satRecordId);
        // Act
        boolean data = uut.updateTimSatRecordId(timId, satRecordId);

        // Assert
        Assertions.assertFalse(data, "updateTimSatRecordId returned true when failure");
        verify(mockConnection).prepareStatement("update tim set sat_record_id = ? where tim_id = ?");
        verify(mockConnection).close();
    }

    @Test
    public void updateTimSatRecordId_SUCCESS() throws SQLException {
        // Arrange
        Long timId = -1l;
        String satRecordId = "asdf";
        // Act
        boolean data = uut.updateTimSatRecordId(timId, satRecordId);

        // Assert
        Assertions.assertTrue(data, "updateTimSatRecordId returned false when successful");
        verify(mockConnection).prepareStatement("update tim set sat_record_id = ? where tim_id = ?");
        verify(mockPreparedStatement).setString(1, satRecordId);
        verify(mockPreparedStatement).setLong(2, timId);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void setActiveTimByRegionName_regionNameFAIL() {
        // Arrange

        // Act
        ActiveTim data = uut.setActiveTimByRegionName(null);

        // Assert
        Assertions.assertNull(data);
    }

    @Test
    public void setActiveTimByRegionName_RsuSUCCESS() {
        // Arrange
        String regionName = "I_Prairie Center Cir_RSU-10.145.1.100_RC_clientId";
        TimType timType = new TimType();
        timType.setType("RC");
        timType.setTimTypeId(-1l);
        doReturn(timType).when(uut).getTimType("RC");

        // Act
        ActiveTim data = uut.setActiveTimByRegionName(regionName);

        // Assert
        Assertions.assertNotNull(data);
        Assertions.assertNotNull(data.getDirection());
        Assertions.assertNotNull(data.getRoute());
        Assertions.assertNotNull(data.getRsuTarget());
        Assertions.assertNotNull(data.getTimType());
        Assertions.assertNotNull(data.getTimTypeId());
        Assertions.assertNotNull(data.getClientId());
    }

    @Test
    public void setActiveTimByRegionName_SatSUCCESS() {
        // Arrange
        String regionName = "I_Prairie Center Cir_SAT-satId_RC_clientId";
        TimType timType = new TimType();
        timType.setType("RC");
        timType.setTimTypeId(-1l);
        doReturn(timType).when(uut).getTimType("RC");

        // Act
        ActiveTim data = uut.setActiveTimByRegionName(regionName);

        // Assert
        Assertions.assertNotNull(data);
        Assertions.assertNotNull(data.getDirection());
        Assertions.assertNotNull(data.getRoute());
        Assertions.assertNotNull(data.getSatRecordId());
        Assertions.assertNotNull(data.getTimType());
        Assertions.assertNotNull(data.getTimTypeId());
        Assertions.assertNotNull(data.getClientId());
    }

    @Test
    public void getTimType_FAIL() {
        // Arrange
        doReturn(new ArrayList<>()).when(mockTts).getTimTypes();
        // Act
        TimType data = uut.getTimType("timTypeName");

        // Assert
        Assertions.assertNull(data);
    }

    @Test
    public void getTimType_SUCCESS() {
        // Arrange
        List<TimType> timTypes = new ArrayList<TimType>();
        TimType tt = new TimType();
        tt.setType("timTypeName");
        tt.setTimTypeId(-1l);
        timTypes.add(tt);
        doReturn(timTypes).when(mockTts).getTimTypes();
        // Act
        TimType data = uut.getTimType("timTypeName");

        // Assert
        Assertions.assertNotNull(data);
        Assertions.assertEquals(Long.valueOf(-1), data.getTimTypeId());
    }

    @Test
    public void getItisCodeId_FAIL() {
        // Arrange
        doReturn(new ArrayList<>()).when(mockItisCodesService).selectAllItisCodes();

        // Act
        String data = uut.getItisCodeId("1234");

        // Assert
        Assertions.assertNull(data);
    }

    @Test
    public void getItisCodeId_SUCCESS() {
        // Arrange
        List<ItisCode> itisCodes = new ArrayList<ItisCode>();
        ItisCode code = new ItisCode();
        code.setItisCode(1234);
        code.setItisCodeId(-1);
        itisCodes.add(code);
        doReturn(itisCodes).when(mockItisCodesService).selectAllItisCodes();

        // Act
        String data = uut.getItisCodeId("1234");

        // Assert
        Assertions.assertEquals("-1", data);
    }

    @Test
    public void updateActiveTimExpiration_SUCCESS() throws ParseException {
        // Arrange
        var data = new CertExpirationModel();
        doReturn(true).when(mockActiveTimService).updateActiveTimExpiration(any(), any(), any());

        // Act
        var result = uut.updateActiveTimExpiration(data);

        // Assert
        Assertions.assertTrue(result);
    }

    @Test
    public void updateActiveTimExpiration_FAIL() throws ParseException {
         // Arrange
         var data = new CertExpirationModel();
         doReturn(false).when(mockActiveTimService).updateActiveTimExpiration(any(), any(), any());
 
         // Act
         var result = uut.updateActiveTimExpiration(data);
 
         // Assert
         Assertions.assertFalse(result);
    }

    private ActiveTimHolding getActiveTimHolding() {
        ActiveTimHolding ath = new ActiveTimHolding();
        ath.setActiveTimHoldingId(-1l);
        ath.setClientId("clientId");
        ath.setDirection("direction");
        ath.setStartPoint(new Coordinate(BigDecimal.valueOf(-1), BigDecimal.valueOf(-2)));
        ath.setEndPoint(new Coordinate(BigDecimal.valueOf(-3), BigDecimal.valueOf(-4)));
        return ath;
    }

    private ActiveTim getActiveTim() {
        ActiveTim aTim = new ActiveTim();
        aTim.setClientId("clientId");
        aTim.setDirection("direction");
        aTim.setTimType("timType");
        aTim.setRsuTarget("rsuTarget");
        return aTim;
    }

    private OdeData getOdeData_requestMsgData() {
        OdeData odeData = new OdeData(getReqMsgMetadata(), getMsgPayload());
        return odeData;
    }

    private OdeData getOdeData() {
        OdeData odeData = new OdeData(getMetadata(), getMsgPayload());
        return odeData;
    }

    private OdeRequestMsgMetadata getReqMsgMetadata() {
        OdeRequestMsgMetadata metadata = new OdeRequestMsgMetadata();
        metadata.setOdeReceivedAt("2020-02-10T17:00:00.000Z[UTC]");
        metadata.setRecordGeneratedAt("2020-02-10T17:00:00.000Z[UTC]");
        metadata.setRecordGeneratedBy(GeneratedBy.TMC);
        metadata.setSerialId(getSerialId());
        metadata.setPayloadType(OdeDataType.TravelerInformationMessage);
        metadata.setRequest(getRequest());
        return metadata;
    }

    private ServiceRequest getRequest() {
        ServiceRequest sr = new ServiceRequest();
        RSU[] rsus = new RSU[1];
        RSU rsu = new RSU();
        rsu.setRsuTarget("127.0.0.1");
        rsus[0] = rsu;
        sr.setRsus(rsus);

        return sr;
    }

    private OdeLogMetadata getMetadata() {
        OdeLogMetadata metadata = new OdeLogMetadata();
        metadata.setOdeReceivedAt("2020-02-10T17:00:00.000Z[UTC]");
        metadata.setRecordGeneratedAt("2020-02-10T17:00:00.000Z[UTC]");
        metadata.setSecurityResultCode(SecurityResultCode.success);
        metadata.setRecordType(RecordType.driverAlert);
        metadata.setRecordGeneratedBy(GeneratedBy.TMC);
        metadata.setSerialId(getSerialId());
        metadata.setReceivedMessageDetails(getRxMessageDetails());
        return metadata;
    }

    private SerialId getSerialId() {
        SerialId serialId = new SerialId();
        serialId.setStreamId("streamId");
        serialId.setBundleId(-1l);
        serialId.setBundleSize(0);
        serialId.setRecordId(1);
        return serialId;
    }

    private ReceivedMessageDetails getRxMessageDetails() {
        ReceivedMessageDetails rxMsg = new ReceivedMessageDetails();
        OdeLogMsgMetadataLocation locationData = new OdeLogMsgMetadataLocation();
        locationData.setLatitude("-1");
        locationData.setLongitude("-2");
        locationData.setHeading("000");
        locationData.setElevation("-3");
        locationData.setSpeed("123");
        rxMsg.setLocationData(locationData);
        return rxMsg;
    }

    private OdeTimPayload getMsgPayload() {
        OdeTimPayload payload = new OdeTimPayload();
        payload.setTim(getTim());
        return payload;
    }

    private OdeTravelerInformationMessage getTim() {
        OdeTravelerInformationMessage tim = new OdeTravelerInformationMessage();
        tim.setDataframes(getDataFrames());
        return tim;
    }

    private DataFrame[] getDataFrames() {
        DataFrame[] dFrames = new DataFrame[1];
        DataFrame dFrame = new DataFrame();

        Region[] regions = new Region[1];

        regions[0] = getRegion();

        dFrame.setRegions(regions);
        dFrame.setItems(new String[] { "asdf" });
        dFrame.setDurationTime(32000);
        dFrames[0] = dFrame;

        return dFrames;
    }

    private Region getRegion() {
        Region region = new Region();
        region.setName("name");
        region.setPath(getPath());
        region.setGeometry(new Geometry());
        return region;
    }

    private Path getPath() {
        Path path = new Path();
        path.setNodes(getNodes());
        return path;
    }

    private NodeXY[] getNodes() {
        NodeXY[] nodes = new NodeXY[1];
        NodeXY nxy = new NodeXY();
        nxy.setDelta("xy");
        nxy.setNodeLat(new BigDecimal(-1));
        nxy.setNodeLong(new BigDecimal(-2));
        nodes[0] = nxy;
        return nodes;
    }
}