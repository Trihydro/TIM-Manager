package com.trihydro.library.model;

/**
 * ActiveRsuTimQueryModel
 */
public class ActiveRsuTimQueryModel {
    String direction;
    String clientId;
    String ipv4;

    public ActiveRsuTimQueryModel() {
    }

    public ActiveRsuTimQueryModel(String direction, String clientId, String ipv4) {
        this.direction = direction;
        this.clientId = clientId;
        this.ipv4 = ipv4;
    }

    public String getDirection() {
        return this.direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getIpv4() {
        return this.ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4;
    }
}