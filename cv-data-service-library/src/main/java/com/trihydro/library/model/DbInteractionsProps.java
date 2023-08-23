package com.trihydro.library.model;

public interface DbInteractionsProps {
    String getDbUsername();

    String getDbPassword();

    String getDbName();

    String getDataSourceClassName();

    int getDbPort();

    String getDbServer();

    String[] getAlertAddresses();

    int getMailPort();

    String getMailHost();

    String getFromEmail();
}