package com.trihydro.odewrapper.model;

import com.trihydro.library.model.WydotTim;

public class WydotTimCc extends WydotTim {

    private String segment;
    private Integer[] advisory;

    public WydotTimCc() {

    }

    public WydotTimCc(WydotTimCc o) {
        super(o);
        this.segment = o.segment;
        if(o.advisory != null)
            this.advisory = o.advisory.clone();
    }

    @Override
    public WydotTimCc copy() {
        return new WydotTimCc(this);
    }

    public String getSegment() {
        return this.segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public Integer[] getAdvisory() {
        return this.advisory;
    }

    public void setAdvisory(Integer[] advisory) {
        this.advisory = advisory;
    }
}