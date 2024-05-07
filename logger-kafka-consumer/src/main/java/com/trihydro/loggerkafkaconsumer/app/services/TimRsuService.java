package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimRsuService extends BaseService {

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
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }

    public boolean recordExists(Long timId, Integer rsuId, int rsuIndex) {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;

        try {
			connection = dbInteractions.getConnectionPool();
			statement = connection.createStatement();
            
            String query = String.format("SELECT 1 FROM %s WHERE TIM_ID = %d AND RSU_ID = %d AND RSU_INDEX = %d", timDbTables.getTimRsuTable(), timId, rsuId, rsuIndex);
            rs = statement.executeQuery(query);
            
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close result set
                if (rs != null)
                    rs.close();
                // close statement
                if (statement != null)
                    statement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}