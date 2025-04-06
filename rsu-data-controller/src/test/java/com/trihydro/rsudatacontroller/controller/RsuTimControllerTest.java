package com.trihydro.rsudatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import com.trihydro.library.helpers.Utility;
import com.trihydro.rsudatacontroller.model.RsuTim;
import com.trihydro.rsudatacontroller.service.RsuService;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class RsuTimControllerTest {
    @Mock
    RsuService mockRsuService;

    @Mock
    Utility mockUtility;

    @InjectMocks
    RsuTimController uut;

    @Test
    public void getAllDeliveryStartTimes_success() throws Exception {
        // Arrange
        when(mockRsuService.getAllDeliveryStartTimes("0.0.0.0")).thenReturn(Arrays.asList(new RsuTim()));
        // Act
        ResponseEntity<List<RsuTim>> result = uut.GetRsuTimsDeliveryStart("0.0.0.0");

        // Assert
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(1, result.getBody().size());
    }

    @Test
    public void getAllDeliveryStartTimes_badIpv4() {
        // Act
        ResponseEntity<List<RsuTim>> result = uut.GetRsuTimsDeliveryStart("");

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Assertions.assertEquals(null, result.getBody());
    }

    @Test
    public void getAllDeliveryStartTimes_unresponsiveRsu() throws Exception {
        // Arrange
        when(mockRsuService.getAllDeliveryStartTimes("0.0.0.0")).thenReturn(null);

        // Act
        ResponseEntity<List<RsuTim>> result = uut.GetRsuTimsDeliveryStart("0.0.0.0");

        // Assert
        Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, result.getStatusCode());
        Assertions.assertEquals(null, result.getBody());
        verify(mockUtility);
        System.out.println(ArgumentMatchers.<String>any());
    }

    @Test
    public void getAllDeliveryStartTimes_processError() throws Exception {
        // Arrange
        when(mockRsuService.getAllDeliveryStartTimes("0.0.0.0")).thenThrow(new Exception());

        // Act
        ResponseEntity<List<RsuTim>> result = uut.GetRsuTimsDeliveryStart("0.0.0.0");

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        Assertions.assertEquals(null, result.getBody());
        verify(mockUtility);
        System.out.println(ArgumentMatchers.<String>any());
    }
}