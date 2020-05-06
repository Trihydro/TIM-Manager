package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PathService extends BaseService {

    private TimOracleTables timOracleTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimOracleTables _timOracleTables, SQLNullHandler _sqlNullHandler) {
        timOracleTables = _timOracleTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long InsertPath() {

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {
            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = timOracleTables.buildInsertQueryStatement("path",
                    timOracleTables.getPathTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "path_id" });
            int fieldNum = 1;

            for (String col : timOracleTables.getPathTable()) {
                if (col.equals("SCALE"))
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, 0);
                fieldNum++;
            }
            // execute insert statement
            Long pathId = dbInteractions.executeAndLog(preparedStatement, "pathId");
            return pathId;
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
        // if we got here, its an error
        return Long.valueOf(0);
    }
}