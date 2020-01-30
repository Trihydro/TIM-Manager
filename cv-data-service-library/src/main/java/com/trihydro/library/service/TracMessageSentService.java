package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.TracMessageSent;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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

	public static List<String> selectPacketIds() {
		String url = String.format("/%s/trac-message/packet-ids", CVRestUrl);
		ResponseEntity<String[]> response = RestTemplateProvider.GetRestTemplate().getForEntity(url, String[].class);
		return Arrays.asList(response.getBody());
	}

	public static Long insertTracMessageSent(TracMessageSent tracMessageSent) {
		String url = String.format("/%s/trac-message/add-trac-message-sent", CVRestUrl);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<TracMessageSent> entity = new HttpEntity<TracMessageSent>(tracMessageSent, headers);
		ResponseEntity<Long> response = RestTemplateProvider.GetRestTemplate().exchange(url, HttpMethod.POST, entity,
				Long.class);
		return response.getBody();
	}
}
