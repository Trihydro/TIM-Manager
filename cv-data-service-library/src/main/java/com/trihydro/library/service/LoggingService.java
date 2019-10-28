package com.trihydro.library.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.trihydro.library.helpers.DbUtility;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.LoggingTables;

public class LoggingService extends CvDataServiceLibrary {
    public static Long LogHttpRequest(String request, Timestamp requestTime, Timestamp responseTime) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {

            connection = DbUtility.getConnectionPool();
            String insertQueryStatement = LoggingTables.buildInsertQueryStatement("http_logging",
                    LoggingTables.getHttpLoggingTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "http_logging_id" });
            int fieldNum = 1;

            for (String col : LoggingTables.getHttpLoggingTable()) {
                if (col.equals("REQUEST_TIME")) {
                    SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, requestTime);
                } else if (col.equals("REST_REQUEST")) {
                    SQLNullHandler.setStringOrNull(preparedStatement, fieldNum, request);
                } else if (col.equals("RESPONSE_TIME")) {
                    SQLNullHandler.setTimestampOrNull(preparedStatement, fieldNum, responseTime);
                }

                fieldNum++;
            }

            Long httpLoggingId = log(preparedStatement, "http_logging");
            return httpLoggingId;
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