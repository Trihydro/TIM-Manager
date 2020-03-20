package com.trihydro.cvdatacontroller.model;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Milepost {
    @Id
    @GeneratedValue
    private Long id;

    private Double Latitude;
    private Double Milepost;
    private String Direction;
    private Double Longitude;
    private String CommonName;

    public Long getId() {
		return id;
    }
    
    public Double getLatitude() {
        return Latitude;
    }

    public String getCommonName() {
        return CommonName;
    }

    public void setCommonName(String commonName) {
        this.CommonName = commonName;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        this.Longitude = longitude;
    }

    public String getDirection() {
        return Direction;
    }

    public void setDirection(String direction) {
        this.Direction = direction;
    }

    public Double getMilepost() {
        return Milepost;
    }

    public void setMilepost(Double milepost) {
        this.Milepost = milepost;
    }

    public void setLatitude(Double latitude) {
        this.Latitude = latitude;
    }

}