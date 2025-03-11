package com.trihydro.library.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WydotTim {

    @ApiModelProperty(value = "Expected values are I, D, B", required = true)
    private String direction;
    @ApiModelProperty(required = true)
    private Coordinate startPoint;
    @ApiModelProperty(required = true)
    private Coordinate endPoint;
    @ApiModelProperty(value = "The common name for the selected route", required = true)
    private String route;
    @ApiModelProperty(required = true)
    private List<String> itisCodes;
    @ApiModelProperty(required = true)
    private String clientId;

    public WydotTim() {

    }

    public WydotTim(WydotTim o) {
        this.direction = o.direction;
        if (o.startPoint != null) {
            this.startPoint =
                new Coordinate(o.startPoint.getLatitude(), o.startPoint.getLongitude());
        }
        if (o.endPoint != null) {
            this.endPoint = new Coordinate(o.endPoint.getLatitude(), o.endPoint.getLongitude());
        }
        this.route = o.route;
        if (o.itisCodes != null) {
            this.itisCodes = new ArrayList<>(o.itisCodes);
        }
        this.clientId = o.clientId;
    }

    public WydotTim(TimUpdateModel aTim) {
        setClientId(aTim.getClientId());
        setDirection(aTim.getDirection());
        setStartPoint(aTim.getStartPoint());
        setEndPoint(aTim.getEndPoint());
        // note: route and itisCodes are not set in this constructor
    }

    public WydotTim copy() {
        return new WydotTim(this);
    }

}