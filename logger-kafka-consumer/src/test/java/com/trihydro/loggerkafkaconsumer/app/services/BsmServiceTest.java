package com.trihydro.loggerkafkaconsumer.app.services;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.JsonToJavaConverter;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.tables.BsmDbTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeMsgPayload;
import us.dot.its.jpo.ode.plugin.j2735.J2735AccelerationSet4Way;
import us.dot.its.jpo.ode.plugin.j2735.J2735BitString;
import us.dot.its.jpo.ode.plugin.j2735.J2735BrakeSystemStatus;
import us.dot.its.jpo.ode.plugin.j2735.J2735Bsm;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmCoreData;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmPart2Content;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmPart2Content.J2735BsmPart2Id;
import us.dot.its.jpo.ode.plugin.j2735.J2735PositionalAccuracy;
import us.dot.its.jpo.ode.plugin.j2735.J2735SpecialVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735TransmissionState;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSize;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

public class BsmServiceTest extends TestBase<BsmService> {

    @Spy
    private BsmDbTables mockBsmDbTables = new BsmDbTables();
    @Mock
    private JsonToJavaConverter mockJsonToJava;
    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Mock
    private J2735VehicleSafetyExtensions mockJ2735VehicleSafetyExtensions;
    @Mock
    private J2735SpecialVehicleExtensions mockJ2735SpecialVehicleExtensions;
    @Mock
    private J2735SupplementalVehicleExtensions mockJ2735SupplementalVehicleExtensions;
    @Mock
    private BsmPart2SpveService mockBsmPart2SpveService;
    @Mock
    private BsmPart2VseService mockBsmPart2VseService;
    @Mock
    private BsmPart2SuveService mockBsmPart2SuveService;
    @Mock
    private Utility mockUtility;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockJsonToJava, mockBsmDbTables, mockSqlNullHandler, mockBsmPart2SpveService,
                mockBsmPart2VseService, mockBsmPart2SuveService, mockUtility);
    }

    private void setupSafetyExtensions() {
        doReturn(mockJ2735VehicleSafetyExtensions).when(mockJsonToJava)
                .convertJ2735VehicleSafetyExtensionsJsonToJava(isA(String.class), isA(Integer.class));
    }

    private void setupSpecialExtensions() {
        doReturn(mockJ2735SpecialVehicleExtensions).when(mockJsonToJava)
                .convertJ2735SpecialVehicleExtensionsJsonToJava(isA(String.class), isA(Integer.class));
    }

    private void setupSupplementalExtensions() {
        doReturn(mockJ2735SupplementalVehicleExtensions).when(mockJsonToJava)
                .convertJ2735SupplementalVehicleExtensionsJsonToJava(isA(String.class), isA(Integer.class));
    }

    @Test
    public void addBSMToDatabase_VehicleSafetyExtensions_SUCCESS() {
        // Arrange
        setupSafetyExtensions();
        Long bsmCoreDataId = -1l;
        doReturn(bsmCoreDataId).when(uut).addBSMCoreData(isA(OdeBsmMetadata.class), isA(J2735Bsm.class));
        OdeData odeData = getOdeData("VehicleSafetyExtensions");

        // Act
        uut.addBSMToDatabase(odeData, "value");
        List<J2735BsmPart2Content> partII = ((OdeBsmPayload) odeData.getPayload()).getBsm().getPartII();

        // Assert
        verify(uut).addBSMCoreData((OdeBsmMetadata) odeData.getMetadata(),
                ((OdeBsmPayload) odeData.getPayload()).getBsm());
        verify(mockJsonToJava).convertJ2735VehicleSafetyExtensionsJsonToJava("value", 0);
        verify(mockBsmPart2VseService).insertBSMPart2VSE(partII.get(0), mockJ2735VehicleSafetyExtensions,
                bsmCoreDataId);
    }

    @Test
    public void addBSMToDatabase_SpecialVehicleExtensions_SUCCESS() {
        // Arrange
        setupSpecialExtensions();
        Long bsmCoreDataId = -1l;
        doReturn(bsmCoreDataId).when(uut).addBSMCoreData(isA(OdeBsmMetadata.class), isA(J2735Bsm.class));
        OdeData odeData = getOdeData("SpecialVehicleExtensions");

        // Act
        uut.addBSMToDatabase(odeData, "value");
        List<J2735BsmPart2Content> partII = ((OdeBsmPayload) odeData.getPayload()).getBsm().getPartII();

        // Assert
        verify(uut).addBSMCoreData((OdeBsmMetadata) odeData.getMetadata(),
                ((OdeBsmPayload) odeData.getPayload()).getBsm());
        verify(mockJsonToJava).convertJ2735SpecialVehicleExtensionsJsonToJava("value", 0);
        verify(mockBsmPart2SpveService).insertBSMPart2SPVE(partII.get(0), mockJ2735SpecialVehicleExtensions,
                bsmCoreDataId);
    }

    @Test
    public void addBSMToDatabase_SupplementalVehicleExtensions_SUCCESS() {
        // Arrange
        setupSupplementalExtensions();
        Long bsmCoreDataId = -1l;
        doReturn(bsmCoreDataId).when(uut).addBSMCoreData(isA(OdeBsmMetadata.class), isA(J2735Bsm.class));
        OdeData odeData = getOdeData("SupplementalVehicleExtensions");

        // Act
        uut.addBSMToDatabase(odeData, "value");
        List<J2735BsmPart2Content> partII = ((OdeBsmPayload) odeData.getPayload()).getBsm().getPartII();

        // Assert
        verify(uut).addBSMCoreData((OdeBsmMetadata) odeData.getMetadata(),
                ((OdeBsmPayload) odeData.getPayload()).getBsm());
        verify(mockJsonToJava).convertJ2735SupplementalVehicleExtensionsJsonToJava("value", 0);
        verify(mockBsmPart2SuveService).insertBSMPart2SUVE(partII.get(0), mockJ2735SupplementalVehicleExtensions,
                bsmCoreDataId);
    }

    @Test
    public void addBSMToDatabase_FailNullMetadata() {
        // Arrange
        OdeData odeData = new OdeData();
        odeData.setPayload(new OdeBsmPayload());

        // Act
        uut.addBSMToDatabase(odeData, "value");

        // Assert
        verify(uut, times(0)).addBSMCoreData((OdeBsmMetadata) odeData.getMetadata(),
                ((OdeBsmPayload) odeData.getPayload()).getBsm());
    }

    @Test
    public void addBSMCoreData_SUCCESS() throws SQLException {
        // Arrange
        OdeBsmMetadata metadata = getMetadata();
        J2735Bsm bsm = getBsmPayload("VehicleSafetyExtensions");
        List<SecurityResultCodeType> srcts = new ArrayList<>();
        SecurityResultCodeType srct = new SecurityResultCodeType();
        srct.setSecurityResultCodeType("success");
        srct.setSecurityResultCodeTypeId(-1);
        srcts.add(srct);
        doReturn(srcts).when(uut).GetSecurityResultCodeTypes();

        var recTime = Instant.parse(metadata.getOdeReceivedAt());
        java.util.Date recDate = java.util.Date.from(recTime);
        doReturn(recDate).when(mockUtility).convertDate(metadata.getOdeReceivedAt());
        mockUtility.timestampFormat = timestampFormat;

        // Act
        Long data = uut.addBSMCoreData(metadata, bsm);

        // Assert
        Assertions.assertEquals(Long.valueOf(-1), data);
        verify(mockPreparedStatement).setString(1, bsm.getCoreData().getId());// ID
        verify(mockPreparedStatement).setString(2, null);// MSGCNT
        verify(mockPreparedStatement).setString(3, null);// SECMARK
        verify(mockPreparedStatement).setBigDecimal(4, bsm.getCoreData().getPosition().getLatitude());// POSITION_LAT
        verify(mockPreparedStatement).setBigDecimal(5, bsm.getCoreData().getPosition().getLongitude());// POSITION_LONG
        verify(mockPreparedStatement).setBigDecimal(6, bsm.getCoreData().getPosition().getElevation());// POSITION_ELEV
        verify(mockPreparedStatement).setBigDecimal(7, bsm.getCoreData().getAccelSet().getAccelLat());// ACCELSET_ACCELLAT
        verify(mockPreparedStatement).setBigDecimal(8, bsm.getCoreData().getAccelSet().getAccelLong());// ACCELSET_ACCELLONG
        verify(mockPreparedStatement).setBigDecimal(9, bsm.getCoreData().getAccelSet().getAccelVert());// ACCELSET_ACCELVERT
        verify(mockPreparedStatement).setBigDecimal(10, bsm.getCoreData().getAccelSet().getAccelYaw());// ACCELSET_ACCELYAW
        verify(mockPreparedStatement).setString(11, null);// ACCURACY_SEMIMAJOR
        verify(mockPreparedStatement).setString(12, null);// ACCURACY_SEMIMINOR
        verify(mockPreparedStatement).setString(13, null);// ACCURACY_ORIENTATION
        verify(mockPreparedStatement).setString(14, bsm.getCoreData().getTransmission().toString());// TRANSMISSION
        verify(mockPreparedStatement).setString(15, null);// SPEED
        verify(mockPreparedStatement).setString(16, null);// HEADING
        verify(mockPreparedStatement).setString(17, null);// ANGLE
        verify(mockPreparedStatement).setString(18, bsm.getCoreData().getBrakes().getWheelBrakes().toString());// BRAKES_WHEELBRAKES
        verify(mockPreparedStatement).setString(19, bsm.getCoreData().getBrakes().getTraction());// BRAKES_TRACTION
        verify(mockPreparedStatement).setString(20, bsm.getCoreData().getBrakes().getAbs());// BRAKES_ABS
        verify(mockPreparedStatement).setString(21, bsm.getCoreData().getBrakes().getScs());// BRAKES_SCS
        verify(mockPreparedStatement).setString(22, bsm.getCoreData().getBrakes().getBrakeBoost());// BRAKES_BRAKEBOOST
        verify(mockPreparedStatement).setString(23, bsm.getCoreData().getBrakes().getAuxBrakes());// BRAKES_AUXBRAKES
        verify(mockPreparedStatement).setString(24, null);// SIZE_LENGTH
        verify(mockPreparedStatement).setString(25, null);// SIZE_WIDTH
        verify(mockPreparedStatement).setString(26, metadata.getLogFileName());// LOG_FILE_NAME
        verify(mockPreparedStatement).setString(27, null);// RECORD_GENERATED_AT
        verify(mockPreparedStatement).setInt(28, srct.getSecurityResultCodeTypeId());// SECURITY_RESULT_CODE
        verify(mockPreparedStatement).setString(29, "0");// SANITIZED
        verify(mockPreparedStatement).setString(30, metadata.getSerialId().getStreamId());// SERIAL_ID_STREAM_ID
        verify(mockPreparedStatement).setInt(31, metadata.getSerialId().getBundleSize());// SERIAL_ID_BUNDLE_SIZE
        verify(mockPreparedStatement).setLong(32, metadata.getSerialId().getBundleId());// SERIAL_ID_BUNDLE_ID
        verify(mockPreparedStatement).setInt(33, metadata.getSerialId().getRecordId());// SERIAL_ID_RECORD_ID
        verify(mockPreparedStatement).setLong(34, metadata.getSerialId().getSerialNumber());// SERIAL_ID_SERIAL_NUMBER
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 35, timestampFormat.format(recDate)); // ODE_RECEIVED_AT
        verify(mockPreparedStatement).setString(36, metadata.getRecordType().toString());// RECORD_TYPE
        verify(mockPreparedStatement).setString(37, metadata.getPayloadType());// PAYLOAD_TYPE
        verify(mockPreparedStatement).setInt(38, metadata.getSchemaVersion());// SCHEMA_VERSION
        verify(mockPreparedStatement).setString(39, null);// RECORD_GENERATED_BY
        verify(mockPreparedStatement).setString(40, null);// BSM_SOURCE
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void addBSMCoreData_FAIL() throws SQLException {
        // Arrange
        OdeBsmMetadata metadata = getMetadata();
        J2735Bsm bsm = getBsmPayload("VehicleSafetyExtensions");
        doThrow(new SQLException()).when(mockPreparedStatement).setString(1, bsm.getCoreData().getId());
        // Act
        Long data = uut.addBSMCoreData(metadata, bsm);

        // Assert
        Assertions.assertEquals(Long.valueOf(0), data);
    }

    private OdeData getOdeData(String partIIName) {
        OdeData odeData = new OdeData(getMetadata(), getMsgPayload(partIIName));
        return odeData;
    }

    private OdeBsmMetadata getMetadata() {
        OdeBsmMetadata metadata = new OdeBsmMetadata();
        metadata.setSecurityResultCode(SecurityResultCode.success);
        metadata.setRecordType(RecordType.bsmLogDuringEvent);
        return metadata;
    }

    private OdeMsgPayload getMsgPayload(String partIIName) {
        OdeBsmPayload payload = new OdeBsmPayload();
        ((OdeBsmPayload) payload).setBsm(getBsmPayload(partIIName));
        return payload;
    }

    private J2735Bsm getBsmPayload(String partIIName) {
        J2735Bsm bsmPayload = new J2735Bsm();
        List<J2735BsmPart2Content> partII = new ArrayList<>();
        J2735BsmPart2Content content = new J2735BsmPart2Content();

        switch (partIIName) {
            case "VehicleSafetyExtensions":
                content.setId(J2735BsmPart2Id.VehicleSafetyExtensions);
                break;
            case "SpecialVehicleExtensions":
                content.setId(J2735BsmPart2Id.SpecialVehicleExtensions);
                break;

            case "SupplementalVehicleExtensions":
                content.setId(J2735BsmPart2Id.SupplementalVehicleExtensions);
                break;
        }
        partII.add(content);
        bsmPayload.setPartII(partII);

        // set core data
        bsmPayload.setCoreData(getCoreData());
        return bsmPayload;
    }

    private J2735BsmCoreData getCoreData() {
        J2735BsmCoreData coreData = new J2735BsmCoreData();
        BigDecimal latitude = new BigDecimal(1);
        BigDecimal longitude = new BigDecimal(2);
        BigDecimal elevation = new BigDecimal(3);
        OdePosition3D position = new OdePosition3D();
        position.setLatitude(latitude);
        position.setLongitude(longitude);
        position.setElevation(elevation);
        coreData.setPosition(position);

        J2735AccelerationSet4Way accelSet = new J2735AccelerationSet4Way();
        accelSet.setAccelLat(latitude);
        accelSet.setAccelLong(longitude);
        accelSet.setAccelVert(new BigDecimal(5));
        accelSet.setAccelYaw(new BigDecimal(6));
        coreData.setAccelSet(accelSet);

        J2735PositionalAccuracy accuracy = new J2735PositionalAccuracy();
        coreData.setAccuracy(accuracy);

        J2735BrakeSystemStatus brakes = new J2735BrakeSystemStatus();
        J2735BitString wheelBrakes = new J2735BitString();
        brakes.setWheelBrakes(wheelBrakes);
        coreData.setBrakes(brakes);

        J2735VehicleSize size = new J2735VehicleSize();
        coreData.setSize(size);

        coreData.setTransmission(J2735TransmissionState.NEUTRAL);
        return coreData;
    }
}