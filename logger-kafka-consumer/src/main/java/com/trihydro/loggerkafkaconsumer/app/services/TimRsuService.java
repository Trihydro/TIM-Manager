package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TimRsuService extends BaseService {

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long AddTimRsu(Long timId, Integer rsuId, Integer rsuIndex) {
        String insertQueryStatement = timDbTables.buildInsertQueryStatement("tim_rsu", timDbTables.getTimRsuTable());

        try(Connection connection = dbInteractions.getConnectionPool(); PreparedStatement preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"tim_rsu_id"})) {
            int fieldNum = 1;

            for (String col : timDbTables.getTimRsuTable()) {
                if (col.equals("TIM_ID")) {
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, timId);
                } else if (col.equals("RSU_ID")) {
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, rsuId);
                } else if (col.equals("RSU_INDEX")) {
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, rsuIndex);
                }
                fieldNum++;
            }
            return dbInteractions.executeAndLog(preparedStatement, "tim rsu");
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("unique constraint")) {
                // Avoid logging the common error when trying to insert a duplicate record
                return 0L;
            }

            // Log the exception if it's not a unique constraint violation
            log.error("SQL Exception while adding TIM RSU: {}", e.getMessage(), e);
            return 0L;
        }
    }
}