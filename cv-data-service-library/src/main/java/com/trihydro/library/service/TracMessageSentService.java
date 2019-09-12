package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.model.TracMessageSent;
import com.trihydro.library.tables.TracMessageOracleTables;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;

public class TracMessageSentService extends CvDataServiceLibrary {

	public static List<TracMessageSent> selectAll() {

		List<TracMessageSent> tracMessagesSent = new ArrayList<TracMessageSent>();

		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			// build SQL statement
			rs = statement.executeQuery("select * from TRAC_MESSAGE_SENT");
			// convert to TracMessageSent objects
			while (rs.next()) {
				TracMessageSent tracMessageSent = new TracMessageSent();
				tracMessageSent.setTracMessageSentId(rs.getInt("trac_message_sent_id"));
				tracMessageSent.setTracMessageTypeId(rs.getInt("trac_message_type_id"));
				tracMessageSent.setDateTimeSent(rs.getTimestamp("date_time_sent"));
				tracMessageSent.setMessageText(rs.getString("message_text"));
				tracMessageSent.setPacketId(rs.getString("packet_id"));

				int responseCode = rs.getInt("rest_response_code");
				// rs.getInt returns 0 if null is found so only set if non-zero
				if (responseCode != 0) {
					tracMessageSent.setRestResponseCode(responseCode);
				}
				tracMessageSent.setRestResponseMessage(rs.getString("rest_response_message"));
				tracMessageSent.setMessageSent(rs.getBoolean("message_sent"));
				tracMessageSent.setEmailSent(rs.getBoolean("email_sent"));
				tracMessagesSent.add(tracMessageSent);
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
		return tracMessagesSent;
	}

	public static Long insertTracMessageSent(TracMessageSent tracMessageSent) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = DbUtility.getConnectionPool();
			TracMessageOracleTables tracMessageOracleTables = new TracMessageOracleTables();
			String insertQueryStatement = TracMessageOracleTables.buildInsertQueryStatement("TRAC_MESSAGE_SENT",
					tracMessageOracleTables.getTracMessageSentTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement,
					new String[] { "trac_message_sent_id" });
			int fieldNum = 1;

			for (String col : tracMessageOracleTables.getTracMessageSentTable()) {
				if (col.equals("trac_message_type_id"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
							tracMessageSent.getTracMessageTypeId());
				else if (col.equals("date_time_sent"))
					SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, tracMessageSent.getDateTimeSent());
				else if (col.equals("message_text"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, tracMessageSent.getMessageText());
				else if (col.equals("packet_id"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							tracMessageSent.getPacketId().toString());
				else if (col.equals("rest_response_code"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, tracMessageSent.getRestResponseCode());
				else if (col.equals("rest_response_message"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum,
							tracMessageSent.getRestResponseMessage());
				else if (col.equals("message_sent"))
					SQLNullHandler.setIntegerFromBool(preparedStatement, fieldNum, tracMessageSent.isMessageSent());
				else if (col.equals("email_sent"))
					SQLNullHandler.setIntegerFromBool(preparedStatement, fieldNum, tracMessageSent.isEmailSent());
				fieldNum++;
			}
			// execute insert statement
			Long tracMessageSentId = log(preparedStatement, "tracMessageSentId");
			return tracMessageSentId;
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
}
