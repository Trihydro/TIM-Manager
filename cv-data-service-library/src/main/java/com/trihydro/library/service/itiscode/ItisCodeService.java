package com.trihydro.library.service.itiscode;

import com.trihydro.library.model.ItisCode;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.library.service.CvDataLoggerLibrary;
import java.sql.ResultSet;

public class ItisCodeService extends CvDataLoggerLibrary {

	public static List<ItisCode> selectAll(Connection connection){
		List<ItisCode> itisCodes = new ArrayList<ItisCode>();
		try {
			// select all Itis Codes from ItisCode table   			
   		    Statement statement = connection.createStatement();
   			ResultSet rs = statement.executeQuery("select * from itis_code");
   			// convert to ItisCode objects   			
   			while (rs.next()) {   			
			    ItisCode itisCode = new ItisCode();
			    itisCode.setItisCodeId(rs.getInt("itis_code_id"));
			    itisCode.setItisCode(rs.getInt("itis_code"));
			    itisCode.setDescription(rs.getString("description"));    
			    itisCode.setCategoryId(rs.getInt("category_id"));
			    itisCodes.add(itisCode);
   			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return itisCodes;
	}

}