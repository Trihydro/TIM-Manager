package com.trihydro.library.helpers;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Milepost;

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
        // "dd-MMM-yy HH.mm.ss"
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
        // "dd-MMM-yy HH.mm.ss"
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

    @Test
    public void getMinutesDurationBetweenTwoDates_SUCCESS_different_formats() {
        // Arrange
        String startDateTime = "2024-03-22T07:36:15.711Z[UTC]";
        String endDateTime = "2024-03-22T07:41:00.000Z";

        // Act
        var duration = uut.getMinutesDurationBetweenTwoDates(startDateTime, endDateTime);

        // Assert
        Assertions.assertEquals(4, duration);
    }

    @Test
    public void getMinutesDurationBetweenTwoDates_FAILURE_unrecognized_format() {
        // Arrange
        String startDateTime = "banana";
        String endDateTime = "2024-03-22T07:36:15.711Z";

        // Act
        var duration = uut.getMinutesDurationBetweenTwoDates(startDateTime, endDateTime);

        // Assert
        Assertions.assertEquals(-1, duration);
    }

    @Test
    public void calculateAnchorCoordinateExample1() {
        // Arrange
        BigDecimal firstLat = new BigDecimal(-29.944604);
        BigDecimal firstLon = new BigDecimal(-71.135100);
        BigDecimal secondLat = new BigDecimal(-29.945241);
        BigDecimal secondLon = new BigDecimal(-71.134510);
        Milepost firstMilepost = new Milepost();
        firstMilepost.setLatitude(firstLat);
        firstMilepost.setLongitude(firstLon);
        Milepost secondMilepost = new Milepost();
        secondMilepost.setLatitude(secondLat);
        secondMilepost.setLongitude(secondLon);

        // Act
        Coordinate result = uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);
        
        // Assert
        BigDecimal expectedLat = new BigDecimal(-29.944498794493846).round(new java.math.MathContext(8));
        BigDecimal expectedLon = new BigDecimal(-71.13519744309046).round(new java.math.MathContext(9));
        Assertions.assertEquals(expectedLat, result.getLatitude().round(new java.math.MathContext(8)));
        Assertions.assertEquals(expectedLon, result.getLongitude().round(new java.math.MathContext(9)));
    }

    @Test
    public void calculateAnchorCoordinateExample2() {
        // Arrange
        BigDecimal firstLat = new BigDecimal(-34.864022);
        BigDecimal firstLon = new BigDecimal(138.783680);
        BigDecimal secondLat = new BigDecimal(-34.864070);
        BigDecimal secondLon = new BigDecimal(138.779918);
        Milepost firstMilepost = new Milepost();
        firstMilepost.setLatitude(firstLat);
        firstMilepost.setLongitude(firstLon);
        Milepost secondMilepost = new Milepost();
        secondMilepost.setLatitude(secondLat);
        secondMilepost.setLongitude(secondLon);

        // Act
        Coordinate result = uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);
        
        // Assert
        BigDecimal expectedLat = new BigDecimal(-34.864019902550346).round(new java.math.MathContext(8));
        BigDecimal expectedLon = new BigDecimal(138.78384438761637).round(new java.math.MathContext(9));
        Assertions.assertEquals(expectedLat, result.getLatitude().round(new java.math.MathContext(8)));
        Assertions.assertEquals(expectedLon, result.getLongitude().round(new java.math.MathContext(9)));
    }

    @Test
    public void calculateAnchorCoordinateExample3() {
        // Arrange
        BigDecimal firstLat = new BigDecimal(36.879930);
        BigDecimal firstLon = new BigDecimal(139.924244);
        BigDecimal secondLat = new BigDecimal(36.874820);
        BigDecimal secondLon = new BigDecimal(139.918023);
        Milepost firstMilepost = new Milepost();
        firstMilepost.setLatitude(firstLat);
        firstMilepost.setLongitude(firstLon);
        Milepost secondMilepost = new Milepost();
        secondMilepost.setLatitude(secondLat);
        secondMilepost.setLongitude(secondLon);

        // Act
        Coordinate result = uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);
        
        // Assert
        BigDecimal expectedLat = new BigDecimal(36.88002664477771).round(new java.math.MathContext(8));
        BigDecimal expectedLon = new BigDecimal(139.92436165697887).round(new java.math.MathContext(9));
        Assertions.assertEquals(expectedLat, result.getLatitude().round(new java.math.MathContext(8)));
        Assertions.assertEquals(expectedLon, result.getLongitude().round(new java.math.MathContext(9)));
    }

    @Test
    public void calculateAnchorCoordinateExample4() {
        // Arrange
        BigDecimal firstLat = new BigDecimal(39.503475);
        BigDecimal firstLon = new BigDecimal(-106.147423);
        BigDecimal secondLat = new BigDecimal(39.50418);
        BigDecimal secondLon = new BigDecimal(-106.145944);
        Milepost firstMilepost = new Milepost();
        firstMilepost.setLatitude(firstLat);
        firstMilepost.setLongitude(firstLon);
        Milepost secondMilepost = new Milepost();
        secondMilepost.setLatitude(secondLat);
        secondMilepost.setLongitude(secondLon);

        // Act
        Coordinate result = uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);
        
        // Assert
        BigDecimal expectedLat = new BigDecimal(39.503404).round(new java.math.MathContext(8));
        BigDecimal expectedLon = new BigDecimal(-106.147572).round(new java.math.MathContext(9));
        Assertions.assertEquals(expectedLat, result.getLatitude().round(new java.math.MathContext(8)));
        Assertions.assertEquals(expectedLon, result.getLongitude().round(new java.math.MathContext(9)));
    }
}
