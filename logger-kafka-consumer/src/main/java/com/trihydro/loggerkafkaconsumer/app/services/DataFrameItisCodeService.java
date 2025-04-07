package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataFrameItisCodeService extends BaseService {

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long insertDataFrameItisCode(Long dataFrameId, String itis, Integer position) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            String insertQueryStatement = timDbTables.buildInsertQueryStatement("data_frame_itis_code",
                    timDbTables.getDataFrameItisCodeTable());
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(insertQueryStatement,
                    new String[] { "data_frame_itis_code_id" });
            int fieldNum = 1;

            for (String col : timDbTables.getDataFrameItisCodeTable()) {
                if (col.equals("ITIS_CODE_ID")) {
                    if (StringUtils.isNumeric(itis))
                        sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, Long.parseLong(itis));
                    else
                        sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, null);
                } else if (col.equals("TEXT")) {
                    if (!StringUtils.isNumeric(itis))
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, itis);
                    else
                        sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, null);
                } else if (col.equals("DATA_FRAME_ID")) {
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, dataFrameId);
                } else if (col.equals("POSITION")) {
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, position);
                }
                fieldNum++;
            }

            Long dataFrameItisCodeId = dbInteractions.executeAndLog(preparedStatement, "dataFrameItisCode");
            return dataFrameItisCodeId;
        } catch (SQLException e) {
            log.error("Exception", e);
            return Long.valueOf(0);
        } finally {
            try {
                // close prepared statement
                if (preparedStatement != null)
                    preparedStatement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                log.error("Exception", e);
            }
        }
    }
}