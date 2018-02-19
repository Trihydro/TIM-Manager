package com.trihydro.service.bsm;

import java.sql.*;
import us.dot.its.jpo.ode.plugin.j2735.J2735Bsm;
import us.dot.its.jpo.ode.model.OdeBsmMetadata;
import java.text.ParseException;
import java.util.List;
import com.trihydro.service.tables.BsmOracleTables;
import com.trihydro.service.CvDataLoggerLibrary;
import com.trihydro.service.lookuptables.SecurityResultCodeTypeLut;
import com.trihydro.service.model.SecurityResultCodeType;

public class BsmCoreDataService extends CvDataLoggerLibrary {
	
	static PreparedStatement preparedStatement = null;
	static Statement statement = null;

	public static Long insertBSMCoreData(OdeBsmMetadata metadata, J2735Bsm bsm, Connection connection) {
 
		try {
			System.out.println("insert into bsm core data...");

			BsmOracleTables bsmOracleTables = new BsmOracleTables();

			List<SecurityResultCodeType> securityResultCodeTypes = SecurityResultCodeTypeLut.getSecurityResultCodeTypes(connection);

			String insertQueryStatement = bsmOracleTables.buildInsertQueryStatement("bsm_core_data", bsmOracleTables.getBsmCoreDataTable());

		    preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "bsm_core_data_id" });

			int fieldNum = 1;
			
