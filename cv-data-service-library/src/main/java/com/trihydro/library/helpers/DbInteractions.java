package com.trihydro.library.helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

import com.trihydro.library.model.DbInteractionsProps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DbInteractions {
    private HikariDataSource hds;
    private HikariConfig config;

    protected DbInteractionsProps dbConfig;
    protected Utility utility;
    private EmailHelper emailHelper;

    @Autowired
    public void InjectDependencies(DbInteractionsProps props, Utility _utility, EmailHelper _emailHelper) {
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
            config.setMaximumPoolSize(dbConfig.getPoolSize());
            // Pool Size formula: connections = ((core_count*2) + effective_spindle_count)
            // https://stackoverflow.com/questions/28987540/why-does-hikaricp-recommend-fixed-size-pool-for-better-performance
            config.setMaxLifetime(600000);// setting to 10 minutes (defaults to 30), to help avoid connection issues

            hds = new HikariDataSource(config);
        }
        // return a connection
        try {
            return hds.getConnection();
        } catch (SQLException ex) {
            String body = "Failed attempting to open a connection to ";
            body += dbConfig.getDbUrl();
            body += ". <br/>Exception message: ";
            body += ex.getMessage();
            body += "<br/>Stacktrace: ";
            body += ExceptionUtils.getStackTrace(ex);
            try {
                emailHelper.SendEmail(dbConfig.getAlertAddresses(), "Failed To Get Connection", body);
            } catch (Exception exception) {
                utility.logWithDate("Failed to open connection to " + dbConfig.getDbUrl()
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

    public boolean deleteWithPossibleZero(PreparedStatement preparedStatement) {
        boolean result = false;
        try {
            preparedStatement.executeUpdate();
            result = true;
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