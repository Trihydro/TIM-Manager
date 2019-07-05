package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import com.trihydro.library.tables.TimOracleTables;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;

public class DataFrameService extends CvDataServiceLibrary {

	public static Long insertDataFrame(Long timID, DataFrame dFrame) {

		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {

			connection = DbUtility.getConnectionPool();
			String insertQueryStatement = TimOracleTables.buildInsertQueryStatement("data_frame",
					TimOracleTables.getDataFrameTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "data_frame_id" });
			int fieldNum = 1;

			for (String col : TimOracleTables.getDataFrameTable()) {
				if (col.equals("TIM_ID"))
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, timID);
				else if (col.equals("CONTENT"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, dFrame.getContent());
				else if (col.equals("DURATION_TIME"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, dFrame.getDurationTime());
				else if (col.equals("FRAME_TYPE"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, dFrame.getFrameType().ordinal());
				// else if (col.equals("MSG_ID"))
				// us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.MsgId
				// dFrame.getMsgId();// MsgId
				else if (col.equals("PRIORITY"))
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, dFrame.getPriority());
				else if (col.equals("SSP_LOCATION_RIGHTS"))
					SQLNullHandler.setShortOrNull(preparedStatement, fieldNum, dFrame.getSspLocationRights());
				else if (col.equals("SSP_MSG_TYPES"))
					SQLNullHandler.setShortOrNull(preparedStatement, fieldNum, dFrame.getSspMsgTypes());
				else if (col.equals("SSP_MSG_CONTENT"))
					SQLNullHandler.setShortOrNull(preparedStatement, fieldNum, dFrame.getSspMsgContent());
				else if (col.equals("START_DATE_TIME"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, dFrame.getStartDateTime());
				else if (col.equals("URL"))
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, dFrame.getUrl());
				fieldNum++;
			}

			Long dataFrameId = log(preparedStatement, "dataframe");
			return dataFrameId;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (preparedStatement != null)
					preparedStatement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return new Long(0);
	}
}
