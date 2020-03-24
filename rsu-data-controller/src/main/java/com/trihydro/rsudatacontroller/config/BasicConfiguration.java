package com.trihydro.rsudatacontroller.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")
public class BasicConfiguration {
    private int snmpRetries;
    private int snmpTimeoutSeconds;
    private String snmpUserName;
    private String snmpAuthPassphrase;
    private String snmpAuthProtocol;
    private String snmpSecurityLevel;

    public int getSnmpRetries() {
        return snmpRetries;
    }

    public void setSnmpRetries(int snmpRetries) {
        this.snmpRetries = snmpRetries;
    }

    public int getSnmpTimeoutSeconds() {
        return snmpTimeoutSeconds;
    }

    public void setSnmpTimeoutSeconds(int snmpTimeoutSeconds) {
        this.snmpTimeoutSeconds = snmpTimeoutSeconds;
    }

    public String getSnmpUserName() {
        return snmpUserName;
    }

    public void setSnmpUserName(String snmpUserName) {
        this.snmpUserName = snmpUserName;
    }

    public String getSnmpAuthPassphrase() {
        return snmpAuthPassphrase;
    }

    public void setSnmpAuthPassphrase(String snmpAuthPassphrase) {
        this.snmpAuthPassphrase = snmpAuthPassphrase;
    }

    public String getSnmpAuthProtocol() {
        return snmpAuthProtocol;
    }

    public void setSnmpAuthProtocol(String snmpAuthProtocol) {
        this.snmpAuthProtocol = snmpAuthProtocol;
    }

    public String getSnmpSecurityLevel() {
        return snmpSecurityLevel;
    }
    
    public void setSnmpSecurityLevel(String snmpSecurityLevel) {
        this.snmpSecurityLevel = snmpSecurityLevel;
    }
}