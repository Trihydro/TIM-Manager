package com.trihydro.loggerkafkaconsumer.app.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.DriverAlertType;
import com.trihydro.library.tables.DriverAlertOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeLogMsgMetadataLocation;
import us.dot.its.jpo.ode.model.OdeMsgMetadata.GeneratedBy;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.SerialId;

public class DriverAlertServiceTest extends TestBase<DriverAlertService> {

    @Spy
    private DriverAlertOracleTables mockDriverAlertOracleTables = new DriverAlertOracleTables();
    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Mock
    private ItisCodeService mockItisCodeService;
    @Mock
    private DriverAlertTypeService mockDriverAlertTypeService;
    @Mock
    private DriverAlertItisCodeService mockDriverAlertItisCodeService;

    private DriverAlertType dat;

    @Before
    public void setupSubTest() {
        uut.InjectDependencies(mockDriverAlertOracleTables, mockSqlNullHandler, mockItisCodeService,
                mockDriverAlertTypeService, mockDriverAlertItisCodeService);

        List<DriverAlertType> dats = new ArrayList<DriverAlertType>();
        dat = new DriverAlertType();
        dat.setShortName("alert");
        dat.setDriverAlertTypeId(-1);
        dats.add(dat);
        doReturn(dats).when(mockDriverAlertTypeService).getDriverAlertTypes();
    }

    @Test
    public void addDriverAlertToOracleDB_SUCCESS() throws SQLException {
        // Arrange
        OdeData odeData = getOdeData();
        OdeLogMetadata odeDriverAlertMetadata = (OdeLogMetadata) odeData.getMetadata();

        // Act
        Long data = uut.addDriverAlertToOracleDB(odeData);

        // Assert
        assertEquals(new Long(-1), data);
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 1,
                odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getLatitude());// LATITUDE
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2,
                odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getLongitude());// LONGITUDE
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 3,
                odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getHeading());// HEADING
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 4,
                odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getElevation());// ELEVATION_M
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5,
                odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getSpeed());// SPEED
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 6, dat.getDriverAlertTypeId());//
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 7, odeDriverAlertMetadata.getLogFileName());// LOG_FILE_NAME
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 8,
                odeDriverAlertMetadata.getRecordType().toString());// RECORD_TYPE
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 9, odeDriverAlertMetadata.getPayloadType());// PAYLOAD_TYPE
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 10,
                odeDriverAlertMetadata.getSerialId().getStreamId());// SERIAL_ID_STREAM_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 11,
                odeDriverAlertMetadata.getSerialId().getBundleSize());// SERIAL_ID_BUNDLE_SIZE
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 12,
                odeDriverAlertMetadata.getSerialId().getBundleId());// SERIAL_ID_BUNDLE_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 13,
                odeDriverAlertMetadata.getSerialId().getRecordId());// SERIAL_ID_RECORD_ID
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 14,
                odeDriverAlertMetadata.getSerialId().getSerialNumber());// SERIAL_ID_SERIAL_NUMBER
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 15, "10-Feb-20 05.00.00.000 PM");// ODE_RECEIVED_AT
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 16,
                odeDriverAlertMetadata.getSchemaVersion());// SCHEMA_VERSION
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 17, "10-Feb-20 05.00.00.000 PM");// RECORD_GENERATED_AT
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 18,
                odeDriverAlertMetadata.getRecordGeneratedBy().toString());// RECORD_GENERATED_BY
        verify(mockPreparedStatement).setString(19, "0"); // SANITIZED
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void addDriverAlertToOracleDB_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 1, "-1");

        // Act
        Long data = uut.addDriverAlertToOracleDB(getOdeData());

        // Assert
        assertEquals(new Long(0), data);
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    // ******************************************* //
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

    private OdeDriverAlertPayload getMsgPayload() {
        OdeDriverAlertPayload payload = new OdeDriverAlertPayload("alert");
        return payload;
    }
}