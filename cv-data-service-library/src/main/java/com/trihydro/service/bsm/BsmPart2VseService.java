package com.trihydro.service.bsm;

import java.sql.*;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmPart2Content;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;
import com.trihydro.service.CvDataLoggerLibrary;
import com.trihydro.service.tables.BsmOracleTables;

public class BsmPart2VseService extends CvDataLoggerLibrary {
	
	static PreparedStatement preparedStatement = null;

	public static Long insertBSMPart2VSE(J2735BsmPart2Content part2Content, J2735VehicleSafetyExtensions vse, Long bsmCoreDataId, Connection connection) {

		try {
			BsmOracleTables bsmOracleTables = new BsmOracleTables();	
			String insertQueryStatement = bsmOracleTables.buildInsertQueryStatement("bsm_part2_vse", bsmOracleTables.getBsmPart2VseTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"bsm_part2_vse_id"});

			int fieldNum = 1;
			
			for(String col: bsmOracleTables.getBsmPart2VseTable()) {
				if(col.equals("BSM_CORE_DATA_ID")) {	
					preparedStatement.setLong(fieldNum, bsmCoreDataId);
				}
				else if(col.equals("ID")){
					preparedStatement.setString(fieldNum, part2Content.getId().name());
				}
				else if(col.equals("EVENTS")){
					if(vse.getEvents() != null)
						preparedStatement.setString(fieldNum, vse.getEvents().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_LAT")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getPosition().getLatitude() != null)
						preparedStatement.setBigDecimal(fieldNum, vse.getPathHistory().getInitialPosition().getPosition().getLatitude());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_LONG")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getPosition().getLongitude() != null)
						preparedStatement.setBigDecimal(fieldNum, vse.getPathHistory().getInitialPosition().getPosition().getLongitude());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_ELEV")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getPosition().getElevation() != null)
						preparedStatement.setBigDecimal(fieldNum, vse.getPathHistory().getInitialPosition().getPosition().getElevation());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_HEADING")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getHeading() != null)
						preparedStatement.setBigDecimal(fieldNum, vse.getPathHistory().getInitialPosition().getHeading());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_POSACCRCY_SEMIMAJ")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getPosAccuracy().getSemiMajor() != null)
						preparedStatement.setBigDecimal(fieldNum, vse.getPathHistory().getInitialPosition().getPosAccuracy().getSemiMajor());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_POSACCRCY_SEMIMIN")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getPosAccuracy().getSemiMinor() != null)
						preparedStatement.setBigDecimal(fieldNum, vse.getPathHistory().getInitialPosition().getPosAccuracy().getSemiMinor());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_POSACCRCY_ORIEN")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getPosAccuracy().getOrientation() != null)
						preparedStatement.setBigDecimal(fieldNum, vse.getPathHistory().getInitialPosition().getPosAccuracy().getOrientation());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_POSCONFIDENCE_POS")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getPosConfidence().getPos() != null)
						preparedStatement.setString(fieldNum, vse.getPathHistory().getInitialPosition().getPosConfidence().getPos().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_POSCONFIDENCE_ELEV")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getPosConfidence().getElevation() != null)
						preparedStatement.setString(fieldNum, vse.getPathHistory().getInitialPosition().getPosConfidence().getElevation().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_SPEED")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getSpeed().getSpeed() != null)
						preparedStatement.setBigDecimal(fieldNum, vse.getPathHistory().getInitialPosition().getSpeed().getSpeed());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_TRANSMISSION")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getSpeed().getTransmisson() != null)
						preparedStatement.setString(fieldNum, vse.getPathHistory().getInitialPosition().getSpeed().getTransmisson().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_SPEEDCONF_HEADING")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getSpeedConfidence().getHeading() != null)
						preparedStatement.setString(fieldNum, vse.getPathHistory().getInitialPosition().getSpeedConfidence().getHeading().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_SPEEDCONF_SPEED")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getSpeedConfidence().getSpeed() != null)
						preparedStatement.setString(fieldNum, vse.getPathHistory().getInitialPosition().getSpeedConfidence().getSpeed().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_SPEEDCONF_THROTTLE")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getSpeedConfidence().getThrottle() != null)
						preparedStatement.setString(fieldNum, vse.getPathHistory().getInitialPosition().getSpeedConfidence().getThrottle().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_TIMECONF")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getTimeConfidence() != null)
						preparedStatement.setString(fieldNum, vse.getPathHistory().getInitialPosition().getTimeConfidence().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_UTCTIME_DAY")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getUtcTime().getDay() != null)
						preparedStatement.setInt(fieldNum, vse.getPathHistory().getInitialPosition().getUtcTime().getDay());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_UTCTIME_HOUR")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getUtcTime().getHour() != null)
						preparedStatement.setInt(fieldNum, vse.getPathHistory().getInitialPosition().getUtcTime().getHour());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_UTCTIME_MINUTE")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getUtcTime().getMinute() != null)
						preparedStatement.setInt(fieldNum, vse.getPathHistory().getInitialPosition().getUtcTime().getMinute());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_UTCTIME_MONTH")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getUtcTime().getMonth() != null)
						preparedStatement.setInt(fieldNum, vse.getPathHistory().getInitialPosition().getUtcTime().getMonth());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_UTCTIME_OFFSET")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getUtcTime().getOffset() != null)
						preparedStatement.setInt(fieldNum, vse.getPathHistory().getInitialPosition().getUtcTime().getOffset());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_UTCTIME_SECOND")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getUtcTime().getSecond() != null)
						preparedStatement.setInt(fieldNum, vse.getPathHistory().getInitialPosition().getUtcTime().getSecond());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_INITPOS_UTCTIME_YEAR")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null && vse.getPathHistory().getInitialPosition().getUtcTime().getYear() != null)
						preparedStatement.setInt(fieldNum, vse.getPathHistory().getInitialPosition().getUtcTime().getYear());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_CURRGNSSSTATUS")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getCurrGNSSstatus() != null)
						preparedStatement.setString(fieldNum, vse.getPathHistory().getCurrGNSSstatus().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PH_CRUMBDATA")){
					if(vse.getPathHistory() != null && vse.getPathHistory().getCrumbData() != null)			
						preparedStatement.setString(fieldNum, vse.getPathHistory().getCrumbData().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PP_CONFIDENCE")){
					if(vse.getPathPrediction() != null && vse.getPathPrediction().getConfidence() != null)
						preparedStatement.setBigDecimal(fieldNum, vse.getPathPrediction().getConfidence());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("PP_RADIUSOFCURVE")){
					if(vse.getPathPrediction() != null && vse.getPathPrediction().getRadiusOfCurve() != null)
						preparedStatement.setBigDecimal(fieldNum, vse.getPathPrediction().getRadiusOfCurve());
					else
						preparedStatement.setString(fieldNum, null);
				}
				else if(col.equals("LIGHTS")){
				    if(vse.getLights() != null)
						preparedStatement.setString(fieldNum, vse.getLights().toString());
					else
						preparedStatement.setString(fieldNum, null);
				}
				fieldNum++;
			}


			// execute insert statement
 			Long bsmPart2VseId = log(preparedStatement, "bsmPart2VseId");
			return bsmPart2VseId;
		}
		catch (SQLException e) {
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
