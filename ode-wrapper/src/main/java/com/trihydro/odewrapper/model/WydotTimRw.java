package com.trihydro.odewrapper.model;

import java.util.List;

public class WydotTimRw extends WydotTim {

    private String id;
    private String action;
    private String schedStart;
    private String schedEnd;
    private String highway;
    private String surface;
    private List<Buffer> buffers;
    private Integer[] advisory;

    @Override
    public WydotTimRw clone() throws CloneNotSupportedException {
        return (WydotTimRw) super.clone();
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

    public String getSurface() {
        return this.surface;
    }

    public void setSurface(String surface) {
        this.surface = surface;
    }

    public List<Buffer> getBuffers() {
        return this.buffers;
    }

    public void setBuffers(List<Buffer> buffers) {
        this.buffers = buffers;
    }
}