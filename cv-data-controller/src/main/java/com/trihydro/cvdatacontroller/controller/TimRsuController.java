package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
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
            connection = GetConnectionPool();
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
            Long timRsuId = executeAndLog(preparedStatement, "tim rsu");
            return ResponseEntity.ok(timRsuId);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Long(0));
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