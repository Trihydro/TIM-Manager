package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("hmi-log")
@ApiIgnore
public class HmiLogController extends BaseController {

    @RequestMapping(value = "/delete-old", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<Boolean> DeleteOldHmiLogs() {
        boolean deleteResult = true;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        Timestamp oneMonthPriorTimestamp = getOneMonthPriorTimestamp();

        try {
            String deleteSQL = "DELETE FROM hmi_log WHERE received_at < ?";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setTimestamp(1, oneMonthPriorTimestamp);

            // execute delete SQL stetement
            deleteResult = dbInteractions.updateOrDelete(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
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

        return ResponseEntity.ok(deleteResult);
    }

}