package com.trihydro.library.model;

public interface DbInteractionsProps {
    String getDbUsername();

    String getDbPassword();

    String getDbUrl();

    String getDbDriver();

    int getPoolSize();

    String[] getAlertAddresses();

    int getMailPort();

    String getMailHost();

    String getFromEmail();
}