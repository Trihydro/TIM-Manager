package com.trihydro.loggerkafkaconsumer.app.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.SecurityResultCodeType;
import com.trihydro.loggerkafkaconsumer.config.LoggerConfiguration;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BaseService {

    private HikariDataSource hds = null;
    private HikariConfig config;
    private LoggerConfiguration dbConfig;
    private JavaMailSenderImplProvider mailProvider;
    private Utility utility;
    private EmailHelper emailHelper;

    private DateFormat utcFormatMilliSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private DateFormat utcFormatSec = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private DateFormat utcFormatMin = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    public DateFormat mstFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss.SSS a");

    @Autowired
    public void InjectDependencies(LoggerConfiguration _loggerConfiguration, JavaMailSenderImplProvider _mailProvider,
            Utility _utility, EmailHelper _emailHelper) {
        dbConfig = _loggerConfiguration;
        mailProvider = _mailProvider;
        utility = _utility;
        emailHelper = _emailHelper;
    }

    public Connection GetConnectionPool() throws SQLException {

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
            config.setMaximumPoolSize(dbConfig.getPoolSize());// formula: connections = ((core_count*2) +
                                                              // effective_spindle_count)
            // https://stackoverflow.com/questions/28987540/why-does-hikaricp-recommend-fixed-size-pool-for-better-performance
            // we have 8 cores and are limiting the container to run on a single 'drive'
            // which gives us 17 pool size. we round up here
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

    public Date convertDate(String incomingDate) {

        Date convertedDate = null;

        try {
            if (incomingDate != null) {
                if (incomingDate.contains("."))
                    convertedDate = utcFormatMilliSec.parse(incomingDate);
                else if (incomingDate.length() == 22)
                    convertedDate = utcFormatMin.parse(incomingDate);
                else
                    convertedDate = utcFormatSec.parse(incomingDate);
            }
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        return convertedDate;
    }

    public List<SecurityResultCodeType> GetSecurityResultCodeTypes() {
        SecurityResultCodeType securityResultCodeType = null;
        List<SecurityResultCodeType> securityResultCodeTypes = new ArrayList<SecurityResultCodeType>();
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;

        try {

            connection = GetConnectionPool();
            statement = connection.createStatement();
            rs = statement.executeQuery("select * from SECURITY_RESULT_CODE_TYPE");

            // convert to ActiveTim object
            while (rs.next()) {
                securityResultCodeType = new SecurityResultCodeType();
                securityResultCodeType.setSecurityResultCodeTypeId(rs.getInt("SECURITY_RESULT_CODE_TYPE_ID"));
                securityResultCodeType.setSecurityResultCodeType(rs.getString("SECURITY_RESULT_CODE_TYPE"));
                securityResultCodeTypes.add(securityResultCodeType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                // close prepared statement
                if (statement != null)
                    statement.close();
                // return connection back to pool
                if (connection != null)
                    connection.close();
                // close result set
                if (rs != null)
                    rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return securityResultCodeTypes;
    }
}