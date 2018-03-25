package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.TimType;
import com.trihydro.library.service.CvDataServiceLibrary;

public class TimTypeService extends CvDataServiceLibrary { 

    public static List<TimType> selectAll() {
    	List<TimType> timTypes = new ArrayList<TimType>();
		
		try {
			// build SQL statement
				ResultSet rs = statement.executeQuery("select * from TIM_TYPE");
				// convert to tim type objects   			
				while (rs.next()) {   			
					TimType timType = new TimType();
					timType.setTimTypeId(rs.getLong("TIM_TYPE_ID"));
					timType.setType(rs.getString("TYPE"));	
					timType.setDescription(rs.getString("DESCRIPTION"));			   
					timTypes.add(timType);					
				}
			} 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return timTypes;
    }
}
