package com.trihydro.odewrapper.model;

import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.WydotTim;

import io.swagger.annotations.ApiModelProperty;

public class WydotTimBowr extends WydotTim {

    @ApiModelProperty(value = "Expected value is BlowOverWeightRestriction", required = true)
	private String type;
    
	@ApiModelProperty(required = false)
	private String startDateTime;

    @ApiModelProperty(required = false)
	private String endDateTime;

    @ApiModelProperty(required = true)
	private int data;

    public WydotTimBowr() {

    }

    public WydotTimBowr(WydotTimBowr o) {
        super(o);
        this.type = o.type;
        this.startDateTime = o.startDateTime;
        this.endDateTime = o.endDateTime;
        this.data = o.data;
    }

    public WydotTimBowr copy() {
        return new WydotTimBowr(this);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }
}