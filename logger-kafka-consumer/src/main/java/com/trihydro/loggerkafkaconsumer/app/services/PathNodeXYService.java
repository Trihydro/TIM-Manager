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
public class PathNodeXYService extends BaseService {

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long insertPathNodeXY(Long nodeXYId, Long pathId) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {

            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = timDbTables.buildInsertQueryStatement("path_node_xy",
                    timDbTables.getPathNodeXYTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "path_node_xy_id" });
            int fieldNum = 1;

            for (String col : timDbTables.getPathNodeXYTable()) {
                if (col.equals("NODE_XY_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, nodeXYId);
                else if (col.equals("PATH_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);
                fieldNum++;
            }
            // execute insert statement
            Long pathNodeXYId = dbInteractions.executeAndLog(preparedStatement, "pathnodexyid");
            return pathNodeXYId;

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
        return Long.valueOf(0);
    }
}