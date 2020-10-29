package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;
import com.trihydro.library.helpers.JsonToJavaConverter;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.tables.BsmOracleTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import us.dot.its.jpo.ode.model.OdeBsmPayload;
import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.plugin.j2735.J2735Bsm;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmPart2Content;
import us.dot.its.jpo.ode.plugin.j2735.J2735SpecialVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;

@Component
public class BsmService extends BaseService {

    private JsonToJavaConverter jsonToJava;
    private BsmOracleTables bsmOracleTables;
    private SQLNullHandler sqlNullHandler;
    private BsmPart2SpveService bsmPart2SpveService;
    private BsmPart2VseService bsmPart2VseService;
    private BsmPart2SuveService bsmPart2SuveService;
    private Utility utility;

    @Autowired
    public void InjectDependencies(JsonToJavaConverter _jsonToJava, BsmOracleTables _bsmOracleTables,
            SQLNullHandler _sqlNullHandler, BsmPart2SpveService _bsmPart2SpveService,
            BsmPart2VseService _bsmPart2VseService, BsmPart2SuveService _bsmPart2SuveService, Utility _utility) {
        jsonToJava = _jsonToJava;
        bsmOracleTables = _bsmOracleTables;
        sqlNullHandler = _sqlNullHandler;
        bsmPart2SpveService = _bsmPart2SpveService;
        bsmPart2VseService = _bsmPart2VseService;
        bsmPart2SuveService = _bsmPart2SuveService;
        utility = _utility;
    }

    public void addBSMToOracleDB(OdeData odeData, String value) {
        OdeBsmMetadata metadata = (OdeBsmMetadata) odeData.getMetadata();
        OdeBsmPayload payload = (OdeBsmPayload) odeData.getPayload();
        Gson gson = new Gson();
        if (metadata != null && payload != null) {
            System.out.println("Logging: " + metadata.getLogFileName());

            J2735Bsm bsm = payload.getBsm();
            if (bsm != null) {
                Long bsmCoreDataId = addBSMCoreData(metadata, bsm);
                List<J2735BsmPart2Content> partII = payload.getBsm().getPartII();
                if (bsmCoreDataId != null && !bsmCoreDataId.equals(Long.valueOf(0)) && partII != null) {
                    for (int i = 0; i < partII.size(); i++) {
                        if (partII.get(i).getId().name().equalsIgnoreCase("VehicleSafetyExtensions")) {
                            J2735VehicleSafetyExtensions vse = jsonToJava
                                    .convertJ2735VehicleSafetyExtensionsJsonToJava(value, i);
                            if (vse != null)
                                bsmPart2VseService.insertBSMPart2VSE(partII.get(i), vse, bsmCoreDataId);
                        } else if (partII.get(i).getId().name().equalsIgnoreCase("SpecialVehicleExtensions")) {
                            J2735SpecialVehicleExtensions spve = jsonToJava
                                    .convertJ2735SpecialVehicleExtensionsJsonToJava(value, i);
                            if (spve != null)
                                bsmPart2SpveService.insertBSMPart2SPVE(partII.get(i), spve, bsmCoreDataId);
                        } else if (partII.get(i).getId().name().equalsIgnoreCase("SupplementalVehicleExtensions")) {
                            J2735SupplementalVehicleExtensions suve = jsonToJava
                                    .convertJ2735SupplementalVehicleExtensionsJsonToJava(value, i);
                            if (suve != null)
                                bsmPart2SuveService.insertBSMPart2SUVE(partII.get(i), suve, bsmCoreDataId);
                        }
                    }
                }
            } else {
                utility.logWithDate(
                        "addBSMToOracleDB failed due to null payload.Bsm object. OdeData: " + gson.toJson(odeData));
            }
        } else {
            utility.logWithDate("addBSMToOracleDB failed due to invalid OdeData object: " + gson.toJson(odeData));
        }
    }

