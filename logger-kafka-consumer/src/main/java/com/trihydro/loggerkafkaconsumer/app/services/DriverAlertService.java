package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.DriverAlertType;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.tables.DriverAlertDbTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.model.OdeData;
import us.dot.its.jpo.ode.model.OdeDriverAlertPayload;
import us.dot.its.jpo.ode.model.OdeLogMetadata;

@Component
public class DriverAlertService extends BaseService {

    private DriverAlertDbTables driverAlertDbTables;
    private SQLNullHandler sqlNullHandler;
    private ItisCodeService itisCodeService;
    private DriverAlertTypeService driverAlertTypeService;
    private DriverAlertItisCodeService driverAlertItisCodeService;

    @Autowired
    public void InjectDependencies(DriverAlertDbTables _driverAlertDbTables, SQLNullHandler _sqlNullHandler,
            ItisCodeService _itisCodeService, DriverAlertTypeService _driverAlertTypeService,
            DriverAlertItisCodeService _driverAlertItisCodeService) {
        driverAlertDbTables = _driverAlertDbTables;
        sqlNullHandler = _sqlNullHandler;
        itisCodeService = _itisCodeService;
        driverAlertTypeService = _driverAlertTypeService;
        driverAlertItisCodeService = _driverAlertItisCodeService;
    }

