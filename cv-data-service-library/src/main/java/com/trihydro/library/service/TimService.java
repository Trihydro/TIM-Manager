package com.trihydro.library.service;

import us.dot.its.jpo.ode.model.OdeLogMetadataReceived;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.trihydro.library.service.SecurityResultCodeTypeService;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.tables.TimOracleTables;

public class TimService extends CvDataServiceLibrary {

	static PreparedStatement preparedStatement = null;

	public static Long insertTim(OdeLogMetadataReceived odeTimMetadata, J2735TravelerInformationMessage j2735TravelerInformationMessage) { 
		try {
			TimOracleTables timOracleTables = new TimOracleTables();
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("tim", timOracleTables.getTimTable());
			List<SecurityResultCodeType> securityResultCodeTypes = SecurityResultCodeTypeService.getSecurityResultCodeTypes(DbUtility.getConnection());		
			
			preparedStatement = DbUtility.getConnection().prepareStatement(insertQueryStatement, new String[] {"tim_id"});
			int fieldNum = 1;
			for(String col: timOracleTables.getTimTable()) {
				if(col.equals("MSG_CNT")) 
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, j2735TravelerInformationMessage.getMsgCnt());
				else if(col.equals("PACKET_ID"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, j2735TravelerInformationMessage.getPacketID());
				else if(col.equals("URL_B"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, j2735TravelerInformationMessage.getUrlB());
				else if(col.equals("TIME_STAMP")){
					SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(LocalDateTime.parse(j2735TravelerInformationMessage.getTimeStamp(), DateTimeFormatter.ISO_DATE_TIME)));																
					// if(localDateTimeformatter.parse(j2735TravelerInformationMessage.getTimeStamp() == null)){
					// 	LocalDateTime timeStampDateTime = LocalDateTime.parse(j2735TravelerInformationMessage.getTimeStamp(), localDateTimeformatter);	
					// } 
					// else if(localDateTimeformatter.parse(j2735TravelerInformationMessage.getTimeStamp() == null))

					// LocalDateTime timeStampDateTime = LocalDateTime.parse(j2735TravelerInformationMessage.getTimeStamp(), localDateTimeformatter);
					// System.out.println(timeStampDateTime.toString());
					// Timestamp ts = Timestamp.valueOf(timeStampDateTime);					
					// SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, ts);
					// preparedStatement.setString(fieldNum, null);
					
					//SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(LocalDateTime.parse(j2735TravelerInformationMessage.getTimeStamp(), DateTimeFormatter.ISO_DATE_TIME)));							
					
				}		
				else if(col.equals("RECORD_GENERATED_BY"))	{
					if(odeTimMetadata.getRecordGeneratedBy() != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getRecordGeneratedBy().toString());											
					else
						preparedStatement.setString(fieldNum, null);																
				}					
				else if(col.equals("RMD_LD_ELEVATION")){
					if(odeTimMetadata.getReceivedMessageDetails() != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getLocationData().getElevation());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("RMD_LD_HEADING")){
					if(odeTimMetadata.getReceivedMessageDetails() != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getLocationData().getHeading());
					else
						preparedStatement.setString(fieldNum, null);	
				}					
				else if(col.equals("RMD_LD_LATITUDE")){
					if(odeTimMetadata.getReceivedMessageDetails() != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getLocationData().getLatitude());
					else
						preparedStatement.setString(fieldNum, null);	
				}					
				else if(col.equals("RMD_LD_LONGITUDE")){
					if(odeTimMetadata.getReceivedMessageDetails() != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getLocationData().getLongitude());
					else
						preparedStatement.setString(fieldNum, null);	
				}					
				else if(col.equals("RMD_LD_SPEED")){
					if(odeTimMetadata.getReceivedMessageDetails() != null)
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getLocationData().getSpeed());
					else
						preparedStatement.setString(fieldNum, null);	
				}					
				else if(col.equals("RMD_RX_SOURCE")){					
					if(odeTimMetadata.getReceivedMessageDetails() != null && odeTimMetadata.getReceivedMessageDetails().getRxSource() != null)					
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getRxSource().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("SCHEMA_VERSION"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeTimMetadata.getSchemaVersion());
				else if(col.equals("SECURITY_RESULT_CODE")) {
					if(odeTimMetadata.getSecurityResultCode() != null){
						SecurityResultCodeType securityResultCodeType = securityResultCodeTypes.stream()
						.filter(x -> x.getSecurityResultCodeType().equals(odeTimMetadata.getSecurityResultCode().toString()))
						.findFirst()
						.orElse(null);						
						preparedStatement.setInt(fieldNum, securityResultCodeType.getSecurityResultCodeTypeId());	
					}
					else
						preparedStatement.setString(fieldNum, null);																	
				}												
				else if(col.equals("LOG_FILE_NAME"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getLogFileName());
				else if(col.equals("RECORD_GENERATED_AT")){
					if(odeTimMetadata.getRecordGeneratedAt() != null){
						java.util.Date recordGeneratedAtDate = convertDate(odeTimMetadata.getRecordGeneratedAt());				
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, mstFormat.format(recordGeneratedAtDate));	
					}
					else{
						preparedStatement.setString(fieldNum, null);																							
					}
					//SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, java.sql.Timestamp.valueOf(LocalDateTime.parse(odeTimMetadata.getRecordGeneratedAt(), DateTimeFormatter.ISO_DATE_TIME)));																					
				}
				else if(col.equals("SANITIZED")) {
					if(odeTimMetadata.isSanitized())
						preparedStatement.setString(fieldNum, "1");
					else
						preparedStatement.setString(fieldNum, "0");								
				}				
				else if(col.equals("SERIAL_ID_STREAM_ID"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getSerialId().getStreamId());	
				else if(col.equals("SERIAL_ID_BUNDLE_SIZE"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeTimMetadata.getSerialId().getBundleSize());
				else if(col.equals("SERIAL_ID_BUNDLE_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, odeTimMetadata.getSerialId().getBundleId());
				else if(col.equals("SERIAL_ID_RECORD_ID"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeTimMetadata.getSerialId().getRecordId());
				else if(col.equals("SERIAL_ID_SERIAL_NUMBER"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, odeTimMetadata.getSerialId().getSerialNumber());
				else if(col.equals("PAYLOAD_TYPE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getPayloadType());
				else if(col.equals("RECORD_TYPE") && odeTimMetadata.getRecordType() != null)
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getRecordType().toString());
				else if(col.equals("ODE_RECEIVED_AT")) {
					if(odeTimMetadata.getOdeReceivedAt() != null) {
						java.util.Date receivedAtDate = convertDate(odeTimMetadata.getOdeReceivedAt());				
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, mstFormat.format(receivedAtDate));				
					}
					else{
						preparedStatement.setString(fieldNum, null);	
					}
				}
				else if(col.equals("RSU_INDEX"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, j2735TravelerInformationMessage.getIndex());
				else
					preparedStatement.setString(fieldNum, null);											
				fieldNum++;
			}			
			// execute insert statement
			System.out.println(preparedStatement.toString());
			Long timId = log(preparedStatement, "timID");
			return timId;
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {			
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new Long(0);
	}

	public static J2735TravelerInformationMessage getTim(Long timId) { 
		
		J2735TravelerInformationMessage tim = new J2735TravelerInformationMessage();
		
		try {
			// build SQL statement
				Statement statement = DbUtility.getConnection().createStatement();
				ResultSet rs = statement.executeQuery("select * from tim where tim_id = " + timId);
				try {
					// convert to DriverAlertType objects   			
					while (rs.next()) {   			
						tim.setPacketID(rs.getString("PACKET_ID"));
						tim.setMsgCnt(rs.getInt("MSG_CNT"));
						tim.setTimeStamp(rs.getString("TIME_STAMP"));
						tim.setUrlB(rs.getString("URL_B"));
						tim.setIndex(rs.getInt("RSU_INDEX"));
					}
				}
				finally {
					try {
						rs.close();
					}
					catch (Exception e) {
						e.printStackTrace();
					}					
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return tim;
	}

	public static boolean deleteTim(Long timId){
		
		boolean deleteTimResult = false;

		String deleteSQL = "DELETE FROM TIM WHERE TIM_ID = ?";

		try {			
		
			preparedStatement = DbUtility.getConnection().prepareStatement(deleteSQL);			
			preparedStatement.setLong(1, timId);

			// execute delete SQL stetement
			deleteTimResult = updateOrDelete(preparedStatement);

			System.out.println("Tim is deleted!");

		}catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				preparedStatement.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}					
		}
		return deleteTimResult;
	}
}

