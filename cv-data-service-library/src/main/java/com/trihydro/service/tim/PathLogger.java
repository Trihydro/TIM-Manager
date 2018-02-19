package com.trihydro.service.tim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.service.CvDataLoggerLibrary;
import com.trihydro.service.helpers.SQLNullHandler;
import java.sql.SQLException;
import com.trihydro.service.tables.TimOracleTables;

public class PathLogger extends CvDataLoggerLibrary {

	static PreparedStatement preparedStatement = null;

    public static Long insertPath(Connection connection) { 
        try {
			TimOracleTables timOracleTables = new TimOracleTables();
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("path", timOracleTables.getPathTable());			
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"path_id"});
			int fieldNum = 1;
			for(String col: timOracleTables.getPathTable()) {
				if(col.equals("SCALE")) 
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, 0);														
				fieldNum++;
			}			
			// execute insert statement
			Long pathId = log(preparedStatement, "pathId");
			return pathId;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {			
			try {
				preparedStatement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	   return new Long(0);
     }

}

