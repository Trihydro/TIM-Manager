package com.trihydro.service.tim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.service.CvDataLoggerLibrary;
import com.trihydro.service.helpers.SQLNullHandler;
import java.sql.SQLException;
import com.trihydro.service.tables.TimOracleTables;

public class DataFrameLogger extends CvDataLoggerLibrary {

	static PreparedStatement preparedStatement = null;

    public static Long insertDataFrame(Long timID, Connection connection) { 
		try {
			TimOracleTables timOracleTables = new TimOracleTables();
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("data_frame", timOracleTables.getDataFrameTable());			
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"data_frame_id"});
			int fieldNum = 1;
			for(String col: timOracleTables.getDataFrameTable()) {
				if(col.equals("TIM_ID")) 
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, timID);														
				fieldNum++;
			}			
			
			Long dataFrameId = log(preparedStatement, "dataframe");		 		
			return dataFrameId;
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

