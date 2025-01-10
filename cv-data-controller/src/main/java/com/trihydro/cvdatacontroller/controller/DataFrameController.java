package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import springfox.documentation.annotations.ApiIgnore;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;

@CrossOrigin
@RestController
@RequestMapping("data-frame")
@ApiIgnore
public class DataFrameController extends BaseController {

	private TimDbTables timDbTables;
	private SQLNullHandler sqlNullHandler;

	@Autowired
	public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
		timDbTables = _timDbTables;
		sqlNullHandler = _sqlNullHandler;
	}

	@RequestMapping(method = RequestMethod.GET, produces = "application/json", value = "/itis-for-data-frame/{dataFrameId}")
	public ResponseEntity<String[]> GetItisCodesForDataFrameId(@PathVariable Integer dataFrameId) {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		List<String> itisCodes = new ArrayList<>();

		try {
			connection = dbInteractions.getConnectionPool();

			statement = connection.createStatement();

			String selectStatement = "select ic.itis_code, dfic.text";
			selectStatement += " from data_frame_itis_code dfic left join itis_code ic on dfic.itis_code_id = ic.itis_code_id";
			selectStatement += " where data_frame_id = ";
			selectStatement += dataFrameId;
			selectStatement += " order by dfic.position asc";

			rs = statement.executeQuery(selectStatement);

			// convert to ActiveTim object
			while (rs.next()) {
				var code = rs.getString("ITIS_CODE");
				if (code == null) {
					code = rs.getString("TEXT");
				}
				itisCodes.add(code);
			}
			return ResponseEntity.ok(itisCodes.toArray(new String[itisCodes.size()]));
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(itisCodes.toArray(new String[itisCodes.size()]));
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
	}

	@RequestMapping(value = "/add-data-frame/{timId}", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<Long> AddDataFrame(@RequestBody DataFrame dFrame, @PathVariable Long timId) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;

		try {

			connection = dbInteractions.getConnectionPool();
			String insertQueryStatement = timDbTables.buildInsertQueryStatement("data_frame",
					timDbTables.getDataFrameTable());
			preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "data_frame_id" });
			int fieldNum = 1;

			for (String col : timDbTables.getDataFrameTable()) {
				if (col.equals("TIM_ID")) {
					sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, timId);
				} else if (col.equals("SSP_TIM_RIGHTS")) {
					sqlNullHandler.setShortOrNull(preparedStatement, fieldNum, dFrame.getDoNotUse1());
				} else if (col.equals("FRAME_TYPE")) {
					Integer ordinal = null;
					if (dFrame.getFrameType() != null) {
						ordinal = dFrame.getFrameType().ordinal();
					}
					sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, ordinal);
				} else if (col.equals("DURATION_TIME")) {
					sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, dFrame.getDurationTime());
				} else if (col.equals("PRIORITY")) {
					sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, dFrame.getPriority());
				} else if (col.equals("SSP_LOCATION_RIGHTS")) {
					sqlNullHandler.setShortOrNull(preparedStatement, fieldNum, dFrame.getDoNotUse2());
				} else if (col.equals("SSP_MSG_TYPES")) {
					sqlNullHandler.setShortOrNull(preparedStatement, fieldNum, dFrame.getDoNotUse4());
				} else if (col.equals("SSP_MSG_CONTENT")) {
					sqlNullHandler.setShortOrNull(preparedStatement, fieldNum, dFrame.getDoNotUse3());
				} else if (col.equals("CONTENT")) {
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, dFrame.getContent());
				} else if (col.equals("URL")) {
					sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, dFrame.getUrl());
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
					sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, time);
				}

				fieldNum++;
			}

			Long dataFrameId = dbInteractions.executeAndLog(preparedStatement, "dataframe");
			return ResponseEntity.ok(dataFrameId);
		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Long.valueOf(0));
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
	}
}