    public Long addBSMCoreData(OdeBsmMetadata metadata, J2735Bsm bsm) {

        String bsmCoreInsertQueryStatement = bsmOracleTables.buildInsertQueryStatement("bsm_core_data",
                bsmOracleTables.getBsmCoreDataTable());
        PreparedStatement bsmPreparedStatement = null;
        Connection connection = null;

        try {
            connection = dbInteractions.getConnectionPool();
            bsmPreparedStatement = connection.prepareStatement(bsmCoreInsertQueryStatement,
                    new String[] { "bsm_core_data_id" });

            System.out.println("insert into bsm core data...");

            int fieldNum = 1;

            for (String col : bsmOracleTables.getBsmCoreDataTable()) {
                if (col.equals("ID")) {
                    bsmPreparedStatement.setString(fieldNum, bsm.getCoreData().getId());
                } else if (col.equals("MSGCNT")) {
                    if (bsm.getCoreData().getMsgCnt() != null)
                        bsmPreparedStatement.setInt(fieldNum, bsm.getCoreData().getMsgCnt());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("SECMARK")) {
                    if (bsm.getCoreData().getSecMark() != null)
                        bsmPreparedStatement.setInt(fieldNum, bsm.getCoreData().getSecMark());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("POSITION_LAT")) {
                    if (bsm.getCoreData().getPosition().getLatitude() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getPosition().getLatitude());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("POSITION_LONG")) {
                    if (bsm.getCoreData().getPosition().getLongitude() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getPosition().getLongitude());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("POSITION_ELEV")) {
                    if (bsm.getCoreData().getPosition().getElevation() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getPosition().getElevation());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("ACCELSET_ACCELLAT")) {
                    if (bsm.getCoreData().getAccelSet().getAccelLat() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccelSet().getAccelLat());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("ACCELSET_ACCELLONG")) {
                    if (bsm.getCoreData().getAccelSet().getAccelLong() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccelSet().getAccelLong());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("ACCELSET_ACCELVERT")) {
                    if (bsm.getCoreData().getAccelSet().getAccelVert() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccelSet().getAccelVert());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("ACCELSET_ACCELYAW")) {
                    if (bsm.getCoreData().getAccelSet().getAccelYaw() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccelSet().getAccelYaw());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("ACCURACY_SEMIMAJOR")) {
                    if (bsm.getCoreData().getAccuracy().getSemiMajor() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccuracy().getSemiMajor());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("ACCURACY_SEMIMINOR")) {
                    if (bsm.getCoreData().getAccuracy().getSemiMinor() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccuracy().getSemiMinor());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("ACCURACY_ORIENTATION")) {
                    if (bsm.getCoreData().getAccuracy().getOrientation() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccuracy().getOrientation());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("TRANSMISSION")) {
                    if (bsm.getCoreData().getTransmission() != null)
                        bsmPreparedStatement.setString(fieldNum, bsm.getCoreData().getTransmission().toString());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("SPEED")) {
                    if (bsm.getCoreData().getSpeed() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getSpeed());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("HEADING")) {
                    if (bsm.getCoreData().getHeading() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getHeading());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("ANGLE")) {
                    if (bsm.getCoreData().getAngle() != null)
                        bsmPreparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAngle());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("BRAKES_WHEELBRAKES")) {
                    if (bsm.getCoreData().getBrakes().getWheelBrakes() != null)
                        bsmPreparedStatement.setString(fieldNum,
                                bsm.getCoreData().getBrakes().getWheelBrakes().toString());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("BRAKES_TRACTION")) {
                    bsmPreparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getTraction());
                } else if (col.equals("BRAKES_ABS")) {
                    bsmPreparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getAbs());
                } else if (col.equals("BRAKES_SCS")) {
                    bsmPreparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getScs());
                } else if (col.equals("BRAKES_BRAKEBOOST")) {
                    bsmPreparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getBrakeBoost());
                } else if (col.equals("BRAKES_AUXBRAKES")) {
                    bsmPreparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getAuxBrakes());
                } else if (col.equals("SIZE_LENGTH")) {
                    if (bsm.getCoreData().getSize().getLength() != null)
                        bsmPreparedStatement.setInt(fieldNum, bsm.getCoreData().getSize().getLength());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("SIZE_WIDTH")) {
                    if (bsm.getCoreData().getSize().getWidth() != null)
                        bsmPreparedStatement.setInt(fieldNum, bsm.getCoreData().getSize().getWidth());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("LOG_FILE_NAME")) {
                    bsmPreparedStatement.setString(fieldNum, metadata.getLogFileName());
                } else if (col.equals("RECORD_GENERATED_AT")) {
                    if (metadata.getRecordGeneratedAt() != null) {
                        java.util.Date recordGeneratedAtDate = utility.convertDate(metadata.getRecordGeneratedAt());
                        sqlNullHandler.setStringOrNull(bsmPreparedStatement, fieldNum,
                                utility.timestampFormat.format(recordGeneratedAtDate));
                    } else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("SECURITY_RESULT_CODE")) {
                    SecurityResultCodeType securityResultCodeType = GetSecurityResultCodeTypes().stream().filter(
                            x -> x.getSecurityResultCodeType().equals(metadata.getSecurityResultCode().toString()))
                            .findFirst().orElse(null);
                    bsmPreparedStatement.setInt(fieldNum, securityResultCodeType.getSecurityResultCodeTypeId());
                } else if (col.equals("SANITIZED")) {
                    if (metadata.isSanitized())
                        bsmPreparedStatement.setString(fieldNum, "1");
                    else
                        bsmPreparedStatement.setString(fieldNum, "0");
                } else if (col.equals("SERIAL_ID_STREAM_ID")) {
                    bsmPreparedStatement.setString(fieldNum, metadata.getSerialId().getStreamId());
                } else if (col.equals("SERIAL_ID_BUNDLE_SIZE")) {
                    bsmPreparedStatement.setInt(fieldNum, metadata.getSerialId().getBundleSize());
                } else if (col.equals("SERIAL_ID_BUNDLE_ID")) {
                    bsmPreparedStatement.setLong(fieldNum, metadata.getSerialId().getBundleId());
                } else if (col.equals("SERIAL_ID_RECORD_ID")) {
                    bsmPreparedStatement.setInt(fieldNum, metadata.getSerialId().getRecordId());
                } else if (col.equals("SERIAL_ID_SERIAL_NUMBER")) {
                    bsmPreparedStatement.setLong(fieldNum, metadata.getSerialId().getSerialNumber());
                } else if (col.equals("ODE_RECEIVED_AT")) {
                    if (metadata.getOdeReceivedAt() != null) {
                        java.util.Date receivedAtDate = utility.convertDate(metadata.getOdeReceivedAt());
                        sqlNullHandler.setStringOrNull(bsmPreparedStatement, fieldNum,
                                utility.timestampFormat.format(receivedAtDate));
                    } else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("RECORD_TYPE")) {
                    bsmPreparedStatement.setString(fieldNum, metadata.getRecordType().toString());
                } else if (col.equals("PAYLOAD_TYPE")) {
                    bsmPreparedStatement.setString(fieldNum, metadata.getPayloadType());
                } else if (col.equals("SCHEMA_VERSION")) {
                    bsmPreparedStatement.setInt(fieldNum, metadata.getSchemaVersion());
                } else if (col.equals("RECORD_GENERATED_BY")) {
                    if (metadata.getRecordGeneratedBy() != null)
                        bsmPreparedStatement.setString(fieldNum, metadata.getRecordGeneratedBy().toString());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                } else if (col.equals("BSM_SOURCE")) {
                    if (metadata.getBsmSource() != null)
                        bsmPreparedStatement.setString(fieldNum, metadata.getBsmSource().toString());
                    else
                        bsmPreparedStatement.setString(fieldNum, null);
                }

                fieldNum++;
            }

            // execute insert statement
            Long bsmCoreDataId = dbInteractions.executeAndLog(bsmPreparedStatement, "bsmCoreDataId");
            return bsmCoreDataId;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (bsmPreparedStatement != null)
                    bsmPreparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return Long.valueOf(0);
    }
}