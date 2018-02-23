package com.trihydro.library.service.tim;

import java.sql.Connection;
import us.dot.its.jpo.ode.model.OdeTimMetadata;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.library.service.CvDataLoggerLibrary;
import com.trihydro.library.service.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import com.trihydro.library.service.lookuptables.SecurityResultCodeTypeLut;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.service.tables.TimOracleTables;

public class TimService extends CvDataLoggerLibrary {

	static PreparedStatement preparedStatement = null;

	public static Long insertTim(OdeTimMetadata odeTimMetadata, J2735TravelerInformationMessage j2735TravelerInformationMessage, Connection connection) { 
		try {
			TimOracleTables timOracleTables = new TimOracleTables();
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("tim", timOracleTables.getTimTable());
			List<SecurityResultCodeType> securityResultCodeTypes = SecurityResultCodeTypeLut.getSecurityResultCodeTypes(connection);		
			
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"tim_id"});
			int fieldNum = 1;
			for(String col: timOracleTables.getTimTable()) {
				if(col.equals("MSG_CNT")) 
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, j2735TravelerInformationMessage.getMsgCnt());
				else if(col.equals("PACKET_ID"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, j2735TravelerInformationMessage.getPacketID());
				else if(col.equals("URL_B"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, j2735TravelerInformationMessage.getUrlB());
				else if(col.equals("TIME_STAMP")){
					LocalDateTime timeStampDateTime = LocalDateTime.parse(j2735TravelerInformationMessage.getTimeStamp(), localDateTimeformatter);
					System.out.println(timeStampDateTime.toString());
					Timestamp ts = Timestamp.valueOf(timeStampDateTime);					
					SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, ts);	
				}		
				else if(col.equals("RECORD_GENERATED_BY"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getRecordGeneratedBy().toString());											
				else if(col.equals("RMD_LD_ELEVATION"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getLocationData().getElevation());
				else if(col.equals("RMD_LD_HEADING"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getLocationData().getHeading());
				else if(col.equals("RMD_LD_LATITUDE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getLocationData().getLatitude());
				else if(col.equals("RMD_LD_LONGITUDE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getLocationData().getLongitude());
				else if(col.equals("RMD_LD_SPEED"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getLocationData().getSpeed());
				else if(col.equals("RMD_RX_SOURCE"))
					if(odeTimMetadata.getReceivedMessageDetails().getRxSource() != null)					
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getReceivedMessageDetails().getRxSource().toString());
					else
						preparedStatement.setString(fieldNum, null);
				else if(col.equals("SCHEMA_VERSION"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeTimMetadata.getSchemaVersion());
				else if(col.equals("SECURITY_RESULT_CODE")) {
					SecurityResultCodeType securityResultCodeType = securityResultCodeTypes.stream()
					.filter(x -> x.getSecurityResultCodeType().equals(odeTimMetadata.getSecurityResultCode().toString()))
					.findFirst()
					.orElse(null);						
					preparedStatement.setInt(fieldNum, securityResultCodeType.getSecurityResultCodeTypeId());														
				}												
				else if(col.equals("LOG_FILE_NAME"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getLogFileName());
				else if(col.equals("RECORD_GENERATED_AT")){
					java.util.Date recordGeneratedAtDate = convertDate(odeTimMetadata.getRecordGeneratedAt());				
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, mstFormat.format(recordGeneratedAtDate));		
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
				else if(col.equals("RECORD_TYPE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeTimMetadata.getRecordType().toString());
				else if(col.equals("ODE_RECEIVED_AT")) {
					java.util.Date receivedAtDate = convertDate(odeTimMetadata.getOdeReceivedAt());				
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, mstFormat.format(receivedAtDate));				
				}
				else if(col.equals("RSU_INDEX"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, j2735TravelerInformationMessage.getIndex());
				else
					preparedStatement.setString(fieldNum, null);											
				fieldNum++;
			}			
			// execute insert statement
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

	public static J2735TravelerInformationMessage getTim(Long timId, Connection connection) { 
		
		J2735TravelerInformationMessage tim = new J2735TravelerInformationMessage();
		
		try {
			// build SQL statement
				Statement statement = connection.createStatement();
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

	public static boolean deleteTim(Long timId, Connection connection){
		
		boolean deleteTimResult = false;

		String deleteSQL = "DELETE FROM TIM WHERE TIM_ID = ?";

		try {			
		
			preparedStatement = connection.prepareStatement(deleteSQL);			
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

