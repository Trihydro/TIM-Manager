package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.model.TimType;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeLogMsgMetadataLocation;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.OdeTimPayload;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.SerialId;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Geometry;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Path;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

@RunWith(MockitoJUnitRunner.class)
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

    private WydotRsu rsu;
    private Long pathId = -99l;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockActiveTimService, mockTimOracleTables, mockSqlNullHandler, mockPathService,
                mockRegionService, mockDataFrameService, mockRsuService, mockTts, mockItisCodesService,
                mockTimRsuService, mockDataFrameItisCodeService, mockPathNodeXYService, mockNodeXYService, mockUtility,
                mockActiveTimHoldingService);

        ArrayList<WydotRsu> rsus = new ArrayList<>();
        rsu = new WydotRsu();
        rsu.setRsuId(-1);
        rsu.setRsuIndex(99);
        rsu.setLatitude(-1d);
        rsu.setLongitude(-2d);
        rsu.setMilepost(99d);
        rsu.setRsuTarget("rsuTarget");
        rsus.add(rsu);
        lenient().doReturn(rsus).when(mockRsuService).getRsus();

        lenient().doReturn(pathId).when(mockPathService).InsertPath();
    }

    //TODO: finish test after merge
    // @Test
    // public void addActiveTimToOracleDB_SUCCESS() {
    //     // Arrange
    //     OdeData odeData = getOdeData();
    //     ActiveTim aTim = new ActiveTim();
    //     aTim.setSatRecordId("satRecordId");
    //     Long timId = -1l;
    //     Long dataFrameId = -2l;
    //     DataFrame[] dFrames = ((OdeTimPayload) odeData.getPayload()).getTim().getDataframes();

    //     doReturn(getActiveTim()).when(uut).setActiveTimByRegionName(isA(String.class));
    //     doReturn(timId).when(uut).AddTim(odeData.getMetadata(), null, ((OdeTimPayload) odeData.getPayload()).getTim(),
    //             null, null, null, aTim.getSatRecordId(), dFrames[0].getRegions()[0].getName());
    //     doReturn(dataFrameId).when(mockDataFrameService).AddDataFrame(dFrames[0], timId);
    //     doNothing().when(uut).addRegion(any(), any());
    //     doNothing().when(uut).addDataFrameItis(any(), any());
    //     // doReturn(pathId).when(mockPathService).InsertPath();
    //     doReturn(aTim).when(uut).setActiveTimByRegionName(anyString());

    //     // Act

    //     // Assert
    // }

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
                any(), any(), any(), any());
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
    public void addRegion_pathSUCCESS() {
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
        assertFalse("updateTimSatRecordId returned true when failure", data);
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
        assertTrue("updateTimSatRecordId returned false when successful", data);
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
        assertNull(data);
    }

    @Test
    public void setActiveTimByRegionName_RsuSUCCESS() {
        // Arrange
        String regionName = "any_Prairie Center Cir_108.82122_108.66974_RSU-10.145.1.100_RC_clientId";
        TimType timType = new TimType();
        timType.setType("RC");
        timType.setTimTypeId(-1l);
        doReturn(timType).when(uut).getTimType("RC");

        // Act
        ActiveTim data = uut.setActiveTimByRegionName(regionName);

        // Assert
        assertNotNull(data);
        assertNotNull(data.getDirection());
        assertNotNull(data.getRoute());
        assertNotNull(data.getMilepostStart());
        assertNotNull(data.getMilepostStop());
        assertNotNull(data.getRsuTarget());
        assertNotNull(data.getTimType());
        assertNotNull(data.getTimTypeId());
        assertNotNull(data.getClientId());
    }

    @Test
    public void setActiveTimByRegionName_SatSUCCESS() {
        // Arrange
        String regionName = "any_Prairie Center Cir_108.82122_108.66974_SAT-satId_RC_clientId";
        TimType timType = new TimType();
        timType.setType("RC");
        timType.setTimTypeId(-1l);
        doReturn(timType).when(uut).getTimType("RC");

        // Act
        ActiveTim data = uut.setActiveTimByRegionName(regionName);

        // Assert
        assertNotNull(data);
        assertNotNull(data.getDirection());
        assertNotNull(data.getRoute());
        assertNotNull(data.getMilepostStart());
        assertNotNull(data.getMilepostStop());
        assertNotNull(data.getSatRecordId());
        assertNotNull(data.getTimType());
        assertNotNull(data.getTimTypeId());
        assertNotNull(data.getClientId());
    }

    @Test
    public void getTimType_FAIL() {
        // Arrange
        doReturn(new ArrayList<>()).when(mockTts).getTimTypes();
        // Act
        TimType data = uut.getTimType("timTypeName");

        // Assert
        assertNull(data);
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
        assertNotNull(data);
        assertEquals(new Long(-1), data.getTimTypeId());
    }

    @Test
    public void getItisCodeId_FAIL() {
        // Arrange
        doReturn(new ArrayList<>()).when(mockItisCodesService).selectAllItisCodes();

        // Act
        String data = uut.getItisCodeId("1234");

        // Assert
        assertNull(data);
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
        assertEquals("-1", data);
    }

    private ActiveTim getActiveTim() {
        ActiveTim aTim = new ActiveTim();
        aTim.setRsuTarget("rsuTarget");
        return aTim;
    }

    private OdeData getOdeData() {
        OdeData odeData = new OdeData(getMetadata(), getMsgPayload());
        return odeData;
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
        nxy.setNodeLat(new BigDecimal(-1));
        nxy.setNodeLong(new BigDecimal(-2));
        nodes[0] = nxy;
        return nodes;
    }
}