package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.trihydro.library.model.TracMessageSent;
import com.trihydro.library.tables.TracMessageOracleTables;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;

public class TracMessageSentService extends CvDataServiceLibrary {
    
    static PreparedStatement preparedStatement = null;
    
    public static List<TracMessageSent> selectAll(){

        List<TracMessageSent> tracMessagesSent = new ArrayList<TracMessageSent>();
		
		try (Statement statement = DbUtility.getConnection().createStatement()) {
			    // build SQL statement				
				ResultSet rs = statement.executeQuery("select * from TRAC_MESSAGE_SENT");
				// convert to TracMessageSent objects   			
				while (rs.next()) {   			
					TracMessageSent tracMessageSent = new TracMessageSent();
					tracMessageSent.setTracMessageSentId(rs.getInt("trac_message_sent_id"));
					tracMessageSent.setTracMessageTypeId(rs.getInt("trac_message_type_id"));	
                    tracMessageSent.setDateTimeSent(rs.getTimestamp("date_time_sent"));			
                    tracMessageSent.setMessageText(rs.getString("message_text"));			
                    tracMessageSent.setPacketId(rs.getString("packet_id"));				   
					tracMessagesSent.add(tracMessageSent);
				}
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
			return tracMessagesSent;
    }

    public static Long insertTracMessageSent(TracMessageSent tracMessageSent){
        
		try {
			
			TracMessageOracleTables tracMessageOracleTables = new TracMessageOracleTables();
			String insertQueryStatement = tracMessageOracleTables.buildInsertQueryStatement("TRAC_MESSAGE_SENT", tracMessageOracleTables.getTracMessageSentTable());
		
			preparedStatement = DbUtility.getConnection().prepareStatement(insertQueryStatement, new String[] {"trac_message_sent_id"});
			int fieldNum = 1;
			for(String col: tracMessageOracleTables.getTracMessageSentTable()) {				
				if(col.equals("trac_message_type_id"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, tracMessageSent.getTracMessageTypeId());														
				else if(col.equals("date_time_sent"))
					SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, tracMessageSent.getDateTimeSent());
				else if(col.equals("message_text"))			
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, tracMessageSent.getMessageText());														
				else if(col.equals("packet_id"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, tracMessageSent.getPacketId().toString());				
				fieldNum++;
			}			
			// execute insert statement
			Long tracMessageSentId = log(preparedStatement, "tracMessageSentId");
			return tracMessageSentId;
		} 
		catch (SQLException e) {
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
