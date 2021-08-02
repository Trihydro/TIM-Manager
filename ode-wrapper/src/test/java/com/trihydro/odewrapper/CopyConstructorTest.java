package com.trihydro.odewrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.math.BigDecimal;
import java.util.Arrays;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trihydro.library.model.Buffer;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.WydotTim;
import com.trihydro.library.model.WydotTimRw;
import com.trihydro.odewrapper.model.WydotTimCc;
import com.trihydro.odewrapper.model.WydotTimIncident;
import com.trihydro.odewrapper.model.WydotTimParking;
import com.trihydro.odewrapper.model.WydotTimRc;
import com.trihydro.odewrapper.model.WydotTimVsl;

import org.junit.Test;

public class CopyConstructorTest {
    private Gson gson;

    public CopyConstructorTest() {
        GsonBuilder builder = new GsonBuilder();
        builder.serializeNulls();
        gson = builder.create();
    }

    public void verifyFullCopies(WydotTim o1, WydotTim o2) {
        var first = gson.toJson(o1);
        var second = gson.toJson(o2);

        // Ensure that each declared property was copied over
        assertEquals(first, second);

        // Ensure all properties were copied. If new properties are added,
        // they should serialize as null, indicating they weren't tested
        // and these tests need to be updated.
        assertFalse("Object contains null values", first.contains("null"));
        assertFalse("Object contains null values", second.contains("null"));
    }

    private void setBaseClassProps(WydotTim o) {
        o.setDirection("B");
        o.setStartPoint(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(1)));
        o.setEndPoint(new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2)));
        o.setRoute("firstRoute");
        o.setItisCodes(Arrays.asList("1", "2"));
        o.setClientId("firstClientId");
    }

    @Test
    public void wydotTimCc_copy() {
        // Arrange
        var first = new WydotTimCc();
        setBaseClassProps(first);
        first.setSegment("segment");
        first.setAdvisory(new Integer[] { 3 });

        // Act
        var second = new WydotTimCc(first);
        var third = first.copy();

        // Assert
        verifyFullCopies(first, second);
        verifyFullCopies(first, third);
    }

    @Test
    public void wydotTimIncident_copy() {
        // Arrange
        var first = new WydotTimIncident();
        setBaseClassProps(first);
        first.setImpact("impact");
        first.setProblem("problem");
        first.setEffect("effect");
        first.setAction("action");
        first.setIncidentId("incidentId");
        first.setHighway("highway");
        first.setSchedStart("schedStart");
        first.setSchedEnd("schedEnd");
        first.setPk(Integer.valueOf(1));
        first.setProblemOtherText("otherText");
        // clientId, route, itisCodes, endPoint set with base props

        // Act
        var second = new WydotTimIncident(first);
        var third = first.copy();

        // Assert
        verifyFullCopies(first, second);
        verifyFullCopies(first, third);
    }

    @Test
    public void wydotTimParking_copy() {
        // Arrange
        var first = new WydotTimParking();
        setBaseClassProps(first);
        first.setMileMarker(Double.valueOf(3));
        first.setAvailability(Integer.valueOf(4));
        first.setExit("exit");
        // itisCodes, endPoint set with base props

        // Act
        var second = new WydotTimParking(first);
        var third = first.copy();

        // Assert
        verifyFullCopies(first, second);
        verifyFullCopies(first, third);
    }

    @Test
    public void wydotTimRc_copy() {
        // Arrange
        var first = new WydotTimRc();
        setBaseClassProps(first);
        first.setRoadCode("roadCode");
        first.setAdvisory(new Integer[] { 3 });
        first.setSegment("segment");
        // clientId, itisCodes set with base props

        // Act
        var second = new WydotTimRc(first);
        var third = first.copy();

        // Assert
        verifyFullCopies(first, second);
        verifyFullCopies(first, third);
    }

    @Test
    public void wydotTimVsl_copy() {
        // Arrange
        var first = new WydotTimVsl();
        setBaseClassProps(first);
        first.setSpeed(Integer.valueOf(3));
        first.setDeviceId("deviceId");

        // Act
        var second = new WydotTimVsl(first);
        var third = first.copy();

        // Assert
        verifyFullCopies(first, second);
        verifyFullCopies(first, third);
    }

    @Test
    public void wydotTimRw_copy() {
        // Arrange
        var first = new WydotTimRw();
        setBaseClassProps(first);
        first.setId("id");
        first.setAction("action");
        first.setSchedStart("schedStart");
        first.setSchedEnd("schedEnd");
        first.setHighway("highway");
        first.setProjectKey(Integer.valueOf(3));

        var buffer = new Buffer();
        buffer.setAction("action");
        buffer.setDistance(Double.valueOf(4));
        first.setBuffers(Arrays.asList(buffer));

        first.setAdvisory(new Integer[] { 5 });

        // Act
        var second = new WydotTimRw(first);
        var third = first.copy();

        // Assert
        verifyFullCopies(first, second);
        verifyFullCopies(first, third);
    }
}