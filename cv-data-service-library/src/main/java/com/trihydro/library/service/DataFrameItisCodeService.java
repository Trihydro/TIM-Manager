package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.DataFrameItisCode;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.tables.TimOracleTables;

import org.apache.commons.lang3.StringUtils;

public class DataFrameItisCodeService extends CvDataServiceLibrary {

	public static List<DataFrameItisCode> selectAll() {
		
		List<DataFrameItisCode> dataFrameItisCodes = new ArrayList<DataFrameItisCode>();
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

		try {
			connection = DbUtility.getConnectionPool();
			statement = connection.createStatement();

			// build SQL statement							
			rs = statement.executeQuery("select * from DATA_FRAME_ITIS_CODE");
	
			// convert to DriverAlertItisCode objects   			
			while (rs.next()) {   			
				DataFrameItisCode dataFrameItisCode = new DataFrameItisCode();
				dataFrameItisCode.setDataFrameId(rs.getInt("DATA_FRAME_ID"));
				dataFrameItisCode.setItisCodeId(rs.getInt("ITIS_CODE_ID"));									   
				dataFrameItisCodes.add(dataFrameItisCode);
			}
		}						
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {			
			try {
				// close prepared statement
				if(statement != null)
					statement.close();
				// return connection back to pool
				if(connection != null)
					connection.close();
				// close result set
				if(rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return dataFrameItisCodes;
    }

    public static Long insertDataFrameItisCode(Long dataFrameId, String itis) { 

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {

			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("data_frame_itis_code", TimOracleTables.getDataFrameItisCodeTable());			
			connection = DbUtility.getConnectionPool();
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"data_frame_itis_code_id"});
			int fieldNum = 1;

			for(String col: TimOracleTables.getDataFrameItisCodeTable()) {
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
				// close prepared statement
				if(preparedStatement != null)
					preparedStatement.close();
				// return connection back to pool
				if(connection != null)
					connection.close();				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return new Long(0);
	}
}

