package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PathNodeLLService extends BaseService {
    private static final Logger LOG = LoggerFactory.getLogger(PathNodeLLService.class);

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long insertPathNodeLL(Long nodeLLId, Long pathId) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {

            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = timDbTables.buildInsertQueryStatement("path_node_ll",
                    timDbTables.getPathNodeLLTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "path_node_ll_id" });
            int fieldNum = 1;

            for (String col : timDbTables.getPathNodeLLTable()) {
                if (col.equals("NODE_LL_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, nodeLLId);
                else if (col.equals("PATH_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);
                fieldNum++;
            }
            // execute insert statement
            Long pathNodeLLId = dbInteractions.executeAndLog(preparedStatement, "pathnodellid");
            return pathNodeLLId;

        } catch (SQLException e) {
            LOG.error("Exception", e);
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
        return Long.valueOf(0);
    }
}