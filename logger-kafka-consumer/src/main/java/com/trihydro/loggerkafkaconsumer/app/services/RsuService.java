package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.trihydro.library.model.WydotRsu;

import org.springframework.stereotype.Component;

@Component
public class RsuService extends BaseService {

    public ArrayList<WydotRsu> getRsus() {

		ArrayList<WydotRsu> rsus = new ArrayList<WydotRsu>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = GetConnectionPool();
			statement = connection.createStatement();

			// select all RSUs from RSU table
			rs = statement.executeQuery(
					"select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid order by milepost asc");

			while (rs.next()) {
				WydotRsu rsu = new WydotRsu();
				rsu.setRsuId(rs.getInt("rsu_id"));
				rsu.setRsuTarget(rs.getString("ipv4_address"));
				rsu.setLatitude(rs.getDouble("latitude"));
				rsu.setLongitude(rs.getDouble("longitude"));
				rsu.setRoute(rs.getString("route"));
				rsu.setMilepost(rs.getDouble("milepost"));
				rsus.add(rsu);
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

		return rsus;
	}
}