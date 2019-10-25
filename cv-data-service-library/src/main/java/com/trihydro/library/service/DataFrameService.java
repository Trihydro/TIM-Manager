package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.trihydro.library.service.CvDataServiceLibrary;
import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
			String params = "";

			for (String col : TimOracleTables.getDataFrameTable()) {
				params += col + ":";
				if (col.equals("TIM_ID")) {
					params += timID;
					SQLNullHandler.setLongOrNull(preparedStatement, fieldNum, timID);
				} else if (col.equals("CONTENT")) {
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, dFrame.getContent());
					params += dFrame.getContent();
				} else if (col.equals("DURATION_TIME")) {
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, dFrame.getDurationTime());
					params += dFrame.getDurationTime();
				} else if (col.equals("FRAME_TYPE")) {
					Integer ordinal = null;
					if (dFrame.getFrameType() != null) {
						ordinal = dFrame.getFrameType().ordinal();
					}
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, ordinal);
					params += ordinal;
				}
				// else if (col.equals("MSG_ID")) {
				// //msg_id is a whole object...ignore it for now
				// // dFrame.getMsgId();
				// //
				// us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.MsgId
				// id;
				// }
				else if (col.equals("PRIORITY")) {
					SQLNullHandler.setIntegerOrNull(preparedStatement, fieldNum, dFrame.getPriority());
					params += dFrame.getPriority();
				} else if (col.equals("SSP_LOCATION_RIGHTS")) {
					SQLNullHandler.setShortOrNull(preparedStatement, fieldNum, dFrame.getSspLocationRights());
					params += dFrame.getSspLocationRights();
				} else if (col.equals("SSP_MSG_TYPES")) {
					SQLNullHandler.setShortOrNull(preparedStatement, fieldNum, dFrame.getSspMsgTypes());
					params += dFrame.getSspMsgTypes();
				} else if (col.equals("SSP_MSG_CONTENT")) {
					SQLNullHandler.setShortOrNull(preparedStatement, fieldNum, dFrame.getSspMsgContent());
					params += dFrame.getSspMsgContent();
				} else if (col.equals("START_DATE_TIME")) {
					Timestamp time = null;
					try {
						TimeZone tz = TimeZone.getTimeZone("UTC");
						DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no
																						// timezone offset
						df.setTimeZone(tz);
						Date dt = df.parse(dFrame.getStartDateTime());
						time = new Timestamp(dt.getTime());
					} catch (ParseException ex) {
						System.out.println("Unable to parse startdate: " + dFrame.getStartDateTime());
					}
					SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, time);
					params += time.toString();
				} else if (col.equals("URL")) {
					SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, dFrame.getUrl());
					params += dFrame.getUrl();
				}

				params += ";";
				fieldNum++;
			}

			System.out.println(
					"--------------- dataFrameService inserting: " + insertQueryStatement + " WITH PARAMS: " + params);

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

	public static String[] getItisCodesForDataFrameId(Integer dataFrameId) {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		List<String> itisCodes = new ArrayList<>();

		try {
			connection = DbUtility.getConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select distinct ic.itis_code";
			selectStatement += " from data_frame_itis_Code dfic inner join itis_code ic on dfic.itis_code_id = ic.itis_code_id";
			selectStatement += "where data_frame_id =  ";
			selectStatement += dataFrameId;

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				itisCodes.add(rs.getString("ITIS_CODE"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// close prepared statement
				if (statement != null)
					statement.close();
				// return connection back to pool
				if (connection != null)
					connection.close();
				// close result set
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return itisCodes.toArray(new String[itisCodes.size()]);
	}
}
