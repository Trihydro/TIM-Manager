package com.trihydro.cvdatacontroller.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.ActiveTimHolding;
import com.trihydro.library.tables.TimOracleTables;

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
@RequestMapping("active-tim-holding")
@ApiIgnore
public class ActiveTimHoldingController extends BaseController {
    private TimOracleTables timOracleTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimOracleTables _timOracleTables, SQLNullHandler _sqlNullHandler) {
        timOracleTables = _timOracleTables;
        sqlNullHandler = _sqlNullHandler;
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public ResponseEntity<Long> InsertActiveTimHolding(@RequestBody ActiveTimHolding activeTimHolding) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        Long activeTimId = 0l;
        try {
            String insertQueryStatement = timOracleTables.buildInsertQueryStatement("active_tim_holding",
                    timOracleTables.getActiveTimHoldingTable());

            // get connection
            connection = GetConnectionPool();

            preparedStatement = connection.prepareStatement(insertQueryStatement,
                    new String[] { "active_tim_holding_id" });
            int fieldNum = 1;

            for (String col : timOracleTables.getActiveTimHoldingTable()) {
                if (col.equals("ACTIVE_TIM_HOLDING_ID")) {
                    sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, activeTimHolding.getActiveTimHoldingId());
                } else if (col.equals("CLIENT_ID")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTimHolding.getClientId());
                } else if (col.equals("DIRECTION")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTimHolding.getDirection());
                } else if (col.equals("RSU_TARGET")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTimHolding.getRsuTarget());
                } else if (col.equals("SAT_RECORD_ID")) {
                    sqlNullHandler.setStringOrNull(preparedStatement, fieldNum, activeTimHolding.getSatRecordId());
                } else if (col.equals("START_LATITUDE")) {
                    if (activeTimHolding.getStartPoint() != null) {
                        sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                activeTimHolding.getStartPoint().getLatitude());
                    }
                } else if (col.equals("START_LONGITUDE")) {
                    if (activeTimHolding.getStartPoint() != null) {
                        sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                activeTimHolding.getStartPoint().getLongitude());
                    }
                } else if (col.equals("END_LATITUDE")) {
                    if (activeTimHolding.getEndPoint() != null) {
                        sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                activeTimHolding.getEndPoint().getLatitude());
                    }
                } else if (col.equals("END_LONGITUDE")) {
                    if (activeTimHolding.getEndPoint() != null) {
                        sqlNullHandler.setDoubleOrNull(preparedStatement, fieldNum,
                                activeTimHolding.getEndPoint().getLongitude());
                    }
                }

                fieldNum++;
            }

            activeTimId = executeAndLog(preparedStatement, "active tim holding");
            return ResponseEntity.ok(activeTimId);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(activeTimId);
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