package com.trihydro.cvdatacontroller.model;

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.DbInteractionsProps;
import com.trihydro.library.tables.LoggingTables;
import com.trihydro.library.tables.TimOracleTables;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")
@Import({ TimOracleTables.class, SQLNullHandler.class, Utility.class, EmailHelper.class,
        JavaMailSenderImplProvider.class, LoggingTables.class, DbInteractions.class })
public class DataControllerConfigProperties implements DbInteractionsProps {
    private String dbDriver;
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    private String[] alertAddresses;
    private String fromEmail;

    private String mailHost;
    private int mailPort;

    private int poolSize;

    public String getDbDriver() {
        return dbDriver;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getMailPort() {
        return mailPort;
    }

    public void setMailPort(int mailPort) {
        this.mailPort = mailPort;
    }

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String[] getAlertAddresses() {
        return alertAddresses;
    }

    public void setAlertAddresses(String[] alertAddresses) {
        this.alertAddresses = alertAddresses;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public void setDbDriver(String dbDriver) {
        this.dbDriver = dbDriver;
    }
}