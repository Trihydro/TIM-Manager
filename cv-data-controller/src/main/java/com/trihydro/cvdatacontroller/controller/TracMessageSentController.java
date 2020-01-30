package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("trac-message")
public class TracMessageSentController extends BaseController{

    @RequestMapping(value = "/packet-ids", method = RequestMethod.GET, headers = "Accept=application/json")
    public List<String> selectPacketIds() {

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

}