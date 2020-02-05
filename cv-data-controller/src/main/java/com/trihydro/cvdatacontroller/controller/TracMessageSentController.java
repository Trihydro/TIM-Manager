package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.cvdatacontroller.tables.TracMessageOracleTables;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.TracMessageSent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("trac-message")
public class TracMessageSentController extends BaseController {

	private TracMessageOracleTables tracMessageOracleTables;
	private SQLNullHandler sqlNullHandler;

	@Autowired
	public void InjectDependencies(TracMessageOracleTables _tracMessageOracleTables, SQLNullHandler _sqlNullHandler) {
		tracMessageOracleTables = _tracMessageOracleTables;
		sqlNullHandler = _sqlNullHandler;
	}

	@RequestMapping(value = "/packet-ids", method = RequestMethod.GET, headers = "Accept=application/json")
	public List<String> SelectPacketIds() {

		List<String> packet_ids = new ArrayList<String>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = GetConnectionPool();
			statement = connection.createStatement();

			// build SQL statement
			rs = statement.executeQuery("select PACKET_ID from TRAC_MESSAGE_SENT");
			// get packet_id values
			while (rs.next()) {
				packet_ids.add(rs.getString("packet_id"));
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
		return packet_ids;
	}

	@RequestMapping(value = "/add-trac-message-sent", method = RequestMethod.POST, headers = "Accept=application/json")
	public Long InsertTracMessageSent(@RequestBody TracMessageSent tracMessageSent) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {
			connection = GetConnectionPool();
			String insertQueryStatement = tracMessageOracleTables.buildInsertQueryStatement("TRAC_MESSAGE_SENT",
					tracMessageOracleTables.getTracMessageSentTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement,
					new String[] { "trac_message_sent_id" });
			int fieldNum = 1;

			for (String col : tracMessageOracleTables.getTracMessageSentTable()) {
				if (col.equals("trac_message_type_id"))
					sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum,
							tracMessageSent.getTracMessageTypeId());
				else if (col.equals("date_time_sent"))
					sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, tracMessageSent.getDateTimeSent());
				else if (col.equals("message_text"))
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, tracMessageSent.getMessageText());
				else if (col.equals("packet_id"))
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, tracMessageSent.getPacketId());
				else if (col.equals("rest_response_code"))
					sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, tracMessageSent.getRestResponseCode());
				else if (col.equals("rest_response_message"))
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum,
							tracMessageSent.getRestResponseMessage());
				else if (col.equals("message_sent"))
					sqlNullHandler.setIntegerFromBool(preparedStatement, fieldNum, tracMessageSent.isMessageSent());
				else if (col.equals("email_sent"))
					sqlNullHandler.setIntegerFromBool(preparedStatement, fieldNum, tracMessageSent.isEmailSent());
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