package com.trihydro.service.driveralert;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.service.model.DriverAlertType;

public class DriverAlertTypeService {

	public static List<DriverAlertType> selectAll(Connection connection) {
		
		List<DriverAlertType> driverAlertTypes = new ArrayList<DriverAlertType>();
		
		try {
			// build SQL statement
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery("select * from DRIVER_ALERT_TYPE");
				try {
					// convert to DriverAlertType objects   			
					while (rs.next()) {   			
						DriverAlertType driverAlertType = new DriverAlertType();
						driverAlertType.setDriverAlertTypeId(rs.getInt("DRIVER_ALERT_TYPE_ID"));
						driverAlertType.setShortName(rs.getString("SHORT_NAME"));	
						driverAlertType.setDescription(rs.getString("DESCRIPTION"));			   
						driverAlertTypes.add(driverAlertType);
					}
				}
				finally {
					try {
						rs.close();
					}
					catch (Exception e) {
						e.printStackTrace();
					}					
				}
			} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return driverAlertTypes;
	}
	 
}

