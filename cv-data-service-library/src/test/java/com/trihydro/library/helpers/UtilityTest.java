package com.trihydro.library.helpers;

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
}
