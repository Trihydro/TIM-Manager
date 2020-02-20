package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.BsmOracleTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import us.dot.its.jpo.ode.plugin.j2735.J2735BsmPart2Content;
import us.dot.its.jpo.ode.plugin.j2735.J2735VehicleSafetyExtensions;

@Component
public class BsmPart2VseService extends BaseService {

    private BsmOracleTables bsmOracleTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(BsmOracleTables _bsmOracleTables, SQLNullHandler _sqlNullHandler) {
        bsmOracleTables = _bsmOracleTables;
        sqlNullHandler = _sqlNullHandler;
    }

	public Long insertBSMPart2VSE(J2735BsmPart2Content part2Content, J2735VehicleSafetyExtensions vse,
			Long bsmCoreDataId) {

		String bsmVseInsertQueryStatement = bsmOracleTables.buildInsertQueryStatement("bsm_part2_vse",
        bsmOracleTables.getBsmPart2VseTable());
		PreparedStatement bsmVsePreparedStatement = null;
		Connection connection = null;

		try {
			connection = GetConnectionPool();
			bsmVsePreparedStatement = connection.prepareStatement(bsmVseInsertQueryStatement,
					new String[] { "bsm_part2_vse_id" });

			int fieldNum = 1;

			for (String col : bsmOracleTables.getBsmPart2VseTable()) {
				if (col.equals("BSM_CORE_DATA_ID")) {
					bsmVsePreparedStatement.setLong(fieldNum, bsmCoreDataId);
				} else if (col.equals("ID")) {
					bsmVsePreparedStatement.setString(fieldNum, part2Content.getId().name());
				} else if (col.equals("EVENTS")) {
					if (vse.getEvents() != null)
						bsmVsePreparedStatement.setString(fieldNum, vse.getEvents().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_LAT")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getPosition().getLatitude() != null)
						bsmVsePreparedStatement.setBigDecimal(fieldNum,
								vse.getPathHistory().getInitialPosition().getPosition().getLatitude());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_LONG")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getPosition().getLongitude() != null)
						bsmVsePreparedStatement.setBigDecimal(fieldNum,
								vse.getPathHistory().getInitialPosition().getPosition().getLongitude());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_ELEV")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getPosition().getElevation() != null)
						bsmVsePreparedStatement.setBigDecimal(fieldNum,
								vse.getPathHistory().getInitialPosition().getPosition().getElevation());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_HEADING")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getHeading() != null)
						bsmVsePreparedStatement.setBigDecimal(fieldNum,
								vse.getPathHistory().getInitialPosition().getHeading());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_POSACCRCY_SEMIMAJ")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getPosAccuracy().getSemiMajor() != null)
						bsmVsePreparedStatement.setBigDecimal(fieldNum,
								vse.getPathHistory().getInitialPosition().getPosAccuracy().getSemiMajor());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_POSACCRCY_SEMIMIN")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getPosAccuracy().getSemiMinor() != null)
						bsmVsePreparedStatement.setBigDecimal(fieldNum,
								vse.getPathHistory().getInitialPosition().getPosAccuracy().getSemiMinor());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_POSACCRCY_ORIEN")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getPosAccuracy().getOrientation() != null)
						bsmVsePreparedStatement.setBigDecimal(fieldNum,
								vse.getPathHistory().getInitialPosition().getPosAccuracy().getOrientation());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_POSCONFIDENCE_POS")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getPosConfidence().getPos() != null)
						bsmVsePreparedStatement.setString(fieldNum,
								vse.getPathHistory().getInitialPosition().getPosConfidence().getPos().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_POSCONFIDENCE_ELEV")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getPosConfidence().getElevation() != null)
						bsmVsePreparedStatement.setString(fieldNum,
								vse.getPathHistory().getInitialPosition().getPosConfidence().getElevation().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_SPEED")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getSpeed().getSpeed() != null)
						bsmVsePreparedStatement.setBigDecimal(fieldNum,
								vse.getPathHistory().getInitialPosition().getSpeed().getSpeed());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_TRANSMISSION")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getSpeed().getTransmisson() != null)
						bsmVsePreparedStatement.setString(fieldNum,
								vse.getPathHistory().getInitialPosition().getSpeed().getTransmisson().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_SPEEDCONF_HEADING")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getSpeedConfidence().getHeading() != null)
						bsmVsePreparedStatement.setString(fieldNum,
								vse.getPathHistory().getInitialPosition().getSpeedConfidence().getHeading().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_SPEEDCONF_SPEED")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getSpeedConfidence().getSpeed() != null)
						bsmVsePreparedStatement.setString(fieldNum,
								vse.getPathHistory().getInitialPosition().getSpeedConfidence().getSpeed().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_SPEEDCONF_THROTTLE")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getSpeedConfidence().getThrottle() != null)
						bsmVsePreparedStatement.setString(fieldNum, vse.getPathHistory().getInitialPosition()
								.getSpeedConfidence().getThrottle().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_TIMECONF")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getTimeConfidence() != null)
						bsmVsePreparedStatement.setString(fieldNum,
								vse.getPathHistory().getInitialPosition().getTimeConfidence().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_UTCTIME_DAY")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getUtcTime().getDay() != null)
						bsmVsePreparedStatement.setInt(fieldNum,
								vse.getPathHistory().getInitialPosition().getUtcTime().getDay());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_UTCTIME_HOUR")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getUtcTime().getHour() != null)
						bsmVsePreparedStatement.setInt(fieldNum,
								vse.getPathHistory().getInitialPosition().getUtcTime().getHour());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_UTCTIME_MINUTE")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getUtcTime().getMinute() != null)
						bsmVsePreparedStatement.setInt(fieldNum,
								vse.getPathHistory().getInitialPosition().getUtcTime().getMinute());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_UTCTIME_MONTH")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getUtcTime().getMonth() != null)
						bsmVsePreparedStatement.setInt(fieldNum,
								vse.getPathHistory().getInitialPosition().getUtcTime().getMonth());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_UTCTIME_OFFSET")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getUtcTime().getOffset() != null)
						bsmVsePreparedStatement.setInt(fieldNum,
								vse.getPathHistory().getInitialPosition().getUtcTime().getOffset());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_UTCTIME_SECOND")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getUtcTime().getSecond() != null)
						bsmVsePreparedStatement.setInt(fieldNum,
								vse.getPathHistory().getInitialPosition().getUtcTime().getSecond());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_INITPOS_UTCTIME_YEAR")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getInitialPosition() != null
							&& vse.getPathHistory().getInitialPosition().getUtcTime().getYear() != null)
						bsmVsePreparedStatement.setInt(fieldNum,
								vse.getPathHistory().getInitialPosition().getUtcTime().getYear());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_CURRGNSSSTATUS")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getCurrGNSSstatus() != null)
						bsmVsePreparedStatement.setString(fieldNum,
								vse.getPathHistory().getCurrGNSSstatus().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PH_CRUMBDATA")) {
					if (vse.getPathHistory() != null && vse.getPathHistory().getCrumbData() != null)
						bsmVsePreparedStatement.setString(fieldNum, vse.getPathHistory().getCrumbData().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PP_CONFIDENCE")) {
					if (vse.getPathPrediction() != null && vse.getPathPrediction().getConfidence() != null)
						bsmVsePreparedStatement.setBigDecimal(fieldNum, vse.getPathPrediction().getConfidence());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("PP_RADIUSOFCURVE")) {
					if (vse.getPathPrediction() != null && vse.getPathPrediction().getRadiusOfCurve() != null)
						bsmVsePreparedStatement.setBigDecimal(fieldNum, vse.getPathPrediction().getRadiusOfCurve());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				} else if (col.equals("LIGHTS")) {
					if (vse.getLights() != null)
						bsmVsePreparedStatement.setString(fieldNum, vse.getLights().toString());
					else
						bsmVsePreparedStatement.setString(fieldNum, null);
				}
				fieldNum++;
			}

			// execute insert statement
			Long bsmPart2VseId = executeAndLog(bsmVsePreparedStatement, "bsmPart2VseId");
			return bsmPart2VseId;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (bsmVsePreparedStatement != null)
					bsmVsePreparedStatement.close();
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
