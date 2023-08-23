package com.trihydro.library.helpers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimeZone;

import com.trihydro.library.model.DbInteractionsProps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DbInteractions {
    private static HikariDataSource hds;
    private HikariConfig config;

    protected DbInteractionsProps dbConfig;
    protected Utility utility;
    private EmailHelper emailHelper;

    @Autowired
    public void InjectDependencies(DbInteractionsProps props, Utility _utility, EmailHelper _emailHelper) {
        dbConfig = props;
        utility = _utility;
        emailHelper = _emailHelper;
        utility.logWithDate("DbInteractions: Injecting dependencies");
        initHDS();
    }

    private void initHDS() {
        if (hds == null) {
            TimeZone timeZone = TimeZone.getTimeZone("America/Denver");
            TimeZone.setDefault(timeZone);

            Properties props = new Properties();
            props.setProperty("dataSourceClassName", dbConfig.getDataSourceClassName());
            props.setProperty("dataSource.user", dbConfig.getDbUsername());
            props.setProperty("dataSource.password", dbConfig.getDbPassword());
            props.setProperty("dataSource.databaseName", dbConfig.getDbName());
            props.setProperty("dataSource.portNumber", String.valueOf(dbConfig.getDbPort()));
            props.setProperty("dataSource.serverName", dbConfig.getDbServer());
            props.put("dataSource.logWriter", new java.io.PrintWriter(System.out));
            config = new HikariConfig(props);

            // log the creation of the connection pool and properties
            utility.logWithDate("DbInteractions: Creating connection pool with the following config:");
            utility.logWithDate("                - dataSourceClassName: " + config.getDataSourceClassName());
            utility.logWithDate("                - dataSource.user: " + config.getDataSourceProperties().getProperty("user"));
            utility.logWithDate("                - dataSource.password: " + "******");
            utility.logWithDate("                - dataSource.databaseName: " + config.getDataSourceProperties().getProperty("databaseName"));
            utility.logWithDate("                - dataSource.portNumber: " + config.getDataSourceProperties().getProperty("portNumber"));
            utility.logWithDate("                - dataSource.serverName: " + config.getDataSourceProperties().getProperty("serverName"));

            hds = new HikariDataSource(config);
        }
    }

    public Connection getConnectionPool() throws SQLException {
        // create pool if not already done
        initHDS();

        // return a connection
        try {
            return hds.getConnection();
        } catch (SQLException ex) {
            String body = "Failed attempting to open a connection to ";
            body += dbConfig.getDbServer();
            body += ". <br/>Exception message: ";
            body += ex.getMessage();
            body += "<br/>Stacktrace: ";
            body += ExceptionUtils.getStackTrace(ex);
            try {
                emailHelper.SendEmail(dbConfig.getAlertAddresses(), "Failed To Get Connection", body);
            } catch (Exception exception) {
                utility.logWithDate("Failed to open connection to " + dbConfig.getDbServer()
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