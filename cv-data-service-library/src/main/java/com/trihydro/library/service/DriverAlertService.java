package com.trihydro.library.service;

import us.dot.its.jpo.ode.model.OdeLogMetadata;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.util.List;
import com.trihydro.library.model.DriverAlertType;
import com.trihydro.library.model.ItisCode;
import com.trihydro.library.tables.DriverAlertOracleTables;

public class DriverAlertService extends CvDataServiceLibrary {

	static List<DriverAlertType> driverAlertTypes;
	static List<ItisCode> itisCodes;

	public static Long insertDriverAlert(OdeLogMetadata odeDriverAlertMetadata, String alert) throws SQLException {

		driverAlertTypes = getDriverAlertTypes();
		itisCodes = getItisCodes();
		PreparedStatement preparedStatement = null;
		Connection connection = null;

		try {

			connection = DbUtility.getConnectionPool();
			String insertQueryStatement = DriverAlertOracleTables.buildInsertQueryStatement("driver_alert",
					DriverAlertOracleTables.getDriverAlertTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "driver_alert_id" });
			int fieldNum = 1;

			for (String col : DriverAlertOracleTables.getDriverAlertTable()) {
				if (col.equals("RECORD_GENERATED_BY"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getRecordGeneratedBy().toString());
				else if (col.equals("SCHEMA_VERSION"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getSchemaVersion());
				// else if(col.equals("SECURITY_RESULT_CODE")) {
				// SecurityResultCodeType securityResultCodeType =
				// securityResultCodeTypes.stream()
				// .filter(x ->
				// x.getSecurityResultCodeType().equals(odeDriverAlertMetadata.getSecurityResultCode().toString()))
				// .findFirst()
				// .orElse(null);
				// preparedStatement.setInt(fieldNum,
				// securityResultCodeType.getSecurityResultCodeTypeId());
				// }
				else if (col.equals("LOG_FILE_NAME"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getLogFileName());
				else if (col.equals("RECORD_GENERATED_AT")) {
					if (odeDriverAlertMetadata.getRecordGeneratedAt() != null) {
						java.util.Date recordGeneratedAtDate = convertDate(
								odeDriverAlertMetadata.getRecordGeneratedAt());
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
								mstFormat.format(recordGeneratedAtDate));
					} else
						preparedStatement.setString(fieldNum, null);
				} else if (col.equals("SANITIZED")) {
					if (odeDriverAlertMetadata.isSanitized())
						preparedStatement.setString(fieldNum, "1");
					else
						preparedStatement.setString(fieldNum, "0");
				} else if (col.equals("SERIAL_ID_STREAM_ID"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getSerialId().getStreamId());
				else if (col.equals("SERIAL_ID_BUNDLE_SIZE"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getSerialId().getBundleSize());
				else if (col.equals("SERIAL_ID_BUNDLE_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getSerialId().getBundleId());
				else if (col.equals("SERIAL_ID_RECORD_ID"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getSerialId().getRecordId());
				else if (col.equals("SERIAL_ID_SERIAL_NUMBER"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getSerialId().getSerialNumber());
				else if (col.equals("PAYLOAD_TYPE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getPayloadType());
				else if (col.equals("RECORD_TYPE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getRecordType().toString());
				else if (col.equals("ODE_RECEIVED_AT")) {
					if (odeDriverAlertMetadata.getOdeReceivedAt() != null) {
						java.util.Date odeReceivedAt = convertDate(odeDriverAlertMetadata.getOdeReceivedAt());
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, mstFormat.format(odeReceivedAt));
					} else {
						preparedStatement.setString(fieldNum, null);
					}
				} else if (col.equals("LATITUDE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getLatitude());
				else if (col.equals("LONGITUDE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getLongitude());
				else if (col.equals("HEADING"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getHeading());
				else if (col.equals("ELEVATION_M"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getElevation());
				else if (col.equals("SPEED"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getSpeed());
				else if (col.equals("DRIVER_ALERT_TYPE_ID")) {
					// check for TIMs
					if (alert.split(",").length > 1) {
						DriverAlertType driverAlertType = driverAlertTypes.stream()
								.filter(x -> x.getShortName().equals("TIM")).findFirst().orElse(null);
						SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
								driverAlertType.getDriverAlertTypeId());
					} else {
						for (DriverAlertType dat : driverAlertTypes) {
							if (dat.getShortName().equals(alert)) {
								SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
										dat.getDriverAlertTypeId());
							}
						}
					}
				} else
					preparedStatement.setString(fieldNum, null);
				fieldNum++;
			}
			// execute insert statement
			Long driverAlertId = log(preparedStatement, "driverAlertId");

			// add driver_alert_itis_codes
			if (driverAlertId != null && alert.split(",").length > 1) {
				for (String code : alert.split(",")) {
					if (code.chars().allMatch(Character::isDigit)) {
						for (ItisCode itisCode : itisCodes) {
							try{								
								if (itisCode.getItisCode() == Integer.parseInt(code)) {
									DriverAlertItisCodeService.insertDriverAlertItisCode(driverAlertId,
											itisCode.getItisCodeId());
								}
							}
							catch(NumberFormatException e){
								e.printStackTrace();
							}							
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
		return new Long(0);
	}

}
