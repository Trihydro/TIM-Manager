package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PathService extends BaseService {

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long InsertPath() {

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {
            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = timDbTables.buildInsertQueryStatement("path",
                    timDbTables.getPathTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "path_id" });
            int fieldNum = 1;

            for (String col : timDbTables.getPathTable()) {
                if (col.equals("SCALE"))
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, 0);
                fieldNum++;
            }
            // execute insert statement
            Long pathId = dbInteractions.executeAndLog(preparedStatement, "pathId");
            return pathId;
        } catch (SQLException e) {
            log.error("Exception", e);
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
        // if we got here, its an error
        return Long.valueOf(0);
    }
}