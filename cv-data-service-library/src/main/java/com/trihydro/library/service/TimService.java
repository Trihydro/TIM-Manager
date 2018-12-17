package com.trihydro.library.service;

import us.dot.its.jpo.ode.model.OdeLogMetadata;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.OdeTravelerInputData;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.trihydro.library.service.SecurityResultCodeTypeService;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.tables.TimOracleTables;
import us.dot.its.jpo.ode.plugin.ServiceRequest;
import us.dot.its.jpo.ode.plugin.ServiceRequest.OdeInternal;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;

public class TimService extends CvDataServiceLibrary {

	public static Long insertTim(OdeMsgMetadata odeTimMetadata, ReceivedMessageDetails receivedMessageDetails,
			OdeTravelerInformationMessage j2735TravelerInformationMessage, RecordType recordType, String logFileName, SecurityResultCode securityResultCode) {

		PreparedStatement preparedStatement = null;
		Connection connection = null;

		try {

			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("tim",
					TimOracleTables.getTimTable());
			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "tim_id" });
			int fieldNum = 1;

			for (String col : TimOracleTables.getTimTable()) {
				if (col.equals("MSG_CNT"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
							j2735TravelerInformationMessage.getMsgCnt());
				else if (col.equals("PACKET_ID"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							j2735TravelerInformationMessage.getPacketID());
				else if (col.equals("URL_B"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							j2735TravelerInformationMessage.getUrlB());
				else if (col.equals("TIME_STAMP")) {
					SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum,
							java.sql.Timestamp.valueOf(LocalDateTime.parse(
									j2735TravelerInformationMessage.getTimeStamp(), DateTimeFormatter.ISO_DATE_TIME)));				
				} else if (col.equals("RECORD_GENERATED_BY")) {
					if (odeTimMetadata.getRecordGeneratedBy() != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
								odeTimMetadata.getRecordGeneratedBy().toString());
					else
						preparedStatement.setString(fieldNum, null);
				} else if (col.equals("RMD_LD_ELEVATION")) {
					if (receivedMessageDetails != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
						receivedMessageDetails.getLocationData().getElevation());
					else
						preparedStatement.setString(fieldNum, null);
				} else if (col.equals("RMD_LD_HEADING")) {
					if (receivedMessageDetails != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
						receivedMessageDetails.getLocationData().getHeading());
					else
						preparedStatement.setString(fieldNum, null);
				} else if (col.equals("RMD_LD_LATITUDE")) {
					if (receivedMessageDetails != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
						receivedMessageDetails.getLocationData().getLatitude());
					else
						preparedStatement.setString(fieldNum, null);
				} else if (col.equals("RMD_LD_LONGITUDE")) {
					if (receivedMessageDetails != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
						receivedMessageDetails.getLocationData().getLongitude());
					else
						preparedStatement.setString(fieldNum, null);
				} else if (col.equals("RMD_LD_SPEED")) {
					if (receivedMessageDetails != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
						receivedMessageDetails.getLocationData().getSpeed());
					else
						preparedStatement.setString(fieldNum, null);
				} else if (col.equals("RMD_RX_SOURCE")) {
					if (receivedMessageDetails != null
							&& receivedMessageDetails.getRxSource() != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
						receivedMessageDetails.getRxSource().toString());
					else
						preparedStatement.setString(fieldNum, null);
				} else if (col.equals("SCHEMA_VERSION")) {
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeTimMetadata.getSchemaVersion());
				} else if (col.equals("SECURITY_RESULT_CODE")) {
					if (receivedMessageDetails != null) {
						SecurityResultCodeType securityResultCodeType = getSecurityResultCodeTypes().stream()
								.filter(x -> x.getSecurityResultCodeType()
										.equals(securityResultCode.toString()))
								.findFirst().orElse(null);
						preparedStatement.setInt(fieldNum, securityResultCodeType.getSecurityResultCodeTypeId());
					} else
						preparedStatement.setString(fieldNum, null);
				} else if (col.equals("LOG_FILE_NAME")) {
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, logFileName);
				} else if (col.equals("RECORD_GENERATED_AT")) {
					if (odeTimMetadata.getRecordGeneratedAt() != null) {
						java.util.Date recordGeneratedAtDate = convertDate(odeTimMetadata.getRecordGeneratedAt());
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
								mstFormat.format(recordGeneratedAtDate));
					} else {
						preparedStatement.setString(fieldNum, null);
					}
					// SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum,
					// java.sql.Timestamp.valueOf(LocalDateTime.parse(odeTimMetadata.getRecordGeneratedAt(),
					// DateTimeFormatter.ISO_DATE_TIME)));
				} else if (col.equals("SANITIZED")) {
					if (odeTimMetadata.isSanitized())
						preparedStatement.setString(fieldNum, "1");
					else
						preparedStatement.setString(fieldNum, "0");
				} else if (col.equals("SERIAL_ID_STREAM_ID"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							odeTimMetadata.getSerialId().getStreamId());
				else if (col.equals("SERIAL_ID_BUNDLE_SIZE"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
							odeTimMetadata.getSerialId().getBundleSize());
				else if (col.equals("SERIAL_ID_BUNDLE_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum,
							odeTimMetadata.getSerialId().getBundleId());
				else if (col.equals("SERIAL_ID_RECORD_ID"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
							odeTimMetadata.getSerialId().getRecordId());
				else if (col.equals("SERIAL_ID_SERIAL_NUMBER"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum,
							odeTimMetadata.getSerialId().getSerialNumber());
				else if (col.equals("PAYLOAD_TYPE")) {
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getPayloadType());
				} else if (col.equals("RECORD_TYPE") && recordType != null) {
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							recordType.toString());
				} else if (col.equals("ODE_RECEIVED_AT")) {
					if (odeTimMetadata.getOdeReceivedAt() != null) {
						java.util.Date receivedAtDate = convertDate(odeTimMetadata.getOdeReceivedAt());
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, mstFormat.format(receivedAtDate));
					} else {
						preparedStatement.setString(fieldNum, null);
					}
				} else
					preparedStatement.setString(fieldNum, null);
				fieldNum++;
			}
			// execute insert statement
			System.out.println(preparedStatement.toString());
			Long timId = log(preparedStatement, "timID");
			return timId;
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

	public static WydotOdeTravelerInformationMessage getTim(Long timId) {

		WydotOdeTravelerInformationMessage tim = new WydotOdeTravelerInformationMessage();

		Statement statement = null;
		ResultSet rs = null;
		Connection connection = null;

		try {
			// build SQL statement
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from tim where tim_id = " + timId);

			// convert to DriverAlertType objects
			while (rs.next()) {
				tim.setPacketID(rs.getString("PACKET_ID"));
				tim.setMsgCnt(rs.getInt("MSG_CNT"));
				tim.setTimeStamp(rs.getString("TIME_STAMP"));
				tim.setUrlB(rs.getString("URL_B"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return tim;
	}

	public static boolean deleteTim(Long timId) {

		boolean deleteTimResult = false;
		PreparedStatement preparedStatement = null;
		Connection connection = null;
		String deleteSQL = "DELETE FROM TIM WHERE TIM_ID = ?";

		try {
			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(deleteSQL);
			preparedStatement.setLong(1, timId);

			// execute delete SQL stetement
			deleteTimResult = updateOrDelete(preparedStatement);

			System.out.println("Tim is deleted!");

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
		return deleteTimResult;
	}
}
