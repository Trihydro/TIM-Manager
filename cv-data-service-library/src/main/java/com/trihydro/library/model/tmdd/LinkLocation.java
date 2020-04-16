package com.trihydro.library.model.tmdd;

public class LinkLocation {
    private String linkOwnership;
    private String linkDesignator;
    private PointOnLink primaryLocation;
    private PointOnLink secondaryLocation;
    private String linkDirection;

    public String getLinkOwnership() {
        return linkOwnership;
    }

    public void setLinkOwnership(String linkOwnership) {
        this.linkOwnership = linkOwnership;
    }

    public String getLinkDesignator() {
        return linkDesignator;
    }

    public void setLinkDesignator(String linkDesignator) {
        this.linkDesignator = linkDesignator;
    }

    public PointOnLink getPrimaryLocation() {
        return primaryLocation;
    }

    public void setPrimaryLocation(PointOnLink primaryLocation) {
        this.primaryLocation = primaryLocation;
    }

    public PointOnLink getSecondaryLocation() {
        return secondaryLocation;
    }

    public void setSecondaryLocation(PointOnLink secondaryLocation) {
        this.secondaryLocation = secondaryLocation;
    }

    public String getLinkDirection() {
        return linkDirection;
    }

    public void setLinkDirection(String linkDirection) {
        this.linkDirection = linkDirection;
    }
}
