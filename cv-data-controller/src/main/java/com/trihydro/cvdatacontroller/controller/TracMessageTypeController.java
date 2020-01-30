package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.TracMessageType;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("trac-message-type")
public class TracMessageTypeController extends BaseController{
    @RequestMapping(value = "/GetAll", method = RequestMethod.GET, headers = "Accept=application/json")
    public List<TracMessageType> GetAll(){
        List<TracMessageType> tracMessagesType = new ArrayList<TracMessageType>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {

			connection = GetConnectionPool();
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