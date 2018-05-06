package com.trihydro.library.service;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.model.ItisCode;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;
import com.trihydro.library.service.CvDataServiceLibrary;

import java.sql.ResultSet;

public class ItisCodeService extends CvDataServiceLibrary {

	public static List<ItisCode> selectAll(){
		List<ItisCode> itisCodes = new ArrayList<ItisCode>();
		try (Statement statement = DbUtility.getConnection().createStatement()) {
			// select all Itis Codes from ItisCode table   			   		    
			ResultSet rs = statement.executeQuery("select * from itis_code");
			try {
				// convert to ItisCode objects   			
				while (rs.next()) {   			
					ItisCode itisCode = new ItisCode();
					itisCode.setItisCodeId(rs.getInt("itis_code_id"));
					itisCode.setItisCode(rs.getInt("itis_code"));
					itisCode.setDescription(rs.getString("description").toLowerCase());    
					itisCode.setCategoryId(rs.getInt("category_id"));
					itisCodes.add(itisCode);
				}
			}
			finally {
				try { rs.close(); } catch (Exception ignore) { }
			}
  		} 
  		catch (SQLException e) {
   			e.printStackTrace();
  		}
  		return itisCodes;
	}

}