package com.trihydro.service.timtype;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.service.helpers.SQLNullHandler;
import com.trihydro.service.model.TimType;
import com.trihydro.service.CvDataLoggerLibrary;

public class TimTypeService extends CvDataLoggerLibrary { 

    public static List<TimType> selectAll(Connection connection) {
    	List<TimType> timTypes = new ArrayList<TimType>();
		
		try {
			// build SQL statement
				Statement statement = connection.createStatement();
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
