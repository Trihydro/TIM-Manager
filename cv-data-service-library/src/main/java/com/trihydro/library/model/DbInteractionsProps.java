package com.trihydro.library.model;

public interface DbInteractionsProps {
    String getDbUrl();
    String getDbUsername();
    String getDbPassword();

    int getMaximumPoolSize();
    int getConnectionTimeout();

    String[] getAlertAddresses();
    String getFromEmail();
    String getMailHost();
    int getMailPort();
}