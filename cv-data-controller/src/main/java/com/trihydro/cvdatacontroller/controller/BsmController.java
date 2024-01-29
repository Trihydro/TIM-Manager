package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@CrossOrigin
@RestController
@RequestMapping("bsm")
@ApiIgnore
public class BsmController extends BaseController {

    @RequestMapping(value = "/delete-old/{retentionDays}", method = RequestMethod.DELETE, headers = "Accept=application/json")
    public ResponseEntity<Boolean> deleteOldBsm(@PathVariable Integer retentionDays) {
        // delete all bsm_part2_spve, bsm_part2_suve, bsm_part2_vse, bsm_core_data
        // over retentionDays days old
        boolean deleteResult = true;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            var maxBsmCoreDataId = getMaxBsmCoreDataIdByDate(retentionDays);

            deleteResult = deleteOldBsmPart2Suve(maxBsmCoreDataId);
            deleteResult &= deleteOldBsmPart2Spve(maxBsmCoreDataId);
            deleteResult &= deleteOldBsmPart2Vse(maxBsmCoreDataId);

            if (!deleteResult) {
                utility.logWithDate("Failed to cleanup old BSM");
                return ResponseEntity.ok(false);
            }

            String deleteSQL = "DELETE FROM bsm_core_data WHERE bsm_core_data_id <= ?";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setInt(1, maxBsmCoreDataId);

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

    private Integer getMaxBsmCoreDataIdByDate(Integer retentionDays) {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        Integer maxId = -1;

        try {
            connection = dbInteractions.getConnectionPool();
            statement = connection.createStatement();
            String selectStatement = "select max(bsm_core_data_id) maxId from bsm_core_data where record_generated_at";
            selectStatement += " < Statement_timestamp() - INTERVAL '";
            selectStatement += retentionDays + "' DAY";
            rs = statement.executeQuery(selectStatement);

            // convert to ActiveTim object
            while (rs.next()) {
                maxId = rs.getInt("maxId");
            }
            return maxId;
        } catch (Exception e) {
            e.printStackTrace();
            return maxId;
        } finally {
            try {
                // close prepared statement
                if (statement != null)
                    statement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
                // close result set
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private Boolean deleteOldBsmPart2Suve(Integer maxBsmCoreDataId) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM bsm_part2_suve WHERE bsm_core_data_id <= ?";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setInt(1, maxBsmCoreDataId);

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

    private Boolean deleteOldBsmPart2Spve(Integer maxBsmCoreDataId) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM bsm_part2_spve WHERE bsm_core_data_id <= ?";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setInt(1, maxBsmCoreDataId);

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
            utility.logWithDate("Failed to delete bsm_part2_spve");
        }
        return deleteResult;
    }

    private Boolean deleteOldBsmPart2Vse(Integer maxBsmCoreDataId) {
        boolean deleteResult = false;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            String deleteSQL = "DELETE FROM bsm_part2_vse WHERE bsm_core_data_id <= ?";
            connection = dbInteractions.getConnectionPool();
            preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setInt(1, maxBsmCoreDataId);

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
            utility.logWithDate("Failed to delete bsm_part2_vse");
        }
        return deleteResult;
    }
}