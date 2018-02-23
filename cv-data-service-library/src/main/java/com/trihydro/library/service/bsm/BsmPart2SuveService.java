package com.trihydro.library.service.bsm;

import java.sql.*;
import us.dot.its.jpo.ode.plugin.j2735.J2735BsmPart2Content;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;
import com.trihydro.library.service.CvDataLoggerLibrary;
import com.trihydro.library.service.tables.BsmOracleTables;

public class BsmPart2SuveService extends CvDataLoggerLibrary {

	static PreparedStatement preparedStatement = null;
	static Statement statement = null;

	public static Long insertBSMPart2SUVE(J2735BsmPart2Content part2Content, J2735SupplementalVehicleExtensions suve, Long bsmCoreDataId, Connection connection) {
		try {

            BsmOracleTables bsmOracleTables = new BsmOracleTables();	
			String insertQueryStatement = bsmOracleTables.buildInsertQueryStatement("bsm_part2_suve", bsmOracleTables.getBsmPart2SuveTable());

            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"bsm_part2_suve_id"});

            int fieldNum = 1;

            // bsmCoreDataId 1
			preparedStatement.setString(fieldNum, Long.toString(bsmCoreDataId));
			fieldNum++;

			// id 2
			preparedStatement.setString(fieldNum, part2Content.getId().name());
			fieldNum++;

