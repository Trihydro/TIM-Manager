package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import com.trihydro.library.tables.TimOracleTables;

public class PathNodeXYService extends CvDataServiceLibrary {

	static PreparedStatement preparedStatement = null;

    public static Long insertPathNodeXY(Long nodeXYId, Long pathId) { 
        try {
			TimOracleTables timOracleTables = new TimOracleTables();
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("path_node_xy", timOracleTables.getPathNodeXYTable());			
			preparedStatement = DbUtility.getConnection().prepareStatement(insertQueryStatement, new String[] {"path_node_xy_id"});
			int fieldNum = 1;
			for(String col: timOracleTables.getPathNodeXYTable()) {
                if(col.equals("NODE_XY_ID"))
                    SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, nodeXYId);	
                else if(col.equals("PATH_ID"))
                    SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);														             												
				fieldNum++;
			}			
			// execute insert statement
			Long pathNodeXYId = log(preparedStatement, "pathnodexyid");				
			return pathNodeXYId;

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

