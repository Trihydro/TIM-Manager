package com.trihydro.library.service.driveralert;

import java.sql.Connection;
import us.dot.its.jpo.ode.model.OdeLogMetadataReceived;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataLoggerLibrary;
import com.trihydro.library.service.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import com.trihydro.library.model.DriverAlertType;
import com.trihydro.library.service.lookuptables.SecurityResultCodeTypeLut;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.library.service.tables.DriverAlertOracleTables;

public class DriverAlertService extends CvDataLoggerLibrary {

    static PreparedStatement preparedStatement = null;
    static List<DriverAlertType> driverAlertTypes;

	public static Long insertDriverAlert(OdeLogMetadataReceived odeDriverAlertMetadata, String alert, Connection connection) { 
		
		driverAlertTypes = DriverAlertTypeService.selectAll(connection);
		
		try {
			
			DriverAlertOracleTables driverAlertOracleTables = new DriverAlertOracleTables();
			String insertQueryStatement = driverAlertOracleTables.buildInsertQueryStatement("driver_alert", driverAlertOracleTables.getDriverAlertTable());
			List<SecurityResultCodeType> securityResultCodeTypes = SecurityResultCodeTypeLut.getSecurityResultCodeTypes(connection);		
			
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"driver_alert_id"});
			int fieldNum = 1;
			for(String col: driverAlertOracleTables.getDriverAlertTable()) {				
				if(col.equals("RECORD_GENERATED_BY"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getRecordGeneratedBy().toString());														
				else if(col.equals("SCHEMA_VERSION"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getSchemaVersion());
				// else if(col.equals("SECURITY_RESULT_CODE")) {
				// 	SecurityResultCodeType securityResultCodeType = securityResultCodeTypes.stream()
				// 	.filter(x -> x.getSecurityResultCodeType().equals(odeDriverAlertMetadata.getSecurityResultCode().toString()))
				// 	.findFirst()
				// 	.orElse(null);					
				// 	preparedStatement.setInt(fieldNum, securityResultCodeType.getSecurityResultCodeTypeId());														
				// }													
				else if(col.equals("LOG_FILE_NAME"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getLogFileName());
				else if(col.equals("RECORD_GENERATED_AT")){
					if(odeDriverAlertMetadata.getRecordGeneratedAt() != null) {
						java.util.Date recordGeneratedAtDate = null;					
						if(odeDriverAlertMetadata.getRecordGeneratedAt().contains(".")) {
							try {
								recordGeneratedAtDate = utcFormatThree.parse(odeDriverAlertMetadata.getRecordGeneratedAt());
							} 
							catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else {
							try {
								recordGeneratedAtDate = utcFormatTwo.parse(odeDriverAlertMetadata.getRecordGeneratedAt());
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}																
						}
						preparedStatement.setString(fieldNum, mstFormat.format(recordGeneratedAtDate));	
					}
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("SANITIZED")) {
					if(odeDriverAlertMetadata.isSanitized())
						preparedStatement.setString(fieldNum, "1");
					else
						preparedStatement.setString(fieldNum, "0");								
				}				
				else if(col.equals("SERIAL_ID_STREAM_ID"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getSerialId().getStreamId());	
				else if(col.equals("SERIAL_ID_BUNDLE_SIZE"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getSerialId().getBundleSize());
				else if(col.equals("SERIAL_ID_BUNDLE_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getSerialId().getBundleId());
				else if(col.equals("SERIAL_ID_RECORD_ID"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getSerialId().getRecordId());
				else if(col.equals("SERIAL_ID_SERIAL_NUMBER"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getSerialId().getSerialNumber());
				else if(col.equals("PAYLOAD_TYPE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getPayloadType());
				else if(col.equals("RECORD_TYPE"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getRecordType().toString());
				else if(col.equals("ODE_RECEIVED_AT")) {
					if(odeDriverAlertMetadata.getOdeReceivedAt() != null){
						java.util.Date receivedAtDate = null;
						if(odeDriverAlertMetadata.getOdeReceivedAt().contains(".")){
							try {
								receivedAtDate = utcFormatThree.parse(odeDriverAlertMetadata.getOdeReceivedAt());
							} catch (ParseException e1) {
								e1.printStackTrace();
							}
						}
						else {
							try {
								receivedAtDate = utcFormatTwo.parse(odeDriverAlertMetadata.getOdeReceivedAt());
							} 
							catch (ParseException e) {
								e.printStackTrace();
							}
						}		
						preparedStatement.setString(fieldNum, mstFormat.format(receivedAtDate));												
					}
                }                
                else if(col.equals("LATITUDE")) 
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getLatitude());                
                else if(col.equals("LONGITUDE")) 
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getLongitude());                
                else if(col.equals("HEADING")) 
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getHeading());                
                else if(col.equals("ELEVATION_M")) 
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getElevation());                
                else if(col.equals("SPEED")) 
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, odeDriverAlertMetadata.getReceivedMessageDetails().getLocationData().getSpeed());                
                else if(col.equals("DRIVER_ALERT_TYPE_ID")) {
                    for(DriverAlertType dat : driverAlertTypes){
                        if(dat.getShortName().equals(alert)){
                            SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, dat.getDriverAlertTypeId());
                        }
                    }
                }
				else
					preparedStatement.setString(fieldNum, null);											
				fieldNum++;
			}			
			// execute insert statement
			Long driverAlertId = log(preparedStatement, "driverAlertId");
			return driverAlertId;
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
	 
}

