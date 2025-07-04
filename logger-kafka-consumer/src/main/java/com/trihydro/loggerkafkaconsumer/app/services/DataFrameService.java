package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame;

@Component
@Slf4j
public class DataFrameService extends BaseService {

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) { // TODO: use constructor instead of InjectDependencies
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long AddDataFrame(DataFrame dFrame, Long timId) {
        String insertQueryStatement = timDbTables.buildInsertQueryStatement("data_frame",
            timDbTables.getDataFrameTable());

        try (
            Connection connection = dbInteractions.getConnectionPool();
            PreparedStatement preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"data_frame_id"});
        ) {
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
                    if (dFrame.getStartDateTime() == null) {
                        preparedStatement.setNull(fieldNum, java.sql.Types.TIMESTAMP);
                    } else {
                        Timestamp time = null;
                        try {
                            TimeZone tz = TimeZone.getTimeZone("UTC");
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no
                            // timezone offset
                            df.setTimeZone(tz);
                            Date dt = df.parse(dFrame.getStartDateTime());
                            time = new Timestamp(dt.getTime());
                        } catch (ParseException ex) {
                            log.error("Unable to parse startdate: {}", dFrame.getStartDateTime());
                        }
                        sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, time);
                    }
                }
                fieldNum++;
            }

            return dbInteractions.executeAndLog(preparedStatement, "dataframe");
        } catch (SQLException e) {
            log.error("Error adding data frame", e);
        }
        return 0L;
    }

}