package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.tables.TimOracleTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("tim-rsu")
public class TimRsuController extends BaseController {

    private TimOracleTables timOracleTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimOracleTables _timOracleTables, SQLNullHandler _sqlNullHandler) {
        timOracleTables = _timOracleTables;
        sqlNullHandler = _sqlNullHandler;
    }

    @RequestMapping(value = "/add-tim-rsu/{timId}/{rsuId}/{rsuIndex}", method = RequestMethod.POST, headers = "Accept=application/json")
    public ResponseEntity<Long> AddTimRsu(@PathVariable Long timId, @PathVariable Integer rsuId,
            @PathVariable Integer rsuIndex) {

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {
            connection = dbInteractions.getConnectionPool();
            String insertQueryStatement = timOracleTables.buildInsertQueryStatement("TIM_RSU",
                    timOracleTables.getTimRsuTable());
            preparedStatement = connection.prepareStatement(insertQueryStatement, new String[] { "TIM_RSU_ID" });
            int fieldNum = 1;

            for (String col : timOracleTables.getTimRsuTable()) {
                if (col.equals("TIM_ID"))
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, timId);
                else if (col.equals("RSU_ID"))
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, rsuId);
                else if (col.equals("RSU_INDEX"))
                    sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, rsuIndex);
                fieldNum++;
            }
            Long timRsuId = dbInteractions.executeAndLog(preparedStatement, "tim rsu");
            return ResponseEntity.ok(timRsuId);
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

    @RequestMapping(value = "/tim-id/{timId}", method = RequestMethod.GET)
    public ResponseEntity<List<TimRsu>> GetTimRsusByTimId(@PathVariable Long timId) {

        Statement statement = null;
        Connection connection = null;
        ResultSet rs = null;
        List<TimRsu> timRsus = new ArrayList<TimRsu>();

        try {

            connection = dbInteractions.getConnectionPool();
            statement = connection.createStatement();
            // build SQL statement
            // rs = statement.executeQuery("select * from TIM_RSU where tim_id = " + timId);
            rs = statement.executeQuery("select rsu_id,tim_id,rsu_index from tim_rsu where tim_id = " + timId
                    + " group by rsu_id,tim_id,rsu_index");

            // convert to DriverAlertType objects
            while (rs.next()) {
                TimRsu timRsu = new TimRsu();
                timRsu.setTimId(rs.getLong("TIM_ID"));
                timRsu.setRsuId(rs.getLong("RSU_ID"));
                timRsu.setRsuIndex(rs.getInt("RSU_INDEX"));
                timRsus.add(timRsu);
            }
            return ResponseEntity.ok(timRsus);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(timRsus);
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

    @RequestMapping(value = "/tim-rsu/{timId}/{rsuId}", method = RequestMethod.GET)
    public ResponseEntity<TimRsu> GetTimRsu(@PathVariable Long timId, @PathVariable Integer rsuId) {

        Statement statement = null;
        Connection connection = null;
        ResultSet rs = null;
        TimRsu timRsu = new TimRsu();

        try {
            connection = dbInteractions.getConnectionPool();
            statement = connection.createStatement();
            // build SQL statement
            rs = statement.executeQuery("select * from TIM_RSU where rsu_id = " + rsuId + " and tim_id = " + timId);

            // convert to DriverAlertType objects
            while (rs.next()) {
                timRsu.setTimRsuId(rs.getLong("TIM_RSU_ID"));
                timRsu.setTimId(rs.getLong("TIM_ID"));
                timRsu.setRsuId(rs.getLong("RSU_ID"));
                timRsu.setRsuIndex(rs.getInt("RSU_INDEX"));
            }
            return ResponseEntity.ok(timRsu);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
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
}