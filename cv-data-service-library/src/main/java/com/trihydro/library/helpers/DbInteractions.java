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
    private static HikariDataSource dataSource;

    protected DbInteractionsProps dbConfig;
    protected Utility utility;
    private EmailHelper emailHelper;

    @Autowired
    public void InjectDependencies(DbInteractionsProps props, Utility _utility, EmailHelper _emailHelper) {
        dbConfig = props;
        utility = _utility;
        emailHelper = _emailHelper;
        utility.logWithDate("DbInteractions: Injecting dependencies");
        initDataSource();
    }   

    private void initDataSource() {
        if (dataSource == null) {
            TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
            TimeZone.setDefault(timeZone);

            // check dbconfig for null values
            if (dbConfig.getDbUrl() == null ||
                dbConfig.getDbUsername() == null ||
                dbConfig.getDbPassword() == null ||
                dbConfig.getMaximumPoolSize() == 0 ||
                dbConfig.getConnectionTimeout() == 0) {
                utility.logWithDate("DbInteractions: One or more database configuration values are undefined. Exiting.");
                System.exit(1);
            }

            // initialize connection pool
            try {
                HikariConfig config = new HikariConfig();
                config.setDriverClassName("org.postgresql.Driver");
                config.setJdbcUrl(dbConfig.getDbUrl());
                config.setUsername(dbConfig.getDbUsername());
                config.setPassword(dbConfig.getDbPassword());
                config.setConnectionTimeout(dbConfig.getConnectionTimeout());
                config.setMaximumPoolSize(dbConfig.getMaximumPoolSize());

                // log the configuration of the connection pool
                utility.logWithDate("DbInteractions: Creating connection pool with the following configuration:");
                utility.logWithDate("DbInteractions: dbUrl: " + dbConfig.getDbUrl());
                utility.logWithDate("DbInteractions: dbUsername: " + dbConfig.getDbUsername());
                utility.logWithDate("DbInteractions: connectionTimeout: " + config.getConnectionTimeout());
                utility.logWithDate("DbInteractions: maximumPoolSize: " + config.getMaximumPoolSize());
                utility.logWithDate("DbInteractions: driverClassName: " + config.getDriverClassName());

                dataSource = new HikariDataSource(config);
                utility.logWithDate("DbInteractions: Successfully initialized connection pool");
            } catch (Exception e) {
                utility.logWithDate("DbInteractions: Failed to initialize connection pool due to unexpected exception:\n\n\"" + e.getMessage() + "\"\n");
                // e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public Connection getConnectionPool() throws SQLException {
        // create pool if not already done
        initDataSource();

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