    public Long addDriverAlertToDatabase(OdeData odeData) {

        System.out.println("Logging: " + ((OdeLogMetadata) odeData.getMetadata()).getLogFileName());

        OdeLogMetadata odeDriverAlertMetadata = (OdeLogMetadata) odeData.getMetadata();
        String alert = ((OdeDriverAlertPayload) odeData.getPayload()).getAlert();
        List<DriverAlertType> driverAlertTypes = driverAlertTypeService.getDriverAlertTypes();
        List<ItisCode> itisCodes = itisCodeService.selectAllItisCodes();
        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {

            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = driverAlertDbTables.buildInsertQueryStatement("driver_alert",
                    driverAlertDbTables.getDriverAlertTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "driver_alert_id" });
            int fieldNum = 1;

            for (String col : driverAlertDbTables.getDriverAlertTable()) {
                if (col.equals("RECORD_GENERATED_BY"))
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                            odeDriverAlertMetadata.getRecordGeneratedBy().toString());
                else if (col.equals("SCHEMA_VERSION"))
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                            odeDriverAlertMetadata.getSchemaVersion());
                else if(col.equals("SECURITY_RESULT_CODE")) {
                    // SecurityResultCodeType securityResultCodeType =
                    // securityResultCodeTypes.stream()
                    // .filter(x ->
                    // x.getSecurityResultCodeType().equals(odeDriverAlertMetadata.getSecurityResultCode().toString()))
                    // .findFirst()
                    // .orElse(null);
                    // preparedStatement.setInt(fieldNum,
                    // securityResultCodeType.getSecurityResultCodeTypeId());

                    preparedStatement.setNull(fieldNum, java.sql.Types.INTEGER);
                }
                else if (col.equals("LOG_FILE_NAME"))
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                            odeDriverAlertMetadata.getLogFileName());
                else if (col.equals("RECORD_GENERATED_AT")) {
                    if (odeDriverAlertMetadata.getRecordGeneratedAt() != null) {
                        java.util.Date recordGeneratedAtDate = utility.convertDate(odeDriverAlertMetadata.getRecordGeneratedAt());
                        Timestamp timestamp = new Timestamp(recordGeneratedAtDate.getTime());
                        sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, timestamp);
                    } else
                        preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
                } else if (col.equals("SANITIZED")) {
                    if (odeDriverAlertMetadata.isSanitized())
                        preparedStatement.setInt(fieldNum, 1);
                    else
                        preparedStatement.setInt(fieldNum, 0);
                } else if (col.equals("SERIAL_ID_STREAM_ID"))
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                            odeDriverAlertMetadata.getSerialId().getStreamId());
                else if (col.equals("SERIAL_ID_BUNDLE_SIZE"))
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                            odeDriverAlertMetadata.getSerialId().getBundleSize());
                else if (col.equals("SERIAL_ID_BUNDLE_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum,
                            odeDriverAlertMetadata.getSerialId().getBundleId());
                else if (col.equals("SERIAL_ID_RECORD_ID"))
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                            odeDriverAlertMetadata.getSerialId().getRecordId());
                else if (col.equals("SERIAL_ID_SERIAL_NUMBER"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum,
                            odeDriverAlertMetadata.getSerialId().getSerialNumber());
                else if (col.equals("PAYLOAD_TYPE"))
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                            odeDriverAlertMetadata.getPayloadType());
                else if (col.equals("RECORD_TYPE"))
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
                            odeDriverAlertMetadata.getRecordType().toString());
                else if (col.equals("ODE_RECEIVED_AT")) {
                    if (odeDriverAlertMetadata.getOdeReceivedAt() != null) {
                        java.util.Date odeReceivedAt = utility.convertDate(odeDriverAlertMetadata.getOdeReceivedAt());
                        Timestamp timestamp = new Timestamp(odeReceivedAt.getTime());
                        sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, timestamp);
                    } else {
                        preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
                    }
                } else if (col.equals("LATITUDE"))
                    sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                            Double.parseDouble(odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getLatitude()));
                else if (col.equals("LONGITUDE"))
                    sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                            Double.parseDouble(odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getLongitude()));
                else if (col.equals("HEADING"))
                    sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                            Double.parseDouble(odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getHeading()));
                else if (col.equals("ELEVATION_M"))
                    sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                            Double.parseDouble(odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getElevation()));
                else if (col.equals("SPEED"))
                    sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                            Double.parseDouble(odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getSpeed()));
                else if (col.equals("DRIVER_ALERT_TYPE_ID")) {
                    // check for TIMs
                    if (alert.split(",").length > 1) {
                        DriverAlertType driverAlertType = driverAlertTypes.stream()
                                .filter(x -> x.getShortName().equals("TIM")).findFirst().orElse(null);
                        sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                                driverAlertType.getDriverAlertTypeId());
                    } else {
                        for (DriverAlertType dat : driverAlertTypes) {
                            if (dat.getShortName().equals(alert)) {
                                sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
                                        dat.getDriverAlertTypeId());
                                break;
                            }
                        }
                    }
                } else
                    preparedStatement.setString(fieldNum, null);
                fieldNum++;
            }
            // execute insert statement
            Long driverAlertId = dbInteractions.executeAndLog(preparedStatement, "driverAlertId");

            // add driver_alert_itis_codes
            if (driverAlertId != null && alert.split(",").length > 1) {
                for (String code : alert.split(",")) {
                    if (code.chars().allMatch(Character::isDigit)) {
                        try {
                            var foundItisCode = itisCodes.stream()
                                    .filter(x -> x.getItisCode() == Integer.parseInt(code)).findFirst();
                            if (!foundItisCode.isEmpty()) {
                                driverAlertItisCodeService.insertDriverAlertItisCode(driverAlertId,
                                        foundItisCode.get().getItisCodeId());
                            }
                        } catch (Exception ex) {
                            // Potentially the driver alert has a huge int ("2216472429100300968259" for
                            // instance) and this fails to parse. Log it and move on
                            utility.logWithDate("Failed to parse integer from driver alert: " + ex.getMessage());
                        }
                    } else {
                        // look up by text
                        var itisCode = itisCodes.stream().filter(x -> x.getDescription() != null
                                && !x.getDescription().equals("") && x.getDescription().equals(code)).findFirst();
                        // these text values end with 'START_ITIS_CODE'/'END_ITIS_CODE', so check for
                        // nulls
                        if (!itisCode.isEmpty()) {
                            driverAlertItisCodeService.insertDriverAlertItisCode(driverAlertId,
                                    itisCode.get().getItisCodeId());
                        }
                    }
                }
            }
            return driverAlertId;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
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