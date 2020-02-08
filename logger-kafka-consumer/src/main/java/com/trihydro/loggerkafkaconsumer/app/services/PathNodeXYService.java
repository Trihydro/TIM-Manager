package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PathNodeXYService extends BaseService {

    private TimOracleTables timOracleTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimOracleTables _timOracleTables, SQLNullHandler _sqlNullHandler) {
        timOracleTables = _timOracleTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long insertPathNodeXY(Long nodeXYId, Long pathId) {
        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {

            connection = GetConnectionPool();
            String insertQueryStatement = timOracleTables.buildInsertQueryStatement("path_node_xy",
                    timOracleTables.getPathNodeXYTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "path_node_xy_id" });
            int fieldNum = 1;

            for (String col : timOracleTables.getPathNodeXYTable()) {
                if (col.equals("NODE_XY_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, nodeXYId);
                else if (col.equals("PATH_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, pathId);
                fieldNum++;
            }
            // execute insert statement
            Long pathNodeXYId = log(preparedStatement, "pathnodexyid");
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
        return new Long(0);
    }
}