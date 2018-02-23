package com.trihydro.library.service.tim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataLoggerLibrary;
import com.trihydro.library.service.helpers.SQLNullHandler;
import java.sql.SQLException;
import com.trihydro.library.service.tables.TimOracleTables;
import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;

public class RegionLogger extends CvDataLoggerLibrary {

	static PreparedStatement preparedStatement = null;

    public static Long insertRegion(Long dataFrameId, Long pathId, OdePosition3D anchor, Connection connection) { 
        try {
			TimOracleTables timOracleTables = new TimOracleTables();
			String insertQueryStatement = timOracleTables.buildInsertQueryStatement("region", timOracleTables.getRegionTable());			
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"region_id"});
			int fieldNum = 1;
			for(String col: timOracleTables.getRegionTable()) {
                if(col.equals("DATA_FRAME_ID"))
                    SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, dataFrameId);	
                else if(col.equals("PATH_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);	
				else if(col.equals("ANCHOR_LAT"))
					SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, anchor.getLatitude());														             												
				else if(col.equals("ANCHOR_LONG"))
					SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, anchor.getLongitude());														             																	
				fieldNum++;
			}			
			// execute insert statement
			Long regionId = log(preparedStatement, "regionID");		 		
			return regionId;
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

