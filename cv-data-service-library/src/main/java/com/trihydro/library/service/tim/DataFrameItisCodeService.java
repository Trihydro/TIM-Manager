package com.trihydro.library.service.tim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataLoggerLibrary;
import com.trihydro.library.service.helpers.SQLNullHandler;
import java.sql.SQLException;
import com.trihydro.library.service.tables.TimOracleTables;

public class DataFrameItisCodeService extends CvDataLoggerLibrary {

	static PreparedStatement preparedStatement = null;

    public static Long insertDataFrameItisCode(Long dataFrameId, Long itisCodeId, Connection connection) { 
		try {
			TimOracleTables timOracleTables = new TimOracleTables();
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("data_frame_itis_code", timOracleTables.getDataFrameItisCodeTable());			
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"data_frame_itis_code_id"});
			int fieldNum = 1;
			for(String col: timOracleTables.getDataFrameItisCodeTable()) {
				if(col.equals("ITIS_CODE_ID")) 
                    SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, itisCodeId);	
                else if(col.equals("DATA_FRAME_ID")) 
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, dataFrameId);													
				fieldNum++;
			}			
			
			Long dataFrameItisCodeId = log(preparedStatement, "dataFrameItisCode");		 		
			return dataFrameItisCodeId;
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

