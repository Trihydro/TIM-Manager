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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DbInteractions {
    private static final Logger LOG = LoggerFactory.getLogger(DbInteractions.class);
    private static HikariDataSource dataSource;

    protected DbInteractionsProps dbConfig;
    protected Utility utility;
    protected EmailHelper emailHelper;

    @Autowired
    public void InjectDependencies(DbInteractionsProps props, Utility _utility, EmailHelper _emailHelper) {
        dbConfig = props;
        utility = _utility;
        emailHelper = _emailHelper;
        LOG.info("A new DbInteractions instance has been created.");
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
                LOG.info("Failed to open connection to {}, then failed to send email", dbConfig.getDbUrl());
                LOG.error("Exception", exception);
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
            LOG.error("Exception", e);
        }

        return result;
    }

    public boolean deleteWithPossibleZero(PreparedStatement preparedStatement) {
        boolean result = false;
        try {
            preparedStatement.executeUpdate();
            result = true;
        } catch (SQLException e) {
            LOG.error("Exception", e);
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
                        LOG.info("------ Generated {} {} --------------", type, id);
                    }
                } finally {
                    try {
                        generatedKeys.close();
                    } catch (Exception e) {
                        LOG.error("Exception", e);
                    }
                }
            }
        } catch (SQLException e) {
            LOG.error("Exception", e);
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
                LOG.info("DbInteractions: One or more database configuration values are undefined. Exiting.");
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
        LOG.info("DbInteractions: Creating connection pool with the following configuration:");
        LOG.info("DbInteractions: driverClassName: {}", config.getDriverClassName());
        LOG.info("DbInteractions: dbUrl: {}", dbConfig.getDbUrl());
        LOG.info("DbInteractions: dbUsername: {}", dbConfig.getDbUsername());
        LOG.info("DbInteractions: connectionTimeout: {}", config.getConnectionTimeout());
        LOG.info("DbInteractions: maximumPoolSize: {}", config.getMaximumPoolSize());

        dataSource = new HikariDataSource(config);
        LOG.info("DbInteractions: Successfully initialized connection pool");
    }
}