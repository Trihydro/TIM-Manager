package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveTim;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

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

    private WydotRsu rsu;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockActiveTimService, mockTimOracleTables, mockSqlNullHandler, mockPathService,
                mockRegionService, mockDataFrameService, mockRsuService, mockTts, mockItisCodesService,
                mockTimRsuService, mockDataFrameItisCodeService, mockPathNodeXYService, mockNodeXYService);

        ArrayList<WydotRsu> rsus = new ArrayList<>();
        rsu = new WydotRsu();
        rsu.setRsuId(-1);
        rsu.setRsuIndex(99);
        rsu.setLatitude(-1d);
        rsu.setLongitude(-2d);
        rsu.setMilepost(99d);
        rsu.setRsuTarget("rsuTarget");
        rsus.add(rsu);
        doReturn(rsus).when(mockRsuService).getRsus();
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
        verifyZeroInteractions(mockDataFrameService);
        verifyZeroInteractions(mockRegionService);
        verifyZeroInteractions(mockTimRsuService);
        verifyZeroInteractions(mockDataFrameItisCodeService);
        // verify only these were called on the uut
        verify(uut).InjectDependencies(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any());
        verify(uut).addTimToOracleDB(odeData);
        verify(uut).AddTim(any(), any(), any(), any(), any(), any(), any(), any());
        verifyNoMoreInteractions(uut);
    }

    @Test
    public void addTimToOracleDB_SUCCESS() {
        // Arrange
        OdeData odeData = getOdeData();
        Long timId = -1l;
        Long pathId = -99l;
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

    // ******************************************* //
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
        Region region = new Region();
        region.setName("name");
        region.setPath(getPath());
        region.setGeometry(new Geometry());
        regions[0] = region;

        dFrame.setRegions(regions);
        dFrame.setItems(new String[] { "asdf" });
        dFrames[0] = dFrame;

        return dFrames;
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
        return nodes;
    }
}