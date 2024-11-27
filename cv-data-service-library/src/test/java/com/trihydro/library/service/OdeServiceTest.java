package com.trihydro.library.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.OdeProps;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.WydotRsu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
public class OdeServiceTest extends BaseServiceTest {

    @Mock
    Utility mockUtility;
    @Mock
    OdeProps mockOdeProps;

    @InjectMocks
    private OdeService uut;

    @Test
    public void submitTimQuery_wydotRsu_success() {
        // Arrange
        WydotRsu rsu = new WydotRsu();
        rsu.setRsuId(-1);
        rsu.setRsuTarget("10.10.10.10");
        rsu.setLatitude(new BigDecimal(41.0000));
        rsu.setLongitude(new BigDecimal(-104.000000));
        rsu.setRoute("I 80");
        rsu.setMilepost(10d);
        doReturn("url").when(mockOdeProps).getOdeUrl();

        doReturn("{\"indicies_set\":\"[]\"}").when(mockRestTemplate).postForObject(anyString(), any(), any());

        // Act
        TimQuery timQuery = uut.submitTimQuery(rsu, 1);

        // Assert
        verify(mockRestTemplate).postForObject(anyString(), any(), any());
        Assertions.assertEquals(0, timQuery.getIndicies_set().size());
    }

}
