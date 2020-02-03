package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.TimInsertModel;
import com.trihydro.library.model.WydotOdeTravelerInformationMessage;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.model.OdeLogMetadata.RecordType;
import us.dot.its.jpo.ode.model.OdeLogMetadata.SecurityResultCode;
import us.dot.its.jpo.ode.model.OdeMsgMetadata;
import us.dot.its.jpo.ode.model.ReceivedMessageDetails;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage;

public class TimService extends CvDataServiceLibrary {

	/**
	 * Insert a new TIM record to the database. In the case that the TIM already
	 * exists, this function returns the existing tim_id. If the TIM exists and this
	 * function is passed a satRecordId, it will update the database record to
	 * include this satRecordId
	 * 
	 * @param odeTimMetadata
	 * @param receivedMessageDetails
	 * @param j2735TravelerInformationMessage
	 * @param recordType
	 * @param logFileName
	 * @param securityResultCode
	 * @param satRecordId
	 * @param regionName
	 * @return
	 */
	public static Long insertTim(OdeMsgMetadata odeTimMetadata, ReceivedMessageDetails receivedMessageDetails,
			OdeTravelerInformationMessage j2735TravelerInformationMessage, RecordType recordType, String logFileName,
			SecurityResultCode securityResultCode, String satRecordId, String regionName) {
		String url = String.format("/%s/add-tim", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		TimInsertModel tim = new TimInsertModel();
		tim.setOdeTimMetadata(odeTimMetadata);
		tim.setReceivedMessageDetails(receivedMessageDetails);
		tim.setJ2735TravelerInformationMessage(j2735TravelerInformationMessage);
		tim.setRecordType(recordType);
		tim.setLogFileName(logFileName);
		tim.setSecurityResultCode(securityResultCode);
		tim.setSatRecordId(satRecordId);
		tim.setRegionName(regionName);
		HttpEntity<TimInsertModel> entity = new HttpEntity<TimInsertModel>(null, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
	}

	public static boolean updateTimSatRecordId(Long timId, String satRecordId) {
		PreparedStatement preparedStatement = null;
		Connection connection = null;

		try {
			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement("update tim set sat_record_id = ? where tim_id = ?");
			preparedStatement.setString(1, satRecordId);
			preparedStatement.setLong(2, timId);
			return updateOrDelete(preparedStatement);
		} catch (Exception ex) {
			return false;
		} finally {
			try {
				if (preparedStatement != null)
					preparedStatement.close();

				if (connection != null)
					connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Long getTimId(String packetId, Timestamp timeStamp) {
		ResultSet rs = null;
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		Long id = null;

		try {
			connection = DbUtility.getConnectionPool();
			preparedStatement = connection
					.prepareStatement("select tim_id from tim where packet_id = ? and time_stamp = ?");
			preparedStatement.setString(1, packetId);
			preparedStatement.setTimestamp(2, timeStamp);

			rs = preparedStatement.executeQuery();
			if (rs.next()) {
				id = rs.getLong("tim_id");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (preparedStatement != null)
					preparedStatement.close();

				if (connection != null)
					connection.close();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		}
		return id;
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
