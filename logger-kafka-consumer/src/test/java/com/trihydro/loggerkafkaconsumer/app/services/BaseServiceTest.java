package com.trihydro.loggerkafkaconsumer.app.services;

import com.trihydro.library.helpers.DbInteractions;
import com.trihydro.library.helpers.Utility;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BaseServiceTest {

    @Mock
    private DbInteractions mockDbInteractions;
    @Mock
    private Utility mockUtility;

    @InjectMocks
    private BaseService uut;

    @Test
    public void convertDate_min_SUCCESS() {
        // Arrange
        var date = "2020-10-28T14:53Z";

        // Act
        var convertedDate = uut.convertDate(date);

        // Assert
        Assertions.assertNotNull(convertedDate);
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
    }

}
