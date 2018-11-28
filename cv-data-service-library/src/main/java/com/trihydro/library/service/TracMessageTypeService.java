package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.TracMessageType;
import com.trihydro.library.service.CvDataServiceLibrary;

public class TracMessageTypeService extends CvDataServiceLibrary {

	static PreparedStatement preparedStatement = null;

	public static List<TracMessageType> selectAll() {

		List<TracMessageType> tracMessagesType = new ArrayList<TracMessageType>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {

			connection = DbUtility.getConnectionPool();
			// build SQL statement
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from TRAC_MESSAGE_TYPE");
			// convert to TracMessageSent objects
			while (rs.next()) {
				TracMessageType tracMessageType = new TracMessageType();
				tracMessageType.setTracMessageTypeId(rs.getInt("trac_message_type_id"));
				tracMessageType.setTracMessageType(rs.getString("trac_message_type"));
				tracMessageType.setTracMessageDescription(rs.getString("trac_message_description"));
				tracMessagesType.add(tracMessageType);
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
		return tracMessagesType;
	}

}
