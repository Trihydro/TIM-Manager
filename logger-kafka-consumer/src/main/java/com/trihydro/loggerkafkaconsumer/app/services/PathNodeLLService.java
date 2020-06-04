package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PathNodeLLService extends BaseService {

    private TimOracleTables timOracleTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimOracleTables _timOracleTables, SQLNullHandler _sqlNullHandler) {
        timOracleTables = _timOracleTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long insertPathNodeLL(Long nodeLLId, Long pathId) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {

            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = timOracleTables.buildInsertQueryStatement("path_node_ll",
                    timOracleTables.getPathNodeLLTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "path_node_ll_id" });
            int fieldNum = 1;

            for (String col : timOracleTables.getPathNodeLLTable()) {
                if (col.equals("NODE_LL_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, nodeLLId);
                else if (col.equals("PATH_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);
                fieldNum++;
            }
            // execute insert statement
            Long pathNodeXYId = dbInteractions.executeAndLog(preparedStatement, "pathnodellid");
            return pathNodeXYId;

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
        return Long.valueOf(0);
    }
}