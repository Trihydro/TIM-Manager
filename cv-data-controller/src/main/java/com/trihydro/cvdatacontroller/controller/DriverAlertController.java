package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.Utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("driver-alert")
@ApiIgnore
public class DriverAlertController extends BaseController {
    private Utility utility;

    @Autowired
    public void InjectDependencies(Utility _utility) {
        utility = _utility;
    }

    @RequestMapping(value = "/delete-old", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<Boolean> DeleteOldDriverAlert() {
        boolean deleteResult = true;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        String strDate = getOneMonthPrior();

        try {
            deleteResult = deleteOldDriverAlertItisCode(strDate);

            if (!deleteResult) {
                utility.logWithDate("Failed to cleanup old driver_alert_itis_code");
                return ResponseEntity.ok(false);
            }

            String deleteSQL = "DELETE FROM driver_alert WHERE ode_received_at < ?";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setString(1, strDate);

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

    private boolean deleteOldDriverAlertItisCode(String strDate) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM driver_alert_itis_code WHERE driver_alert_id IN";
            deleteSQL += " (SELECT driver_alert_id FROM driver_alert WHERE ode_received_at < ?)";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setString(1, strDate);

            // execute delete SQL stetement
            deleteResult = dbInteractions.updateOrDelete(preparedStatement);
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

        if (!deleteResult) {
            utility.logWithDate("Failed to delete bsm_part2_suve");
        }
        return deleteResult;
    }
}