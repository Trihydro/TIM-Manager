package com.trihydro.library.helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimeZone;

import com.trihydro.library.model.DbInteractionsProps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DbInteractions {
    private static HikariDataSource dataSource;

    protected DbInteractionsProps dbConfig;
    protected Utility utility;
    protected EmailHelper emailHelper;

    @Autowired
    public void InjectDependencies(DbInteractionsProps props, Utility _utility, EmailHelper _emailHelper) {
        dbConfig = props;
        utility = _utility;
        emailHelper = _emailHelper;
        log.info("A new DbInteractions instance has been created.");
        validateDbConfig();
    }

    public Connection getConnectionPool() throws SQLException {
        // create pool if not already done
        if (dataSource == null) {
            initializePrimaryConnectionPool();
        }

        // return a connection
        try {
            return dataSource.getConnection();
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
                log.error("Failed to open connection to {}, then failed to send email", dbConfig.getDbUrl(), exception);
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
            log.error("Error in updateOrDelete", e);
        }

        return result;
    }

    public boolean deleteWithPossibleZero(PreparedStatement preparedStatement) {
        boolean result = false;
        try {
            preparedStatement.executeUpdate();
            result = true;
        } catch (SQLException e) {
            log.error("Error in deleteWithPossibleZero", e);
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
                        log.trace("------ Generated {} {} --------------", type, id);
                    }
                } finally {
                    try {
                        generatedKeys.close();
                    } catch (Exception e) {
                        log.error("Error in executeAndLog", e);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error in executeAndLog", e);
        }
        return id;
    }

    private void validateDbConfig() {
        if (dataSource == null) {
            TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
            TimeZone.setDefault(timeZone);

            // check dbconfig for null values
            if (dbConfig.getDbUrl() == null ||
                dbConfig.getDbUsername() == null ||
                dbConfig.getDbPassword() == null ||
                dbConfig.getMaximumPoolSize() == 0 ||
                dbConfig.getConnectionTimeout() == 0) {
                log.error("One or more database configuration values are undefined. Exiting.");
                System.exit(1);
            }
        }
    }

    private void initializePrimaryConnectionPool() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");
        config.setJdbcUrl(dbConfig.getDbUrl());
        config.setUsername(dbConfig.getDbUsername());
        config.setPassword(dbConfig.getDbPassword());
        config.setConnectionTimeout(dbConfig.getConnectionTimeout());
        config.setMaximumPoolSize(dbConfig.getMaximumPoolSize());

        // log the configuration of the connection pool
        log.info("Creating connection pool with the following configuration:");
        log.info("driverClassName: {}", config.getDriverClassName());
        log.info("dbUrl: {}", dbConfig.getDbUrl());
        log.info("dbUsername: {}", dbConfig.getDbUsername());
        log.info("connectionTimeout: {}", config.getConnectionTimeout());
        log.info("maximumPoolSize: {}", config.getMaximumPoolSize());

        dataSource = new HikariDataSource(config);
        log.info("Successfully initialized connection pool");
    }
}