package com.trihydro.loggerkafkaconsumer.app.services;

import com.trihydro.library.helpers.DbInteractions;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service class for handling TIM RSU operations.
 */
@Component
@Slf4j
public class TimRsuService {
    private final DbInteractions dbInteractions;
    private final TimDbTables timDbTables;
    private final SQLNullHandler sqlNullHandler;

    /**
     * Constructor for TimRsuService.
     *
     * @param dbInteractions the database interactions helper
     * @param timDbTables the TIM database tables helper
     * @param sqlNullHandler the SQL null handler
     */
    @Autowired
    public TimRsuService(DbInteractions dbInteractions, TimDbTables timDbTables, SQLNullHandler sqlNullHandler) {
        this.dbInteractions = dbInteractions;
        this.timDbTables = timDbTables;
        this.sqlNullHandler = sqlNullHandler;
    }

    /**
     * Adds a TIM RSU record to the database.
     *
     * @param timId the TIM ID
     * @param rsuId the RSU ID
     * @param rsuIndex the RSU index
     * @return the ID of the inserted TIM RSU record, 0 if a duplicate record is detected, or -1 if an error occurs
     */
    public Long AddTimRsu(Long timId, Integer rsuId, Integer rsuIndex) {
        String insertQueryStatement = timDbTables.buildInsertQueryStatement("tim_rsu", timDbTables.getTimRsuTable());

        try (Connection connection = dbInteractions.getConnectionPool(); PreparedStatement preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] {"tim_rsu_id"})) {
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
            return -1L;
        }
    }
}