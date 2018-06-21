package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import com.trihydro.library.tables.TimOracleTables;

import org.apache.commons.lang3.StringUtils;

public class DataFrameItisCodeService extends CvDataServiceLibrary {

	static PreparedStatement preparedStatement = null;

    public static Long insertDataFrameItisCode(Long dataFrameId, String itis) { 
		try {
			TimOracleTables timOracleTables = new TimOracleTables();
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("data_frame_itis_code", timOracleTables.getDataFrameItisCodeTable());			
			preparedStatement = DbUtility.getConnection().prepareStatement(insertQueryStatement, new String[] {"data_frame_itis_code_id"});
			int fieldNum = 1;
			for(String col: timOracleTables.getDataFrameItisCodeTable()) {
				if(col.equals("ITIS_CODE_ID")) {
					if(StringUtils.isNumeric(itis))
						SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, Long.parseLong(itis));
					else
						SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, null);
				}
				else if(col.equals("TEXT")) {
					if(!StringUtils.isNumeric(itis))
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, itis);
					else
						SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, null);
				}                  
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

