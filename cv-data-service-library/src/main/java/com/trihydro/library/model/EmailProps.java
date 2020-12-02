package com.trihydro.library.model;

public interface EmailProps {

    String[] getAlertAddresses();

    public int getMailPort();

    public String getMailHost();

    public String getFromEmail();
}
