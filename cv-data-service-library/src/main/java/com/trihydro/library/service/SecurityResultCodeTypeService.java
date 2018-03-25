package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.tables.TimOracleTables;
import com.trihydro.library.model.SecurityResultCodeType;;

public class SecurityResultCodeTypeService extends CvDataServiceLibrary {

	static PreparedStatement preparedStatement = null;
	
    public static List<SecurityResultCodeType> getSecurityResultCodeTypes(Connection connection){
		
		SecurityResultCodeType securityResultCodeType = null;
		List<SecurityResultCodeType> securityResultCodeTypes = new ArrayList<SecurityResultCodeType>();

		try {
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("select * from SECURITY_RESULT_CODE_TYPE");
			try {
				// convert to ActiveTim object  				
				while (rs.next()) {   	
					securityResultCodeType = new SecurityResultCodeType();		
					securityResultCodeType.setSecurityResultCodeTypeId(rs.getInt("SECURITY_RESULT_CODE_TYPE_ID"));
					securityResultCodeType.setSecurityResultCodeType(rs.getString("SECURITY_RESULT_CODE_TYPE"));						
					securityResultCodeTypes.add(securityResultCodeType);				
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

		return securityResultCodeTypes;
	}
}

