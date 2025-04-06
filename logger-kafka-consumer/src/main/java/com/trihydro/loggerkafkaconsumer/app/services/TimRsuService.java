package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimRsuService extends BaseService {
    private static final Logger LOG = LoggerFactory.getLogger(TimRsuService.class);

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long AddTimRsu(Long timId, Integer rsuId, Integer rsuIndex) {

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {
            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = timDbTables.buildInsertQueryStatement("tim_rsu",
                    timDbTables.getTimRsuTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "tim_rsu_id" });
            int fieldNum = 1;

            for (String col : timDbTables.getTimRsuTable()) {
                if (col.equals("TIM_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, timId);
                else if (col.equals("RSU_ID"))
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, rsuId);
                else if (col.equals("RSU_INDEX"))
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, rsuIndex);
                fieldNum++;
            }
            Long timRsuId = dbInteractions.executeAndLog(preparedStatement, "tim rsu");
            return timRsuId;
        } catch (SQLException e) {
            // java.sql.SQLIntegrityConstraintViolationException: ORA-00001: unique constraint (CVCOMMS.TIM_RSU_U) violated 
            if(!(e instanceof SQLIntegrityConstraintViolationException)) {
                LOG.error("Exception", e);
            }

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
                LOG.error("Exception", e);
            }
        }
    }
}