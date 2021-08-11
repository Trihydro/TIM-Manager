package com.trihydro.library.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class WydotTimRw extends WydotTim {

    @ApiModelProperty(hidden = true)
    private String action;
    @ApiModelProperty(hidden = true)
    private Integer[] advisory;
    @ApiModelProperty(value = "The scheduled start time for the generated TIM. Should follow the format: yyyy-MM-dd")
    private String schedStart;
    @ApiModelProperty(value = "The scheduled end time for the generated TIM. Should follow the format: yyyy-MM-dd")
    private String schedEnd;
    @ApiModelProperty(value = "The highway to associate with the generated TIM", required = true)
    private String highway;
    @ApiModelProperty(value = "Used as the client id", required = true)
    private String id;
    @ApiModelProperty(required = true)
    private Integer projectKey;
    private List<Buffer> buffers;

    @ApiModelProperty(hidden = true)
    private transient String route;

    public WydotTimRw() {

    }

    public WydotTimRw(WydotTimRw o) {
        super(o);
        this.id = o.id;
        this.action = o.action;
        this.schedStart = o.schedStart;
        this.schedEnd = o.schedEnd;
        this.highway = o.highway;
        this.projectKey = o.projectKey;
        if (o.buffers != null) {
            this.buffers = new ArrayList<>(o.buffers.size());
            for(var obj : o.buffers) {
                this.buffers.add(new Buffer(obj));
            }
        }
        if (o.advisory != null)
            this.advisory = o.advisory.clone();
    }

    @Override
    public WydotTimRw copy() {
        return new WydotTimRw(this);
    }

    public Integer[] getAdvisory() {
        return this.advisory;
    }

    public void setAdvisory(Integer[] advisory) {
        this.advisory = advisory;
    }

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSchedStart() {
        return this.schedStart;
    }

    public void setSchedStart(String schedStart) {
        this.schedStart = schedStart;
    }

    public String getSchedEnd() {
        return this.schedEnd;
    }

    public void setSchedEnd(String schedEnd) {
        this.schedEnd = schedEnd;
    }

    public String getHighway() {
        return this.highway;
    }

    public void setHighway(String highway) {
        this.highway = highway;
    }

    public List<Buffer> getBuffers() {
        return this.buffers;
    }

    public void setBuffers(List<Buffer> buffers) {
        this.buffers = buffers;
    }

    public Integer getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(Integer projectKey) {
        this.projectKey = projectKey;
    }
}