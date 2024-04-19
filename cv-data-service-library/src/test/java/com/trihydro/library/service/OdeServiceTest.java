package com.trihydro.library.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.trihydro.library.helpers.Utility;
import com.trihydro.library.model.OdeProps;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.WydotRsu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;

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
        when(mockOdeProps.getOdeUrl()).thenReturn("url");
        var url = "url/tim/query";

        HttpEntity<String> entity = getEntity("{\"rsuTarget\":\"10.10.10.10\",\"rsuRetries\":3,\"rsuTimeout\":5000,\"rsuIndex\":0,\"snmpProtocol\":\"FOURDOT1\"}", String.class);
        when(mockRestTemplate.postForObject(url, entity, String.class))
        .thenReturn("{\"indicies_set\":\"[]\"}");

        // Act
        TimQuery timQuery = uut.submitTimQuery(rsu, 1);

        // Assert
        verify(mockRestTemplate).postForObject(url, entity, String.class);
        Assertions.assertNotNull(timQuery);
        Assertions.assertEquals(0, timQuery.getIndicies_set().size());
    }

}
