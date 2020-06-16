package com.trihydro.odewrapper.controller;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.trihydro.library.model.TimQuery;
import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.service.ActiveTimService;
import com.trihydro.library.service.OdeService;
import com.trihydro.library.service.TimTypeService;
import com.trihydro.odewrapper.config.BasicConfiguration;
import com.trihydro.odewrapper.controller.UtilityController.RsuClearSuccess;
import com.trihydro.odewrapper.model.TimDeleteSummary;
import com.trihydro.odewrapper.service.WydotTimService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner.StrictStubs;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(StrictStubs.class)
public class UtilityControllerTest {
    @Mock
    BasicConfiguration mockConfiguration;
    @Mock
    WydotTimService mockWydotTimService;
    @Mock
    TimQuery mockTimQuery;
    @Mock
    TimTypeService mockTimTypeService;
    @Mock
    OdeService mockOdeService;
    @Mock
    ActiveTimService mockActiveTimService;

    @InjectMocks
    private UtilityController uut;

    private ArrayList<WydotRsu> wydotRsus;
    private String rsuTarget = "a.b.c.d";
    private String rsuTarget2 = "1.2.3.4";
    protected Gson gson = new Gson();

    @Before
    public void setup() throws Exception {
        List<Integer> indices = new ArrayList<>();
        indices.add(1);
        indices.add(2);
        when(mockTimQuery.getIndicies_set()).thenReturn(indices);

        wydotRsus = new ArrayList<>();
        WydotRsu wydotRsu = new WydotRsu();
        wydotRsu.setRsuTarget(rsuTarget);
        wydotRsus.add(wydotRsu);
        wydotRsu = new WydotRsu();
        wydotRsu.setRsuTarget(rsuTarget2);
        wydotRsus.add(wydotRsu);
    }

    @Test
    public void clearRsu_NullAddress() {
        // Arrange

        // Act
        ResponseEntity<String> result = uut.clearRsu(null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("No addresses supplied", result.getBody());
    }

    @Test
    public void clearRsu_EmptyAddress() {
        // Arrange
        String[] addresses = new String[0];
        // Act
        ResponseEntity<String> result = uut.clearRsu(addresses);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("No addresses supplied", result.getBody());
    }

    @Test
    public void clearRsu_NullRsu() {
        // Arrange
        String[] addresses = new String[1];
        addresses[0] = rsuTarget;

        when(mockWydotTimService.getRsus()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<String> result = uut.clearRsu(addresses);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Type typeOfT = new TypeToken<List<RsuClearSuccess>>() {
        }.getType();
        List<RsuClearSuccess> returnMessages = gson.fromJson(result.getBody(), typeOfT);
        assertFalse(returnMessages.isEmpty());
        assertEquals(rsuTarget, returnMessages.get(0).rsuTarget);
        assertEquals(false, returnMessages.get(0).success);
        assertEquals("RSU not found for provided address", returnMessages.get(0).errMessage);
    }

    @Test
    public void clearRsu_NullTimQuery() {
        // Arrange
        String[] addresses = new String[1];
        addresses[0] = rsuTarget;
        when(mockConfiguration.getOdeUrl()).thenReturn("ode_url");
        when(mockWydotTimService.getRsus()).thenReturn(wydotRsus);
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class), isA(String.class)))
                .thenReturn(null);

        // Act
        ResponseEntity<String> result = uut.clearRsu(addresses);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Type typeOfT = new TypeToken<List<RsuClearSuccess>>() {
        }.getType();
        List<RsuClearSuccess> returnMessages = gson.fromJson(result.getBody(), typeOfT);
        assertFalse(returnMessages.isEmpty());
        assertEquals(rsuTarget, returnMessages.get(0).rsuTarget);
        assertEquals(false, returnMessages.get(0).success);
        assertEquals("Querying RSU indexes failed", returnMessages.get(0).errMessage);
    }

    @Test
    public void clearRsu_Success() {
        // Arrange
        String[] addresses = new String[1];
        addresses[0] = rsuTarget;

        when(mockConfiguration.getOdeUrl()).thenReturn("ode_url");
        when(mockWydotTimService.getRsus()).thenReturn(wydotRsus);
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class), isA(String.class)))
                .thenReturn(mockTimQuery);

        // Act
        ResponseEntity<String> result = uut.clearRsu(addresses);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Type typeOfT = new TypeToken<List<RsuClearSuccess>>() {
        }.getType();
        List<RsuClearSuccess> returnMessages = gson.fromJson(result.getBody(), typeOfT);
        assertFalse(returnMessages.isEmpty());
        assertEquals(rsuTarget, returnMessages.get(0).rsuTarget);
        assertEquals(true, returnMessages.get(0).success);
        verify(mockWydotTimService, times(2)).deleteTimFromRsu(isA(WydotRsu.class), isA(Integer.class));
    }

    @Test
    public void clearRsu_MultipleSuccess() {
        // Arrange
        String[] addresses = new String[2];
        addresses[0] = rsuTarget;
        addresses[1] = rsuTarget2;

        when(mockConfiguration.getOdeUrl()).thenReturn("ode_url");
        when(mockWydotTimService.getRsus()).thenReturn(wydotRsus);
        when(mockOdeService.submitTimQuery(isA(WydotRsu.class), isA(Integer.class), isA(String.class)))
                .thenReturn(mockTimQuery);

        // Act
        ResponseEntity<String> result = uut.clearRsu(addresses);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        Type typeOfT = new TypeToken<List<RsuClearSuccess>>() {
        }.getType();
        List<RsuClearSuccess> returnMessages = gson.fromJson(result.getBody(), typeOfT);
        assertFalse(returnMessages.isEmpty());
        assertEquals(2, returnMessages.size());
        assertEquals(rsuTarget, returnMessages.get(0).rsuTarget);
        assertEquals(true, returnMessages.get(0).success);
        verify(mockWydotTimService, times(4)).deleteTimFromRsu(isA(WydotRsu.class), isA(Integer.class));
    }

    @Test
    public void deleteTims_Success() {
        // Arrange
        var summary = new TimDeleteSummary();
        doReturn(summary).when(mockWydotTimService).deleteTimsFromRsusAndSdx(any());
        var aTimIds = new ArrayList<Long>();

        // Act
        var result = uut.deleteTims(aTimIds);

        // Assert
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertThat(result.getBody(), instanceOf(TimDeleteSummary.class));
        assertNotNull((TimDeleteSummary)result.getBody());
        verify(mockWydotTimService).deleteTimsFromRsusAndSdx(any());
    }
}