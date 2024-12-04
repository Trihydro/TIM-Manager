package com.trihydro.library.model;

import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.service.CascadeService;

/**
 * A county road segment is a segment of a county road that has a number of conditions associated with it.
 */
public class CountyRoadSegment {
    private int countyRoadId;
    private String commonName;
    private Double mFrom;
    private Double mTo;
    private Double xFrom;
    private Double yFrom;
    private Double xTo;
    private Double yTo;
    private boolean closed;
    private boolean c2lhpv;
    private boolean loct;
    private boolean ntt;

    public CountyRoadSegment() {
        
    }

    public CountyRoadSegment(int countyRoadId, String commonName, Double mFrom, Double mTo, Double xFrom, 
                       Double yFrom, Double xTo, Double yTo, boolean closed, boolean c2lhpv, boolean loct, boolean ntt) {
        this.countyRoadId = countyRoadId;
        this.commonName = commonName;
        this.mFrom = mFrom;
        this.mTo = mTo;
        this.xFrom = xFrom;
        this.yFrom = yFrom;
        this.xTo = xTo;
        this.yTo = yTo;
        this.closed = closed;
        this.c2lhpv = c2lhpv;
        this.loct = loct;
        this.ntt = ntt;
    }

    public int getId() {
        return countyRoadId;
    }

    public String getCommonName() {
        return commonName;
    }

    public Double getMFrom() {
        return mFrom;
    }

    public Double getMTo() {
        return mTo;
    }

    public Double getXFrom() {
        return xFrom;
    }

    public Double getYFrom() {
        return yFrom;
    }

    public Double getXTo() {
        return xTo;
    }

    public Double getYTo() {
        return yTo;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isC2lhpv() {
        return c2lhpv;
    }

    public boolean isLoct() {
        return loct;
    }

    public boolean isNtt() {
        return ntt;
    }

    public boolean hasOneOrMoreCondition() {
        return closed || c2lhpv || loct || ntt;
    }

    public List<Integer> toITISCodes() {
        List<Integer> itisCodes = new ArrayList<>();
        if (closed) {
            itisCodes.add(Integer.parseInt(CascadeService.closedItisCode));
        }
        if (c2lhpv) {
            itisCodes.add(Integer.parseInt(CascadeService.c2lhpvItisCode));
        }
        if (loct) {
            itisCodes.add(Integer.parseInt(CascadeService.loctItisCode));
        }
        if (ntt) {
            itisCodes.add(Integer.parseInt(CascadeService.nttItisCode));
        }
        return itisCodes;
    }
}