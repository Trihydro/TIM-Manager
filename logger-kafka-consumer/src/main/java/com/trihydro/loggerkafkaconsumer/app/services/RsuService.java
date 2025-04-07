package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.trihydro.library.model.WydotRsu;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RsuService extends BaseService {

    public ArrayList<WydotRsu> getRsus() {

		ArrayList<WydotRsu> rsus = new ArrayList<WydotRsu>();
		Connection connection = null;
		ResultSet rs = null;
		Statement statement = null;

		try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();

			// select all RSUs from RSU table
			rs = statement.executeQuery(
					"select * from rsu inner join rsu_view on rsu.deviceid = rsu_view.deviceid order by milepost asc");

			while (rs.next()) {
				WydotRsu rsu = new WydotRsu();
				rsu.setRsuId(rs.getInt("RSU_ID"));
				rsu.setRsuTarget(rs.getString("IPV4_ADDRESS"));
				rsu.setLatitude(rs.getBigDecimal("LATITUDE"));
				rsu.setLongitude(rs.getBigDecimal("LONGITUDE"));
				rsu.setRoute(rs.getString("ROUTE"));
				rsu.setMilepost(rs.getDouble("MILEPOST"));
				rsus.add(rsu);
			}

		} catch (SQLException e) {
            log.error("Exception", e);
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
                log.error("Exception", e);
			}
		}

		return rsus;
	}
}