package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.BsmCoreDataPartition;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("utility")
@ApiIgnore
public class UtilityController extends BaseController {
    private DateFormat partHighValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @RequestMapping(value = { "/bsm-core-data-partitions" })
    public ResponseEntity<List<BsmCoreDataPartition>> getBsmCoreDataPartitions() {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<BsmCoreDataPartition> results = new ArrayList<>();

        try {
            connection = dbInteractions.getConnectionPool();
            var query = "SELECT partition_name, high_value FROM user_tab_partitions WHERE table_name = 'BSM_CORE_DATA' AND interval = 'YES'";

            ps = connection.prepareStatement(query);
            rs = ps.executeQuery();

            while (rs.next()) {
                var partitionName = rs.getString("PARTITION_NAME");
                var stringHighValue = rs.getString("HIGH_VALUE");

                stringHighValue = stringHighValue.replaceFirst("TIMESTAMP'\\s", "").replace("'", "");

                try {
                    var highValue = partHighValue.parse(stringHighValue);
                    results.add(new BsmCoreDataPartition(partitionName, highValue));
                } catch (ParseException ex) {
                    utility.logWithDate(String.format("Unable to parse Timestamp (%s) for partition (%s)... Skipping.",
                            stringHighValue, partitionName));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            try {
                // close prepared statement
                if (ps != null)
                    ps.close();
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

        return ResponseEntity.ok(results);
    }

    @RequestMapping(value = { "/drop-bsm-partitions" }, method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> dropBsmPartitions(@RequestBody List<String> partitionNames) {
        if (partitionNames == null || partitionNames.size() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        var requestValid = true;
        for (var name : partitionNames) {
            if (StringUtils.isBlank(name)) {
                requestValid = false;
                break;
            }
        }
        if (!requestValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Connection connection = null;
        PreparedStatement ps = null;

        try {
            connection = dbInteractions.getConnectionPool();

            for (var name : partitionNames) {
                utility.logWithDate("Removing partition " + name + " from BSM_CORE_DATA.");
                var statement = String.format("ALTER TABLE BSM_CORE_DATA DROP PARTITION %s UPDATE INDEXES", name);
                ps = connection.prepareStatement(statement);
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            try {
                // close prepared statement
                if (ps != null)
                    ps.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return ResponseEntity.ok(true);
    }
}