package com.trihydro.library.model.tmdd;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeZone {
    private String date;
    private String time;
    private String offset;

    private static final DateTimeFormatter dateTimeFormatter;
    static {
        dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public String asDateTimeString() {
        if (date.length() != 8 || time.length() != 6) {
            return null;
        }

        int oHour;
        int oMin;

        if (offset.length() == 5) {
            oHour = Integer.parseInt(offset.substring(0, 3));
            oMin = Integer.parseInt(offset.substring(3, 5));
        } else if (offset.length() == 4) {
            oHour = Integer.parseInt(offset.substring(0, 2));
            oMin = Integer.parseInt(offset.substring(2, 4));
        } else {
            return null;
        }

        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(4, 6));
        int day = Integer.parseInt(date.substring(6, 8));

        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(2, 4));

        ZoneOffset offset = ZoneOffset.ofHoursMinutes(oHour, oMin);
        // We only care about resolution down to the minute
        ZonedDateTime date = ZonedDateTime.of(year, month, day, hour, minute, 0, 0, offset);

        return date.format(dateTimeFormatter);
    }
}
