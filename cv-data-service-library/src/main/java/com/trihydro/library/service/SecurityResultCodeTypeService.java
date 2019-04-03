package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.model.SecurityResultCodeType;

public class SecurityResultCodeTypeService extends CvDataServiceLibrary {

	static PreparedStatement preparedStatement = null;

	public static List<SecurityResultCodeType> getSecurityResultCodeTypes() {

		SecurityResultCodeType securityResultCodeType = null;
		List<SecurityResultCodeType> securityResultCodeTypes = new ArrayList<SecurityResultCodeType>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {

			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();
			rs = statement.executeQuery("select * from SECURITY_RESULT_CODE_TYPE");

			// convert to ActiveTim object
			while (rs.next()) {
				securityResultCodeType = new SecurityResultCodeType();
				securityResultCodeType.setSecurityResultCodeTypeId(rs.getInt("SECURITY_RESULT_CODE_TYPE_ID"));
				securityResultCodeType.setSecurityResultCodeType(rs.getString("SECURITY_RESULT_CODE_TYPE"));
				securityResultCodeTypes.add(securityResultCodeType);
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

		return securityResultCodeTypes;
	}
}
