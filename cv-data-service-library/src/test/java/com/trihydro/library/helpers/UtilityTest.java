package com.trihydro.library.helpers;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UtilityTest {

    @InjectMocks
    private Utility uut;

    @Test
    public void convertDate_min_SUCCESS() {
        // Arrange
        var date = "2020-10-28T14:53Z";

        // Act
        var convertedDate = uut.convertDate(date);

        // Assert
        Assertions.assertNotNull(convertedDate);
        Assertions.assertEquals(1603896780000l, convertedDate.getTime());
    }

    @Test
    public void convertDate_sec_SUCCESS() {
        // Arrange
        // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        var date = "2020-10-28T14:53:00Z";

        // Act
        var convertedDate = uut.convertDate(date);

        // Assert
        Assertions.assertNotNull(convertedDate);
        Assertions.assertEquals(1603896780000l, convertedDate.getTime());
    }

    @Test
    public void convertDate_milli_SUCCESS() {
        // Arrange
        // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        var date = "2020-10-28T14:53:00.123Z";

        // Act
        var convertedDate = uut.convertDate(date);

        // Assert
        Assertions.assertNotNull(convertedDate);
        Assertions.assertEquals(1603896780123l, convertedDate.getTime());
    }

    @Test
    public void convertDate_utcText_SUCCESS() {
        // Arrange
        // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        var date = "2020-02-10T17:00:00.000Z[UTC]";

        // Act
        var convertedDate = uut.convertDate(date);

        // Assert
        Assertions.assertNotNull(convertedDate);
        Assertions.assertEquals(1581354000000l, convertedDate.getTime());
    }

    @Test
    public void getMinutesDurationBetweenTwoDates_SUCCESS_simpleDate() {
        // Arrange
        // "dd-MMM-yy HH.MM.SS"
        String startDateTime = "15-DEC-20 09.10.00";
        String endDateTime = "15-DEC-20 09.20.00";

        // Act
        var duration = uut.getMinutesDurationBetweenTwoDates(startDateTime, endDateTime);

        // Assert
        Assertions.assertEquals(10, duration);
    }

    @Test
    public void getMinutesDurationBetweenTwoDates_SUCCESS_zonedDate() {
        // Arrange
        // "dd-MMM-yy HH.MM.SS"
        String startDateTime = ZonedDateTime.now(ZoneId.of("UTC")).toString();
        String endDateTime = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10l).toString();

        // Act
        var duration = uut.getMinutesDurationBetweenTwoDates(startDateTime, endDateTime);

        // Assert
        Assertions.assertEquals(10, duration);
    }

    @Test
    public void getMinutesDurationBetweenTwoDates_SUCCESS_YyMmDdDate() {
        // Arrange
        // "yyyy-MM-dd HH:mm:ss"
        String startDateTime = "2020-12-15 09:10:00";
        String endDateTime = "2020-12-15 09:20:00";

        // Act
        var duration = uut.getMinutesDurationBetweenTwoDates(startDateTime, endDateTime);

        // Assert
        Assertions.assertEquals(10, duration);
    }
}
