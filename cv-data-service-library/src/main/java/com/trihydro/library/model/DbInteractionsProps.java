package com.trihydro.library.model;

public interface DbInteractionsProps {
    String getDbUrl();
    
    String getDbUsername();

    String getDbPassword();

    String[] getAlertAddresses();

    int getMailPort();

    String getMailHost();

    String getFromEmail();
}