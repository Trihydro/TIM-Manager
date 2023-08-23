package com.trihydro.cvdatacontroller.model;

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.EmailHelper;
import com.trihydro.library.helpers.JavaMailSenderImplProvider;
import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.DbInteractionsProps;
import com.trihydro.library.model.EmailProps;
import com.trihydro.library.tables.LoggingTables;
import com.trihydro.library.tables.TimDbTables;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")
@Import({ TimDbTables.class, SQLNullHandler.class, Utility.class, EmailHelper.class,
        JavaMailSenderImplProvider.class, LoggingTables.class, DbInteractions.class })
public class DataControllerConfigProperties implements DbInteractionsProps, EmailProps {
    private String dataSourceClassName;    
    private String dbUsername;
    private String dbPassword;
    private String dbName;
    private int dbPort;
    private String dbServer;

    private String[] alertAddresses;
    private String fromEmail;
    private String environmentName;

    private String mailHost;
    private int mailPort;

    private int poolSize;

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

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String[] getAlertAddresses() {
        return alertAddresses;
    }

    public void setAlertAddresses(String[] alertAddresses) {
        this.alertAddresses = alertAddresses;
    }

    public String getDataSourceClassName() {
        return dataSourceClassName;
    }

    public void setDataSourceClassName(String dataSourceClassName) {
        this.dataSourceClassName = dataSourceClassName;
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

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbName() {
        return dbName;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    public void setDbServer(String dbServer) {
        this.dbServer = dbServer;
    }

    public String getDbServer() {
        return dbServer;
    }
}