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
        Assertions.assertEquals(1603896780000L, convertedDate.getTime());
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
        Assertions.assertEquals(1603896780000L, convertedDate.getTime());
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
        Assertions.assertEquals(1603896780123L, convertedDate.getTime());
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
        Assertions.assertEquals(1581354000000L, convertedDate.getTime());
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
        String endDateTime = ZonedDateTime.now(ZoneId.of("UTC")).plusMinutes(10L).toString();

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
    public void calculateAnchorCoordinateExample1() throws Utility.IdenticalPointsException {
        // Arrange
        BigDecimal firstLat = BigDecimal.valueOf(-29.944604);
        BigDecimal firstLon = BigDecimal.valueOf(-71.135100);
        BigDecimal secondLat = BigDecimal.valueOf(-29.945241);
        BigDecimal secondLon = BigDecimal.valueOf(-71.134510);
        Milepost firstMilepost = new Milepost(firstLat, firstLon);
        Milepost secondMilepost = new Milepost(secondLat, secondLon);

        // Act
        Coordinate result = uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);

        // Assert
        BigDecimal expectedLat =
            BigDecimal.valueOf(-29.944498794493846).round(new java.math.MathContext(8));
        BigDecimal expectedLon =
            BigDecimal.valueOf(-71.13519744309046).round(new java.math.MathContext(9));
        Assertions.assertEquals(expectedLat,
            result.getLatitude().round(new java.math.MathContext(8)));
        Assertions.assertEquals(expectedLon,
            result.getLongitude().round(new java.math.MathContext(9)));
    }

    @Test
    public void calculateAnchorCoordinateExample2() throws Utility.IdenticalPointsException {
        // Arrange
        BigDecimal firstLat = BigDecimal.valueOf(-34.864022);
        BigDecimal firstLon = BigDecimal.valueOf(138.783680);
        BigDecimal secondLat = BigDecimal.valueOf(-34.864070);
        BigDecimal secondLon = BigDecimal.valueOf(138.779918);
        Milepost firstMilepost = new Milepost(firstLat, firstLon);
        Milepost secondMilepost = new Milepost(secondLat, secondLon);

        // Act
        Coordinate result = uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);

        // Assert
        BigDecimal expectedLat =
            BigDecimal.valueOf(-34.864019902550346).round(new java.math.MathContext(8));
        BigDecimal expectedLon =
            BigDecimal.valueOf(138.78384438761637).round(new java.math.MathContext(9));
        Assertions.assertEquals(expectedLat,
            result.getLatitude().round(new java.math.MathContext(8)));
        Assertions.assertEquals(expectedLon,
            result.getLongitude().round(new java.math.MathContext(9)));
    }

    @Test
    public void calculateAnchorCoordinateExample3() throws Utility.IdenticalPointsException {
        // Arrange
        BigDecimal firstLat = BigDecimal.valueOf(36.879930);
        BigDecimal firstLon = BigDecimal.valueOf(139.924244);
        BigDecimal secondLat = BigDecimal.valueOf(36.874820);
        BigDecimal secondLon = BigDecimal.valueOf(139.918023);
        Milepost firstMilepost = new Milepost(firstLat, firstLon);
        Milepost secondMilepost = new Milepost(secondLat, secondLon);

        // Act
        Coordinate result = uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);

        // Assert
        BigDecimal expectedLat =
            BigDecimal.valueOf(36.88002664477771).round(new java.math.MathContext(8));
        BigDecimal expectedLon =
            BigDecimal.valueOf(139.92436165697887).round(new java.math.MathContext(9));
        Assertions.assertEquals(expectedLat,
            result.getLatitude().round(new java.math.MathContext(8)));
        Assertions.assertEquals(expectedLon,
            result.getLongitude().round(new java.math.MathContext(9)));
    }

    @Test
    public void calculateAnchorCoordinateExample4() throws Utility.IdenticalPointsException {
        // Arrange
        BigDecimal firstLat = BigDecimal.valueOf(39.503475);
        BigDecimal firstLon = BigDecimal.valueOf(-106.147423);
        BigDecimal secondLat = BigDecimal.valueOf(39.50418);
        BigDecimal secondLon = BigDecimal.valueOf(-106.145944);
        Milepost firstMilepost = new Milepost(firstLat, firstLon);
        Milepost secondMilepost = new Milepost(secondLat, secondLon);

        // Act
        Coordinate result = uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);

        // Assert
        BigDecimal expectedLat = BigDecimal.valueOf(39.503404).round(new java.math.MathContext(8));
        BigDecimal expectedLon =
            BigDecimal.valueOf(-106.147572).round(new java.math.MathContext(9));
        Assertions.assertEquals(expectedLat,
            result.getLatitude().round(new java.math.MathContext(8)));
        Assertions.assertEquals(expectedLon,
            result.getLongitude().round(new java.math.MathContext(9)));
    }

    @Test
    public void calculateAnchorCoordinate_identicalPoints_zero() {
        // Arrange
        BigDecimal lat = BigDecimal.valueOf(0);
        BigDecimal lon = BigDecimal.valueOf(0);
        Milepost firstMilepost = new Milepost(lat, lon);
        Milepost secondMilepost = new Milepost(lat, lon);

        // Act & Assert
        Assertions.assertThrows(Utility.IdenticalPointsException.class, () -> {
            uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);
        });
    }

    @Test
    public void calculateAnchorCoordinate_identicalPoints_positiveLat_negativeLon() {
        // Arrange
        BigDecimal lat = BigDecimal.valueOf(40);
        BigDecimal lon = BigDecimal.valueOf(-100);
        Milepost firstMilepost = new Milepost(lat, lon);
        Milepost secondMilepost = new Milepost(lat, lon);

        // Act & Assert
        Assertions.assertThrows(Utility.IdenticalPointsException.class, () -> {
            uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);
        });
    }

    @Test
    public void calculateAnchorCoordinate_identicalPoints_negativeLat_positiveLon() {
        // Arrange
        BigDecimal lat = BigDecimal.valueOf(-40);
        BigDecimal lon = BigDecimal.valueOf(100);
        Milepost firstMilepost = new Milepost(lat, lon);
        Milepost secondMilepost = new Milepost(lat, lon);

        // Act & Assert
        Assertions.assertThrows(Utility.IdenticalPointsException.class, () -> {
            uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);
        });
    }

    @Test
    public void calculateAnchorCoordinate_identicalPoints_positiveLat_positiveLon() {
        // Arrange
        BigDecimal lat = BigDecimal.valueOf(-40);
        BigDecimal lon = BigDecimal.valueOf(100);
        Milepost firstMilepost = new Milepost(lat, lon);
        Milepost secondMilepost = new Milepost(lat, lon);

        // Act & Assert
        Assertions.assertThrows(Utility.IdenticalPointsException.class, () -> {
            uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);
        });
    }

    @Test
    public void calculateAnchorCoordinate_identicalPoints_negativeLat_negativeLon() {
        // Arrange
        BigDecimal lat = BigDecimal.valueOf(-40);
        BigDecimal lon = BigDecimal.valueOf(-100);
        Milepost firstMilepost = new Milepost(lat, lon);
        Milepost secondMilepost = new Milepost(lat, lon);

        // Act & Assert
        Assertions.assertThrows(Utility.IdenticalPointsException.class, () -> {
            uut.calculateAnchorCoordinate(firstMilepost, secondMilepost);
        });
    }

}