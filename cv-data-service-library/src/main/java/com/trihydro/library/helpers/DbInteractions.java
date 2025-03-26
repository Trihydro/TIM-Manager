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

/**
 * The DbInteractions class is responsible for managing database interactions such as
 * obtaining connections from a connection pool, executing SQL operations, and logging results.
 * It facilitates configurations related to database connectivity and supports email alerts
 * in case of operation failures.
 * <p>
 * This class is annotated with {@code @Component} for Spring's component scanning and
 * uses HikariCP for connection pooling. It depends on {@link DbInteractionsProps} for
 * database configuration properties and {@link EmailHelper} for sending alert emails.
 */
@Component
@Slf4j
public class DbInteractions {
    private static HikariDataSource dataSource;

    private final DbInteractionsProps dbConfig;
    private final EmailHelper emailHelper;

    @Autowired
    public DbInteractions(DbInteractionsProps props, EmailHelper emailHelper) {
        this.dbConfig = props;
        this.emailHelper = emailHelper;
        log.info("A new DbInteractions instance has been created.");
        validateDbConfig();
    }

    /**
     * Retrieves a connection from the connection pool. If the connection pool
     * has not been initialized, it initializes the primary connection pool
     * before retrieving a connection.
     * <p>
     * In case of a failure in obtaining a connection, an email alert is sent
     * to the configured alert addresses and a {@link SQLException} is thrown.
     *
     * @return A {@link Connection} instance from the connection pool.
     * @throws SQLException If unable to retrieve a connection from the pool.
     */
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

    /**
     * Executes the given PreparedStatement to perform an update or delete operation on the database.
     *
     * @param preparedStatement the PreparedStatement to execute for the update or delete operation
     * @return true if the operation affected at least one row in the database, false otherwise
     */
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

    /**
     * Executes a delete operation using the provided PreparedStatement. The method
     * returns a boolean indicating whether the operation was successful. If an
     * exception occurs during execution, it is logged, and the method returns false.
     *
     * @param preparedStatement the PreparedStatement used to perform the delete operation
     * @return true if the delete operation was successful, false if it failed
     */
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

    /**
     * Executes the given prepared statement, logs the generated key if available, and returns the key.
     *
     * @param preparedStatement the prepared statement to be executed
     * @param type              the type of operation or entity being logged, for logging purposes
     * @return the generated key as a Long if the execution is successful and a key is generated, otherwise null
     */
    public Long executeAndLog(PreparedStatement preparedStatement, String type) {
        Long id = null;
        try {
            if (preparedStatement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys != null && generatedKeys.next()) {
                        id = generatedKeys.getLong(1);
                        log.trace("------ Generated {} {} --------------", type, id);
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error in executeAndLog", e);
        }
        return id;
    }

    /**
     * Validates the database configuration settings and ensures all necessary
     * values are defined. If the configuration is invalid or incomplete, the
     * application will log an error and terminate.
     * <p>
     * Key actions performed by this method:
     * - Checks if the `dataSource` is null, indicating a need to validate the configuration.
     * - Sets the default application time zone to "America/Denver."
     * - Verifies that all essential database configuration values (`dbUrl`, `dbUsername`,
     * `dbPassword`, `maximumPoolSize`, and `connectionTimeout`) are defined and non-zero.
     * - Logs an error and exits the program if any required configuration value is missing.
     */
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

    /**
     * Initializes the primary connection pool using HikariCP for managing database connections.
     * Configures the connection pool with properties retrieved from the database configuration.
     * Logs the connection pool configuration details during initialization.
     * On successful initialization, assigns the configured {@code HikariDataSource} to {@code dataSource}.
     * <p>
     * This method is private and intended only for internal use by the class to create and configure
     * the connection pool required for database operations.
     * <p>
     * The connection pool is configured with the following settings:
     * - Driver class name
     * - JDBC URL
     * - Database username
     * - Database password
     * - Connection timeout
     * - Maximum pool size
     * <p>
     * During the initialization process, any logging information regarding the
     * pool's configuration is output through the configured logger.
     */
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