			for(String col: bsmOracleTables.getBsmCoreDataTable()) {
				if(col.equals("ID")) {	
					preparedStatement.setString(fieldNum, bsm.getCoreData().getId()); 
				}
				else if(col.equals("MSGCNT")) {	
					if(bsm.getCoreData().getMsgCnt() != null)		
						preparedStatement.setInt(fieldNum, bsm.getCoreData().getMsgCnt());
					else
						preparedStatement.setString(fieldNum, null);		
				}
				else if(col.equals("SECMARK")) {	
					if(bsm.getCoreData().getSecMark() != null)	
						preparedStatement.setInt(fieldNum, bsm.getCoreData().getSecMark());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("POSITION_LAT")) {	
					if(bsm.getCoreData().getPosition().getLatitude() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getPosition().getLatitude());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("POSITION_LONG")) {	
					if(bsm.getCoreData().getPosition().getLongitude() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getPosition().getLongitude());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("POSITION_ELEV")) {	
					if(bsm.getCoreData().getPosition().getElevation() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getPosition().getElevation());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("ACCELSET_ACCELLAT")) {	
					if(bsm.getCoreData().getAccelSet().getAccelLat() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccelSet().getAccelLat());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("ACCELSET_ACCELLONG")) {	
					if(bsm.getCoreData().getAccelSet().getAccelLong() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccelSet().getAccelLong());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("ACCELSET_ACCELVERT")) {	
					if(bsm.getCoreData().getAccelSet().getAccelVert() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccelSet().getAccelVert());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("ACCELSET_ACCELYAW")) {	
					if(bsm.getCoreData().getAccelSet().getAccelYaw() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccelSet().getAccelYaw());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("ACCURACY_SEMIMAJOR")) {	
					if(bsm.getCoreData().getAccuracy().getSemiMajor() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccuracy().getSemiMajor());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("ACCURACY_SEMIMINOR")) {	
					if(bsm.getCoreData().getAccuracy().getSemiMinor() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccuracy().getSemiMinor());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("ACCURACY_ORIENTATION")) {	
					if(bsm.getCoreData().getAccuracy().getOrientation() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAccuracy().getOrientation());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("TRANSMISSION")) {
					if(bsm.getCoreData().getTransmission() != null)	
						preparedStatement.setString(fieldNum, bsm.getCoreData().getTransmission().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("SPEED")) {	
					if(bsm.getCoreData().getSpeed() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getSpeed());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("HEADING")) {	
					if(bsm.getCoreData().getHeading() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getHeading());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("ANGLE")) {	
					if(bsm.getCoreData().getAngle() != null)	
						preparedStatement.setBigDecimal(fieldNum, bsm.getCoreData().getAngle());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("BRAKES_WHEELBRAKES")) {	
					if(bsm.getCoreData().getBrakes().getWheelBrakes() != null)	
						preparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getWheelBrakes().toString());
					else
						preparedStatement.setString(fieldNum, null);	
				}
				else if(col.equals("BRAKES_TRACTION")) {	
					preparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getTraction());					
				}
				else if(col.equals("BRAKES_ABS")) {	
					preparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getAbs());					
				}
				else if(col.equals("BRAKES_SCS")) {	
					preparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getScs());					
				}
				else if(col.equals("BRAKES_BRAKEBOOST")) {	
					preparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getBrakeBoost());					
				}
				else if(col.equals("BRAKES_AUXBRAKES")) {	
					preparedStatement.setString(fieldNum, bsm.getCoreData().getBrakes().getAuxBrakes());					
				}
				else if(col.equals("SIZE_LENGTH")) {	
					if(bsm.getCoreData().getSize().getLength() != null)
						preparedStatement.setInt(fieldNum, bsm.getCoreData().getSize().getLength());		
					else
						preparedStatement.setString(fieldNum, null);				
				}
				else if(col.equals("SIZE_WIDTH")) {	
					if(bsm.getCoreData().getSize().getWidth() != null)
						preparedStatement.setInt(fieldNum, bsm.getCoreData().getSize().getWidth());		
					else
						preparedStatement.setString(fieldNum, null);				
				}
				else if(col.equals("LOG_FILE_NAME")) {	
					preparedStatement.setString(fieldNum, metadata.getLogFileName());			
				}
				else if(col.equals("RECORD_GENERATED_AT")) {						
					if(metadata.getRecordGeneratedAt() != null){
						java.util.Date recordGeneratedAtDate;					
						if(metadata.getRecordGeneratedAt().contains("."))
							recordGeneratedAtDate = utcFormatThree.parse(metadata.getRecordGeneratedAt());					
						else
							recordGeneratedAtDate = utcFormatTwo.parse(metadata.getRecordGeneratedAt());				
						//preparedStatement.setTimestamp(fieldNum, java.sql.Timestamp.valueOf(mstFormat.format(recordGeneratedAtDate)));
						preparedStatement.setString(fieldNum, mstFormat.format(recordGeneratedAtDate));
						
					}
					else
					preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("SECURITY_RESULT_CODE")) {
					SecurityResultCodeType securityResultCodeType = securityResultCodeTypes.stream()
                    .filter(x -> x.getSecurityResultCodeType().equals(metadata.getSecurityResultCode().toString()))
                    .findFirst()
                    .orElse(null);					
					preparedStatement.setInt(fieldNum, securityResultCodeType.getSecurityResultCodeTypeId());														
				}					
				else if(col.equals("SANITIZED")) {						
					if(metadata.isSanitized())
						preparedStatement.setString(fieldNum, "1");
					else
						preparedStatement.setString(fieldNum, "0");					
				}
				else if(col.equals("SERIAL_ID_STREAM_ID")) {											
					preparedStatement.setString(fieldNum, metadata.getSerialId().getStreamId());						
				}
				else if(col.equals("SERIAL_ID_BUNDLE_SIZE")) {										
					preparedStatement.setInt(fieldNum, metadata.getSerialId().getBundleSize());			
				}
				else if(col.equals("SERIAL_ID_BUNDLE_ID")) {										
					preparedStatement.setLong(fieldNum, metadata.getSerialId().getBundleId());		
				}
				else if(col.equals("SERIAL_ID_RECORD_ID")) {										
					preparedStatement.setInt(fieldNum, metadata.getSerialId().getRecordId());			
				}
				else if(col.equals("SERIAL_ID_SERIAL_NUMBER")) {										
					preparedStatement.setLong(fieldNum, metadata.getSerialId().getSerialNumber());			
				}
				else if(col.equals("ODE_RECEIVED_AT")) {										
					if(metadata.getOdeReceivedAt() != null){
						java.util.Date receivedAtDate;
						if(metadata.getOdeReceivedAt().contains("."))
							receivedAtDate = utcFormatThree.parse(metadata.getOdeReceivedAt());						
						else
							receivedAtDate = utcFormatTwo.parse(metadata.getOdeReceivedAt());		
							
						//receivedAtDate.setTimeZone(TimeZone.getTimeZone("MST"));
						//System.out.println(mstFormat.format(receivedAtDate));
						//System.out.println(java.sql.Timestamp.valueOf(mstFormat.format(receivedAtDate)));
						//preparedStatement.setTimestamp(fieldNum, java.sql.Timestamp.valueOf(mstFormat.format(receivedAtDate)), cal);
						preparedStatement.setString(fieldNum, mstFormat.format(receivedAtDate));
					}
					else
						preparedStatement.setString(fieldNum, null);						
				}
				else if(col.equals("RECORD_TYPE")) {															
					preparedStatement.setString(fieldNum, metadata.getRecordType().toString());
				}
				else if(col.equals("PAYLOAD_TYPE")) {															
					preparedStatement.setString(fieldNum, metadata.getPayloadType());
				}
				else if(col.equals("SCHEMA_VERSION")) {															
					if(metadata.getSchemaVersion() != null)
						preparedStatement.setInt(fieldNum, metadata.getSchemaVersion());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("RECORD_GENERATED_BY")) {															
					if(metadata.getRecordGeneratedBy() != null)
						preparedStatement.setString(fieldNum, metadata.getRecordGeneratedBy().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("BSM_SOURCE")) {																				
					preparedStatement.setString(fieldNum, metadata.getBsmSource().toString());
				}
				
				fieldNum++;
			}

  			// execute insert statement
 			Long bsmCoreDataId = log(preparedStatement, "bsmCoreDataId");
			return bsmCoreDataId;
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
		finally{
			try{
				preparedStatement.close();
			}
			catch(SQLException sqle){

			}
		}
		return new Long(0);
	}
}
