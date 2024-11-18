package com.trihydro.library.models;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.trihydro.library.model.tmdd.DateTimeZone;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DateTimeZoneTest {
    private DateTimeFormatter format;

    @BeforeEach
    public void setup() {
        format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    @Test
    public void asDateTimeString_mountain() {
        // Arrange
        ZoneOffset offset = ZoneOffset.ofHours(-6);
        ZonedDateTime date = ZonedDateTime.of(2020, 01, 01, 12, 30, 0, 0, offset);

        DateTimeZone uut = new DateTimeZone();
        uut.setDate("20200101");
        uut.setTime("123000");
        uut.setOffset("-0600");

        // Act
        String result = uut.asDateTimeString();

        // Assert
        Assertions.assertEquals(date.format(format), result);
    }

    @Test
    public void asDateTimeString_positiveTimezone() {
        // Arrange
        ZoneOffset offset = ZoneOffset.ofHours(6);
        ZonedDateTime date = ZonedDateTime.of(2020, 02, 02, 1, 45, 0, 0, offset);

        DateTimeZone uut = new DateTimeZone();
        uut.setDate("20200202");
        uut.setTime("014500");
        uut.setOffset("0600");

        // Act
        String result = uut.asDateTimeString();

        // Assert
        Assertions.assertEquals(date.format(format), result);
    }

    @Test
    public void asDateTimeString_invalid() {
        // Arrange
        DateTimeZone uut = new DateTimeZone();

        // Act
        String result = uut.asDateTimeString();

        // Assert
        Assertions.assertNull(result);
    }
}