			// classification 3
			if(suve.getClassification() != null)
				preparedStatement.setString(fieldNum, suve.getClassification().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_fueltype 4
			if(suve.getClassDetails() != null && suve.getClassDetails().getFuelType() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getFuelType().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_hpmstype 5
			if(suve.getClassDetails() != null && suve.getClassDetails().getHpmsType() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getHpmsType().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_iso3883 6
			if(suve.getClassDetails() != null && suve.getClassDetails().getIso3883() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getIso3883().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_keytype 7
			if(suve.getClassDetails() != null && suve.getClassDetails().getKeyType() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getKeyType().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_regional 8
			if(suve.getClassDetails() != null && suve.getClassDetails().getRegional() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getRegional().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_respondertype 9
			if(suve.getClassDetails() != null && suve.getClassDetails().getResponderType() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getResponderType().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_responseequip_name 10
			if(suve.getClassDetails() != null && suve.getClassDetails().getResponseEquip() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getResponseEquip().getName());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_responseequip_value 11
			if(suve.getClassDetails() != null && suve.getClassDetails().getResponseEquip() != null &&  suve.getClassDetails().getResponseEquip().getValue() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getResponseEquip().getValue().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_role 12
			if(suve.getClassDetails() != null && suve.getClassDetails().getRole() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getRole().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_vehicletype_name 13
			if(suve.getClassDetails() != null && suve.getClassDetails().getVehicleType() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getVehicleType().getName());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_vehicletype_name 14
			if(suve.getClassDetails() != null && suve.getClassDetails().getVehicleType() != null && suve.getClassDetails().getVehicleType().getValue() != null)
				preparedStatement.setString(fieldNum, suve.getClassDetails().getVehicleType().getValue().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// vd_bumpers_front 15
			if(suve.getVehicleData() != null && suve.getVehicleData().getBumpers() != null && suve.getVehicleData().getBumpers().getFront() != null)
				preparedStatement.setString(fieldNum, suve.getVehicleData().getBumpers().getFront().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// vd_bumpers_rear 16
			if(suve.getVehicleData() != null && suve.getVehicleData().getBumpers() != null && suve.getVehicleData().getBumpers().getRear() != null)
				preparedStatement.setString(fieldNum, suve.getVehicleData().getBumpers().getRear().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// vd_height 17
			if(suve.getVehicleData() != null && suve.getVehicleData().getHeight() != null)
				preparedStatement.setString(fieldNum, suve.getVehicleData().getHeight().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// vd_mass 18
			if(suve.getVehicleData() != null && suve.getVehicleData().getMass() != null)
				preparedStatement.setString(fieldNum, suve.getVehicleData().getMass().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// vd_trailerweight 19
			if(suve.getVehicleData() != null && suve.getVehicleData().getTrailerWeight() != null)
				preparedStatement.setString(fieldNum, suve.getVehicleData().getTrailerWeight().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_friction 20
			if(suve.getWeatherReport() != null && suve.getWeatherReport().getFriction() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherReport().getFriction().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_israining 21
			if(suve.getWeatherReport() != null && suve.getWeatherReport().getIsRaining() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherReport().getIsRaining().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_precipsituation 22
			if(suve.getWeatherReport() != null && suve.getWeatherReport().getPrecipSituation() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherReport().getPrecipSituation().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_rainrate 23
			if(suve.getWeatherReport() != null && suve.getWeatherReport().getRainRate() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherReport().getRainRate().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_roadfriction 24
			if(suve.getWeatherReport() != null && suve.getWeatherReport().getRoadFriction() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherReport().getRoadFriction().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_solarradiation 25
			if(suve.getWeatherReport() != null && suve.getWeatherReport().getSolarRadiation() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherReport().getSolarRadiation().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_airpressure 26
			if(suve.getWeatherProbe() != null && suve.getWeatherProbe().getAirPressure() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherProbe().getAirPressure().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_airtemp 27
			if(suve.getWeatherProbe() != null && suve.getWeatherProbe().getAirTemp() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherProbe().getAirTemp().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_rainrates_ratefront 28
			if(suve.getWeatherProbe() != null && suve.getWeatherProbe().getRainRates() != null && suve.getWeatherProbe().getRainRates().getRateFront() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherProbe().getRainRates().getRateFront().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_rainrates_raterear 29
			if(suve.getWeatherProbe() != null && suve.getWeatherProbe().getRainRates() != null && suve.getWeatherProbe().getRainRates().getRateRear() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherProbe().getRainRates().getRateRear().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_rainrates_statusfront 30
			if(suve.getWeatherProbe() != null && suve.getWeatherProbe().getRainRates() != null && suve.getWeatherProbe().getRainRates().getStatusFront() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherProbe().getRainRates().getStatusFront().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_rainrates_statusrear 31
			if(suve.getWeatherProbe() != null && suve.getWeatherProbe().getRainRates() != null && suve.getWeatherProbe().getRainRates().getStatusRear() != null)
				preparedStatement.setString(fieldNum, suve.getWeatherProbe().getRainRates().getStatusRear().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_datetime 32
			if(suve.getObstacle() != null && suve.getObstacle().getDateTime() != null)
				preparedStatement.setString(fieldNum, suve.getObstacle().getDateTime().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_description 33
			if(suve.getObstacle() != null && suve.getObstacle().getDescription() != null)
				preparedStatement.setString(fieldNum, suve.getObstacle().getDescription().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_locationdetails_name 34
			if(suve.getObstacle() != null && suve.getObstacle().getLocationDetails() != null)
				preparedStatement.setString(fieldNum, suve.getObstacle().getLocationDetails().getName());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_locationdetails_value 35
			if(suve.getObstacle() != null && suve.getObstacle().getLocationDetails() != null && suve.getObstacle().getLocationDetails().getValue() != null)
				preparedStatement.setString(fieldNum, suve.getObstacle().getLocationDetails().getValue().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_obdirect 36
			if(suve.getObstacle() != null && suve.getObstacle().getObDirect() != null)
				preparedStatement.setString(fieldNum, suve.getObstacle().getObDirect().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_obdist 37
			if(suve.getObstacle() != null && suve.getObstacle().getObDist() != null)
				preparedStatement.setString(fieldNum, suve.getObstacle().getObDist().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_vertevent 38
			if(suve.getObstacle() != null && suve.getObstacle().getVertEvent() != null)
				preparedStatement.setString(fieldNum, suve.getObstacle().getVertEvent().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// st_statusdetails 39
			if(suve.getStatus() != null && suve.getStatus().getStatusDetails() != null)
				preparedStatement.setString(fieldNum, suve.getStatus().getStatusDetails().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// st_locationdetails_name 40
			if(suve.getStatus() != null && suve.getStatus().getLocationDetails() != null)
				preparedStatement.setString(fieldNum, suve.getStatus().getLocationDetails().getName());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// st_locationdetails_value 41
			if(suve.getStatus() != null && suve.getStatus().getLocationDetails() != null && suve.getStatus().getLocationDetails().getValue() != null)
				preparedStatement.setString(fieldNum, suve.getStatus().getLocationDetails().getValue().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// sp_speedreports 42
			if(suve.getSpeedProfile() != null && suve.getSpeedProfile().getSpeedReports() != null)
				preparedStatement.setString(fieldNum, suve.getSpeedProfile().getSpeedReports().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// rtcm_msgs 43
			if(suve.getTheRTCM() != null && suve.getTheRTCM().getMsgs() != null)
				preparedStatement.setString(fieldNum, suve.getTheRTCM().getMsgs().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// rtcm_rtcmheader_offsetset_antoffsetx 44
			if(suve.getTheRTCM() != null && suve.getTheRTCM().getRtcmHeader() != null && suve.getTheRTCM().getRtcmHeader().getOffsetSet() != null && suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetX() != null)
				preparedStatement.setString(fieldNum, suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetX().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// rtcm_rtcmheader_offsetset_antoffsety 45
			if(suve.getTheRTCM() != null && suve.getTheRTCM().getRtcmHeader() != null && suve.getTheRTCM().getRtcmHeader().getOffsetSet() != null && suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetY() != null)
				preparedStatement.setString(fieldNum, suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetY().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// rtcm_rtcmheader_offsetset_antoffsetz 46
			if(suve.getTheRTCM() != null && suve.getTheRTCM().getRtcmHeader() != null && suve.getTheRTCM().getRtcmHeader().getOffsetSet() != null && suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetZ() != null)
				preparedStatement.setString(fieldNum, suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetZ().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// rtcm_rtcmheader_status 47
			if(suve.getTheRTCM() != null && suve.getTheRTCM().getRtcmHeader() != null && suve.getTheRTCM().getRtcmHeader().getStatus() != null)
				preparedStatement.setString(fieldNum, suve.getTheRTCM().getRtcmHeader().getStatus().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// regional 48
			if(suve.getRegional() != null)
				preparedStatement.setString(fieldNum, suve.getRegional().toString());
			else
				preparedStatement.setString(fieldNum, null);
			fieldNum++;

			// execute insert statement
 			Long bsmPart2SuveId = log(preparedStatement, "bsmPart2SuveId");			
			return bsmPart2SuveId;
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
