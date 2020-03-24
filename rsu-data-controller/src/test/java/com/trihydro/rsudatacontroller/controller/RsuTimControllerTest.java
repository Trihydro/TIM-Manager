package com.trihydro.rsudatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.rsudatacontroller.model.RsuTim;
import com.trihydro.rsudatacontroller.service.RsuService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RsuTimControllerTest {
    @Mock
    RsuService mockRsuService;

    @Mock
    Utility mockUtility;

    @InjectMocks
    RsuTimController uut;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getAllDeliveryStartTimes_success() throws Exception {
        // Arrange
        when(mockRsuService.getAllDeliveryStartTimes("0.0.0.0")).thenReturn(Arrays.asList(new RsuTim()));
        // Act
        ResponseEntity<List<RsuTim>> result = uut.GetRsuTimsDeliveryStart("0.0.0.0");

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    public void getAllDeliveryStartTimes_badIpv4() {
        // Act
        ResponseEntity<List<RsuTim>> result = uut.GetRsuTimsDeliveryStart("");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(null, result.getBody());
    }

    @Test
    public void getAllDeliveryStartTimes_unresponsiveRsu() throws Exception {
        // Arrange
        when(mockRsuService.getAllDeliveryStartTimes("0.0.0.0")).thenReturn(null);

        // Act
        ResponseEntity<List<RsuTim>> result = uut.GetRsuTimsDeliveryStart("0.0.0.0");

        // Assert
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getStatusCode());
        assertEquals(null, result.getBody());
        verify(mockUtility).logWithDate(any());
    }

    @Test
    public void getAllDeliveryStartTimes_processError() throws Exception {
        // Arrange
        when(mockRsuService.getAllDeliveryStartTimes("0.0.0.0")).thenThrow(new Exception());

        // Act
        ResponseEntity<List<RsuTim>> result = uut.GetRsuTimsDeliveryStart("0.0.0.0");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals(null, result.getBody());
        verify(mockUtility).logWithDate(any());
    }
}