package com.trihydro.library.service;

import java.sql.Connection;
import us.dot.its.jpo.ode.plugin.j2735.J2735TravelerInformationMessage;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import com.trihydro.library.tables.TimOracleTables;

public class NodeXYService extends CvDataServiceLibrary {

    public static Long insertNodeXY(J2735TravelerInformationMessage.NodeXY nodeXY) { 
        
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            
            connection = DbUtility.getConnectionPool();
			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("node_xy", TimOracleTables.getNodeXYTable());			
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"node_xy_id"});
            int fieldNum = 1;

			for(String col: TimOracleTables.getNodeXYTable()) {
                if(col.equals("DELTA"))
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, nodeXY.getDelta());	
                else if(col.equals("NODE_LAT"))
                    SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, nodeXY.getNodeLat());	
                else if(col.equals("NODE_LONG"))
                    SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, nodeXY.getNodeLong());	
                else if(col.equals("X"))
                    SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, nodeXY.getX());	
                else if(col.equals("Y"))
                    SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, nodeXY.getY());	
                else if(col.equals("ATTRIBUTES_DWIDTH"))
                    if(nodeXY.getAttributes() != null)
                        SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, nodeXY.getAttributes().getdWidth());
                    else	
                        preparedStatement.setString(fieldNum, null);		
                else if(col.equals("ATTRIBUTES_DELEVATION"))
                    if(nodeXY.getAttributes() != null)
                        SQLNullHandler.setBigDecimalOrNull(preparedStatement, fieldNum, nodeXY.getAttributes().getdElevation());	
                    else	
                        preparedStatement.setString(fieldNum, null);
				fieldNum++;
			}			
			Long nodeXYId = log(preparedStatement, "nodexy");		          
			return nodeXYId;
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

