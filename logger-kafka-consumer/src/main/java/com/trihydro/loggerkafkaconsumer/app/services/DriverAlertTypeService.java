package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.DriverAlertType;

import org.springframework.stereotype.Component;

@Component
public class DriverAlertTypeService extends BaseService {

    public List<DriverAlertType> getDriverAlertTypes() {

		List<DriverAlertType> driverAlertTypes = new ArrayList<DriverAlertType>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = GetConnectionPool();
			statement = connection.createStatement();

			// build SQL statement
			rs = statement.executeQuery("select * from DRIVER_ALERT_TYPE");

			// convert to DriverAlertType objects
			while (rs.next()) {
				DriverAlertType driverAlertType = new DriverAlertType();
				driverAlertType.setDriverAlertTypeId(rs.getInt("DRIVER_ALERT_TYPE_ID"));
				driverAlertType.setShortName(rs.getString("SHORT_NAME"));
				driverAlertType.setDescription(rs.getString("DESCRIPTION"));
				driverAlertTypes.add(driverAlertType);
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
		return driverAlertTypes;
	}
}