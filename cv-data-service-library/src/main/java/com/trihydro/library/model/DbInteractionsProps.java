package com.trihydro.library.model;

public interface DbInteractionsProps {
    String getDbUrl();
    String getDbUsername();
    String getDbPassword();

    String getDbUrlCountyRoads();
    String getDbUsernameCountyRoads();
    String getDbPasswordCountyRoads();
    
    int getMaximumPoolSize();
    int getConnectionTimeout();

    String[] getAlertAddresses();
    String getFromEmail();
    String getMailHost();
    int getMailPort();
}