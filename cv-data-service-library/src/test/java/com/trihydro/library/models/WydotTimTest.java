package com.trihydro.library.models;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import java.util.Arrays;

import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.WydotTim;

import org.junit.Test;

public class WydotTimTest {
    @Test
    public void clone_deepCopy() throws CloneNotSupportedException {
        // Arrange
        var first = new WydotTim();
        first.setDirection("I");
        first.setStartPoint(new Coordinate(BigDecimal.valueOf(1), BigDecimal.valueOf(1)));
        first.setEndPoint(new Coordinate(BigDecimal.valueOf(2), BigDecimal.valueOf(2)));
        first.setRoute("firstRoute");
        first.setItisCodes(Arrays.asList("1", "2"));
        first.setClientId("firstClientId");

        // Act, Assert
        // After clone, values are the same
        var second = first.clone();

        assertEquals(first.getDirection(), second.getDirection());
        assertEquals(first.getRoute(), second.getRoute());
        assertEquals(first.getItisCodes().get(0), second.getItisCodes().get(0));
        assertEquals(first.getClientId(), second.getClientId());

        assertEquals(first.getStartPoint().getLatitude(), second.getStartPoint().getLatitude());
        assertEquals(first.getStartPoint().getLongitude(), second.getStartPoint().getLongitude());

        assertEquals(first.getEndPoint().getLatitude(), second.getEndPoint().getLatitude());
        assertEquals(first.getEndPoint().getLongitude(), second.getEndPoint().getLongitude());

        // Modify second
        second.setDirection("D");
        second.getStartPoint().setLatitude(null);
        second.getStartPoint().setLongitude(null);
        second.getEndPoint().setLatitude(null);
        second.getEndPoint().setLongitude(null);
        second.setRoute("secondRoute");
        second.getItisCodes().add("3");
        second.setClientId("secondClientId");

        // Verify first and second are distinct
        assertNotEquals(first.getDirection(), second.getDirection());
        assertNotEquals(first.getRoute(), second.getRoute());
        assertNotEquals(first.getItisCodes().size(), second.getItisCodes().size());
        assertNotEquals(first.getClientId(), second.getClientId());

        assertNotEquals(first.getStartPoint().getLatitude(), second.getStartPoint().getLatitude());
        assertNotEquals(first.getStartPoint().getLongitude(), second.getStartPoint().getLongitude());

        assertNotEquals(first.getEndPoint().getLatitude(), second.getEndPoint().getLatitude());
        assertNotEquals(first.getEndPoint().getLongitude(), second.getEndPoint().getLongitude());
    }
}
