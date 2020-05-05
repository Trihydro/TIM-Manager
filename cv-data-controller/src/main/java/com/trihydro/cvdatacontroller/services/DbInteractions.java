package com.trihydro.cvdatacontroller.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

import com.trihydro.cvdatacontroller.model.DataControllerConfigProperties;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.Utility;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DbInteractions {
    private HikariDataSource hds;
    private HikariConfig config;

    protected DataControllerConfigProperties dbConfig;
    protected Utility utility;
    private EmailHelper emailHelper;

    @Autowired
    public void InjectDependencies(DataControllerConfigProperties props, Utility _utility, EmailHelper _emailHelper) {
        dbConfig = props;
        utility = _utility;
        emailHelper = _emailHelper;
    }

    public Connection getConnectionPool() throws SQLException {
        // create pool if not already done
        if (hds == null) {

            TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
            TimeZone.setDefault(timeZone);
            config = new HikariConfig();

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.setUsername(dbConfig.getDbUsername());
            config.setPassword(dbConfig.getDbPassword());
            config.setJdbcUrl(dbConfig.getDbUrl());
            config.setDriverClassName(dbConfig.getDbDriver());
            config.setMaximumPoolSize(dbConfig.getPoolSize());// splitting pool size between here and the kafka consumer
            config.setMaxLifetime(600000);// setting a maxLifetime of 10 minutes (defaults to 30), to help avoid
                                          // connection issues

            hds = new HikariDataSource(config);
        }
        // return a connection
        try {
            return hds.getConnection();
        } catch (SQLException ex) {
            String body = "The ODE Wrapper failed attempting to open a connection to ";
            body += dbConfig.getDbUrl();
            body += ". <br/>Exception message: ";
            body += ex.getMessage();
            body += "<br/>Stacktrace: ";
            body += ExceptionUtils.getStackTrace(ex);
            try {
                emailHelper.SendEmail(dbConfig.getAlertAddresses(), null, "ODE Wrapper Failed To Get Connection", body,
                        dbConfig.getMailPort(), dbConfig.getMailHost(), dbConfig.getFromEmail());
            } catch (Exception exception) {
                utility.logWithDate("ODE Wrapper failed to open connection to " + dbConfig.getDbUrl()
                        + ", then failed to send email");
                exception.printStackTrace();
            }
            throw ex;
        }
    }

    public boolean updateOrDelete(PreparedStatement preparedStatement) {

        boolean result = false;

        try {
            if (preparedStatement.executeUpdate() > 0) {
                result = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Long executeAndLog(PreparedStatement preparedStatement, String type) {
        Long id = null;
        try {
            if (preparedStatement.executeUpdate() > 0) {
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                try {
                    if (generatedKeys != null && generatedKeys.next()) {
                        id = generatedKeys.getLong(1);
                        utility.logWithDate("------ Generated " + type + " " + id + " --------------");
                    }
                } finally {
                    try {
                        generatedKeys.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }
}