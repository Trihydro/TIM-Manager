package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.HttpLoggingModel;
import com.trihydro.library.tables.LoggingTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("http-logging")
@ApiIgnore
public class HttpLoggingController extends BaseController {

    private LoggingTables loggingTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(LoggingTables _loggingTables, SQLNullHandler _sqlNullHandler) {
        loggingTables = _loggingTables;
        sqlNullHandler = _sqlNullHandler;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/add-http-logging")
    public ResponseEntity<Long> LogHttpRequest(@RequestBody HttpLoggingModel httpLoggingModel) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = loggingTables.buildInsertQueryStatement("http_logging",
                    loggingTables.getHttpLoggingTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "http_logging_id" });
            int fieldNum = 1;

            for (String col : loggingTables.getHttpLoggingTable()) {
                if (col.equals("REQUEST_TIME")) {
                    sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, httpLoggingModel.getRequestTime());
                } else if (col.equals("REST_REQUEST")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, httpLoggingModel.getRequest());
                } else if (col.equals("RESPONSE_TIME")) {
                    sqlNullHandler.setTimestampOrNull(preparedStatement, fieldNum, httpLoggingModel.getResponseTime());
                }

                fieldNum++;
            }

            Long httpLoggingId = dbInteractions.executeAndLog(preparedStatement, "http_logging");
            return ResponseEntity.ok(httpLoggingId);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Long.valueOf(0));
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

}