package com.trihydro.library.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.trihydro.library.model.CountyRoadSegment;

public class CountyRoadSegmentTest {
    private CountyRoadSegment allFalseCountyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, false, false, false, false);
    private CountyRoadSegment closedCountyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, true, false, false, false);
    private CountyRoadSegment c2lhpvCountyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, false, true, false, false);
    private CountyRoadSegment loctCountyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, false, false, true, false);
    private CountyRoadSegment nttCountyRoadSegment = new CountyRoadSegment(1, "example common name", 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, false, false, false, true);
    
    @Test
    public void testInitialization() {
        // prepare
        int countyRoadId = 1;
        String commonName = "example common name";
        Double mFrom = 1.0;
        Double mTo = 2.0;
        Double xFrom = 3.0;
        Double yFrom = 4.0;
        Double xTo = 5.0;
        Double yTo = 6.0;
        boolean closed = true;
        boolean c2lhpv = true;
        boolean loct = true;
        boolean ntt = true;

        // execute
        CountyRoadSegment countyRoadSegment = new CountyRoadSegment(countyRoadId, commonName, mFrom, mTo, xFrom, 
                        yFrom, xTo, yTo, closed, c2lhpv, loct, ntt);

        // verify
        assertNotNull(countyRoadSegment);
        assertEquals(countyRoadId, countyRoadSegment.getId());
        assertEquals(commonName, countyRoadSegment.getCommonName());
        assertEquals(mFrom, countyRoadSegment.getMFrom());
        assertEquals(mTo, countyRoadSegment.getMTo());
        assertEquals(xFrom, countyRoadSegment.getXFrom());
        assertEquals(yFrom, countyRoadSegment.getYFrom());
        assertEquals(xTo, countyRoadSegment.getXTo());
        assertEquals(yTo, countyRoadSegment.getYTo());
        assertEquals(closed, countyRoadSegment.isClosed());
        assertEquals(c2lhpv, countyRoadSegment.isC2lhpv());
        assertEquals(loct, countyRoadSegment.isLoct());
        assertEquals(ntt, countyRoadSegment.isNtt());
    }

    @Test
    public void testHasOneOrMoreCondition_Closed_True() {
        // execute
        boolean hasOneOrMoreCondition = closedCountyRoadSegment.hasOneOrMoreCondition();

        // verify
        assertEquals(true, hasOneOrMoreCondition);
    }

    @Test
    public void testHasOneOrMoreCondition_Closed_False() {
        // execute
        boolean hasOneOrMoreCondition = allFalseCountyRoadSegment.hasOneOrMoreCondition();

        // verify
        assertEquals(false, hasOneOrMoreCondition);
    }

    @Test
    public void testHasOneOrMoreCondition_C2lhpv_True() {
        // execute
        boolean hasOneOrMoreCondition = c2lhpvCountyRoadSegment.hasOneOrMoreCondition();

        // verify
        assertEquals(true, hasOneOrMoreCondition);
    }

    @Test
    public void testHasOneOrMoreCondition_C2lhpv_False() {
        // execute
        boolean hasOneOrMoreCondition = allFalseCountyRoadSegment.hasOneOrMoreCondition();

        // verify
        assertEquals(false, hasOneOrMoreCondition);
    }

    @Test
    public void testHasOneOrMoreCondition_Loct_True() {
        // execute
        boolean hasOneOrMoreCondition = loctCountyRoadSegment.hasOneOrMoreCondition();

        // verify
        assertEquals(true, hasOneOrMoreCondition);
    }

    @Test
    public void testHasOneOrMoreCondition_Loct_False() {
        // execute
        boolean hasOneOrMoreCondition = allFalseCountyRoadSegment.hasOneOrMoreCondition();

        // verify
        assertEquals(false, hasOneOrMoreCondition);
    }

    @Test
    public void testHasOneOrMoreCondition_Ntt_True() {
        // execute
        boolean hasOneOrMoreCondition = nttCountyRoadSegment.hasOneOrMoreCondition();

        // verify
        assertEquals(true, hasOneOrMoreCondition);
    }

    @Test
    public void testHasOneOrMoreCondition_Ntt_False() {
        // execute
        boolean hasOneOrMoreCondition = allFalseCountyRoadSegment.hasOneOrMoreCondition();

        // verify
        assertEquals(false, hasOneOrMoreCondition);
    }

    @Test
    public void testHasOneOrMoreCondition_LoctOrNtt_True() {
        // execute
        boolean hasOneOrMoreCondition = loctCountyRoadSegment.hasOneOrMoreCondition();

        // verify
        assertEquals(true, hasOneOrMoreCondition);
    }

    @Test
    public void testHasOneOrMoreCondition_LoctOrNtt_False() {
        // execute
        boolean hasOneOrMoreCondition = allFalseCountyRoadSegment.hasOneOrMoreCondition();

        // verify
        assertEquals(false, hasOneOrMoreCondition);
    }
}