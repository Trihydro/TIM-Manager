package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.BsmOracleTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.j2735.J2735BsmPart2Content;
import us.dot.its.jpo.ode.plugin.j2735.J2735SupplementalVehicleExtensions;

@Component
public class BsmPart2SuveService extends BaseService {

    private BsmOracleTables bsmOracleTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(BsmOracleTables _bsmOracleTables, SQLNullHandler _sqlNullHandler) {
        bsmOracleTables = _bsmOracleTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long insertBSMPart2SUVE(J2735BsmPart2Content part2Content, J2735SupplementalVehicleExtensions suve,
			Long bsmCoreDataId) {

		String bsmSuveInsertQueryStatement = bsmOracleTables.buildInsertQueryStatement("bsm_part2_suve",
        bsmOracleTables.getBsmPart2SuveTable());
		PreparedStatement bsmSuvePreparedStatement = null;
		Connection connection = null;

		try {

			connection = GetConnectionPool();
			bsmSuvePreparedStatement = connection.prepareStatement(bsmSuveInsertQueryStatement,
					new String[] { "bsm_part2_suve_id" });

			int fieldNum = 1;

			// bsmCoreDataId 1
			bsmSuvePreparedStatement.setString(fieldNum, Long.toString(bsmCoreDataId));
			fieldNum++;

			// id 2
			bsmSuvePreparedStatement.setString(fieldNum, part2Content.getId().name());
			fieldNum++;

			// classification 3
			if (suve.getClassification() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getClassification().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_fueltype 4
			if (suve.getClassDetails() != null && suve.getClassDetails().getFuelType() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getClassDetails().getFuelType().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_hpmstype 5
			if (suve.getClassDetails() != null && suve.getClassDetails().getHpmsType() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getClassDetails().getHpmsType().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_iso3883 6
			if (suve.getClassDetails() != null && suve.getClassDetails().getIso3883() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getClassDetails().getIso3883().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_keytype 7
			if (suve.getClassDetails() != null && suve.getClassDetails().getKeyType() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getClassDetails().getKeyType().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_regional 8
			if (suve.getClassDetails() != null && suve.getClassDetails().getRegional() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getClassDetails().getRegional().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_respondertype 9
			if (suve.getClassDetails() != null && suve.getClassDetails().getResponderType() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getClassDetails().getResponderType().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_responseequip_name 10
			if (suve.getClassDetails() != null && suve.getClassDetails().getResponseEquip() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getClassDetails().getResponseEquip().getName());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_responseequip_value 11
			if (suve.getClassDetails() != null && suve.getClassDetails().getResponseEquip() != null
					&& suve.getClassDetails().getResponseEquip().getValue() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getClassDetails().getResponseEquip().getValue().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_role 12
			if (suve.getClassDetails() != null && suve.getClassDetails().getRole() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getClassDetails().getRole().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_vehicletype_name 13
			if (suve.getClassDetails() != null && suve.getClassDetails().getVehicleType() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getClassDetails().getVehicleType().getName());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// cd_vehicletype_name 14
			if (suve.getClassDetails() != null && suve.getClassDetails().getVehicleType() != null
					&& suve.getClassDetails().getVehicleType().getValue() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getClassDetails().getVehicleType().getValue().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// vd_bumpers_front 15
			if (suve.getVehicleData() != null && suve.getVehicleData().getBumpers() != null
					&& suve.getVehicleData().getBumpers().getFront() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getVehicleData().getBumpers().getFront().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// vd_bumpers_rear 16
			if (suve.getVehicleData() != null && suve.getVehicleData().getBumpers() != null
					&& suve.getVehicleData().getBumpers().getRear() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getVehicleData().getBumpers().getRear().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// vd_height 17
			if (suve.getVehicleData() != null && suve.getVehicleData().getHeight() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getVehicleData().getHeight().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// vd_mass 18
			if (suve.getVehicleData() != null && suve.getVehicleData().getMass() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getVehicleData().getMass().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// vd_trailerweight 19
			if (suve.getVehicleData() != null && suve.getVehicleData().getTrailerWeight() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getVehicleData().getTrailerWeight().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_friction 20
			if (suve.getWeatherReport() != null && suve.getWeatherReport().getFriction() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getWeatherReport().getFriction().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_israining 21
			if (suve.getWeatherReport() != null && suve.getWeatherReport().getIsRaining() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getWeatherReport().getIsRaining().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_precipsituation 22
			if (suve.getWeatherReport() != null && suve.getWeatherReport().getPrecipSituation() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getWeatherReport().getPrecipSituation().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_rainrate 23
			if (suve.getWeatherReport() != null && suve.getWeatherReport().getRainRate() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getWeatherReport().getRainRate().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_roadfriction 24
			if (suve.getWeatherReport() != null && suve.getWeatherReport().getRoadFriction() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getWeatherReport().getRoadFriction().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wr_solarradiation 25
			if (suve.getWeatherReport() != null && suve.getWeatherReport().getSolarRadiation() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getWeatherReport().getSolarRadiation().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_airpressure 26
			if (suve.getWeatherProbe() != null && suve.getWeatherProbe().getAirPressure() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getWeatherProbe().getAirPressure().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_airtemp 27
			if (suve.getWeatherProbe() != null && suve.getWeatherProbe().getAirTemp() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getWeatherProbe().getAirTemp().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_rainrates_ratefront 28
			if (suve.getWeatherProbe() != null && suve.getWeatherProbe().getRainRates() != null
					&& suve.getWeatherProbe().getRainRates().getRateFront() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getWeatherProbe().getRainRates().getRateFront().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_rainrates_raterear 29
			if (suve.getWeatherProbe() != null && suve.getWeatherProbe().getRainRates() != null
					&& suve.getWeatherProbe().getRainRates().getRateRear() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getWeatherProbe().getRainRates().getRateRear().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_rainrates_statusfront 30
			if (suve.getWeatherProbe() != null && suve.getWeatherProbe().getRainRates() != null
					&& suve.getWeatherProbe().getRainRates().getStatusFront() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getWeatherProbe().getRainRates().getStatusFront().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// wp_rainrates_statusrear 31
			if (suve.getWeatherProbe() != null && suve.getWeatherProbe().getRainRates() != null
					&& suve.getWeatherProbe().getRainRates().getStatusRear() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getWeatherProbe().getRainRates().getStatusRear().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_datetime 32
			if (suve.getObstacle() != null && suve.getObstacle().getDateTime() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getObstacle().getDateTime().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_description 33
			if (suve.getObstacle() != null && suve.getObstacle().getDescription() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getObstacle().getDescription().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_locationdetails_name 34
			if (suve.getObstacle() != null && suve.getObstacle().getLocationDetails() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getObstacle().getLocationDetails().getName());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_locationdetails_value 35
			if (suve.getObstacle() != null && suve.getObstacle().getLocationDetails() != null
					&& suve.getObstacle().getLocationDetails().getValue() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getObstacle().getLocationDetails().getValue().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_obdirect 36
			if (suve.getObstacle() != null && suve.getObstacle().getObDirect() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getObstacle().getObDirect().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_obdist 37
			if (suve.getObstacle() != null && suve.getObstacle().getObDist() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getObstacle().getObDist().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// ob_vertevent 38
			if (suve.getObstacle() != null && suve.getObstacle().getVertEvent() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getObstacle().getVertEvent().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// st_statusdetails 39
			if (suve.getStatus() != null && suve.getStatus().getStatusDetails() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getStatus().getStatusDetails().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// st_locationdetails_name 40
			if (suve.getStatus() != null && suve.getStatus().getLocationDetails() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getStatus().getLocationDetails().getName());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// st_locationdetails_value 41
			if (suve.getStatus() != null && suve.getStatus().getLocationDetails() != null
					&& suve.getStatus().getLocationDetails().getValue() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getStatus().getLocationDetails().getValue().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// sp_speedreports 42
			if (suve.getSpeedProfile() != null && suve.getSpeedProfile().getSpeedReports() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getSpeedProfile().getSpeedReports().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// rtcm_msgs 43
			if (suve.getTheRTCM() != null && suve.getTheRTCM().getMsgs() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getTheRTCM().getMsgs().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// rtcm_rtcmheader_offsetset_antoffsetx 44
			if (suve.getTheRTCM() != null && suve.getTheRTCM().getRtcmHeader() != null
					&& suve.getTheRTCM().getRtcmHeader().getOffsetSet() != null
					&& suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetX() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetX().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// rtcm_rtcmheader_offsetset_antoffsety 45
			if (suve.getTheRTCM() != null && suve.getTheRTCM().getRtcmHeader() != null
					&& suve.getTheRTCM().getRtcmHeader().getOffsetSet() != null
					&& suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetY() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetY().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// rtcm_rtcmheader_offsetset_antoffsetz 46
			if (suve.getTheRTCM() != null && suve.getTheRTCM().getRtcmHeader() != null
					&& suve.getTheRTCM().getRtcmHeader().getOffsetSet() != null
					&& suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetZ() != null)
				bsmSuvePreparedStatement.setString(fieldNum,
						suve.getTheRTCM().getRtcmHeader().getOffsetSet().getAntOffsetZ().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// rtcm_rtcmheader_status 47
			if (suve.getTheRTCM() != null && suve.getTheRTCM().getRtcmHeader() != null
					&& suve.getTheRTCM().getRtcmHeader().getStatus() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getTheRTCM().getRtcmHeader().getStatus().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// regional 48
			if (suve.getRegional() != null)
				bsmSuvePreparedStatement.setString(fieldNum, suve.getRegional().toString());
			else
				bsmSuvePreparedStatement.setString(fieldNum, null);
			fieldNum++;

			// execute insert statement
			Long bsmPart2SuveId = executeAndLog(bsmSuvePreparedStatement, "bsmPart2SuveId");
			return bsmPart2SuveId;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (bsmSuvePreparedStatement != null)
					bsmSuvePreparedStatement.close();
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