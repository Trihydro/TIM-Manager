package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DriverAlertItisCodeService extends BaseService {

    private TimDbTables timDbTables;
    private SQLNullHandler sqlNullHandler;

    @Autowired
    public void InjectDependencies(TimDbTables _timDbTables, SQLNullHandler _sqlNullHandler) {
        timDbTables = _timDbTables;
        sqlNullHandler = _sqlNullHandler;
    }

    public Long insertDriverAlertItisCode(Long driverAlertId, Integer itisCodeId) {

        PreparedStatement preparedStatement = null;
        Connection connection = null;

        try {

            String insertQueryStatement = timDbTables.buildInsertQueryStatement("driver_alert_itis_code",
                    timDbTables.getDriverAlertItisCodeTable());
            connection = dbInteractions.getConnectionPool();
            if (connection != null) {
                preparedStatement = connection.prepareStatement(insertQueryStatement,
                        new String[] { "driver_alert_itis_code_id" });
                int fieldNum = 1;

                for (String col : timDbTables.getDriverAlertItisCodeTable()) {
                    if (col.equals("ITIS_CODE_ID"))
                        sqlNullHandler.setIntegerOrNull(preparedStatement, fieldNum, itisCodeId);
                    else if (col.equals("DRIVER_ALERT_ID"))
                        sqlNullHandler.setLongOrNull(preparedStatement, fieldNum, driverAlertId);
                    fieldNum++;
                }

                Long driverAlertItisCodeId = dbInteractions.executeAndLog(preparedStatement, "driverAlertItisCode");
                return driverAlertItisCodeId;
            }

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
        return Long.valueOf(0);
    }
}