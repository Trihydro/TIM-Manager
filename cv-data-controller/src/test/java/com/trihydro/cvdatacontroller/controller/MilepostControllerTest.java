package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trihydro.cvdatacontroller.services.MilepostService;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.WydotTim;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class MilepostControllerTest {
    private Coordinate startPoint;
    private Coordinate endPoint;
    private WydotTim wydotTim;

    @Mock
    MilepostService mockMilepostService;
    @Mock
    protected Connection mockConnection;
    @Mock
    protected Statement mockStatement;
    @Mock
    protected PreparedStatement mockPreparedStatement;
    @Mock
    protected ResultSet mockRs;

    @Spy
    @InjectMocks
    MilepostController uut;

    @Before
    public void setup() throws SQLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        lenient().when(mockConnection.createStatement()).thenReturn(mockStatement);
        lenient().when(mockConnection.prepareStatement(isA(String.class))).thenReturn(mockPreparedStatement);
        lenient().when(mockConnection.prepareStatement(isA(String.class), isA(String[].class)))
                .thenReturn(mockPreparedStatement);
        lenient().doReturn(mockConnection).when((BaseController) uut).GetConnectionPool();
        lenient().doReturn(-1l).when((BaseController) uut).executeAndLog(isA(PreparedStatement.class),
                isA(String.class));
        lenient().doReturn(true).when((BaseController) uut).updateOrDelete(mockPreparedStatement);
        lenient().when(mockStatement.executeQuery(isA(String.class))).thenReturn(mockRs);
        lenient().when(mockPreparedStatement.executeQuery()).thenReturn(mockRs);
        lenient().when(mockRs.next()).thenReturn(true).thenReturn(false);
    }

    private void setupWydotTim() {
        startPoint = new Coordinate(-1, -2);
        endPoint = new Coordinate(-3, -4);

        wydotTim = new WydotTim();
        wydotTim.setDirection("direction");
        wydotTim.setRoute("route");
        wydotTim.setStartPoint(startPoint);
        wydotTim.setEndPoint(endPoint);
    }

    @Test
    public void getMileposts_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<Milepost>> data = uut.getMileposts();

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement)
                .executeQuery("select * from MILEPOST_VW_NEW where MOD(milepost, 1) = 0 order by milepost asc");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, data.getBody().size());
    }

    @Test
    public void getMileposts_FAIL() throws SQLException {
        // Arrange
        when(mockRs.getString(isA(String.class))).thenThrow(new SQLException());
        // Act
        ResponseEntity<List<Milepost>> data = uut.getMileposts();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement)
                .executeQuery("select * from MILEPOST_VW_NEW where MOD(milepost, 1) = 0 order by milepost asc");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(0, data.getBody().size());
    }

    @Test
    public void getRoutes_SUCCESS() throws SQLException {
        // Arrange
        doReturn("common name").when(mockRs).getString("COMMON_NAME");

        // Act
        ResponseEntity<List<String>> data = uut.getRoutes();

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        assertEquals(1, data.getBody().size());
    }

    @Test
    public void getRoutes_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getString("COMMON_NAME");

        // Act
        ResponseEntity<List<String>> data = uut.getRoutes();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        assertEquals(0, data.getBody().size());
    }

    @Test
    public void getMilepostsByStartEndPoint_FAIL_startPoint() {
        // Arrange
        setupWydotTim();
        wydotTim.setStartPoint(null);

        // Act
        ResponseEntity<Collection<com.trihydro.cvdatacontroller.model.Milepost>> data = uut
                .getMilepostsByStartEndPoint(wydotTim);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
    }

    @Test
    public void getMilepostsByStartEndPoint_FAIL_endPoint() {
        // Arrange
        setupWydotTim();
        wydotTim.setEndPoint(null);

        // Act
        ResponseEntity<Collection<com.trihydro.cvdatacontroller.model.Milepost>> data = uut
                .getMilepostsByStartEndPoint(wydotTim);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
    }

    @Test
    public void getMilepostsByStartEndPoint_FAIL_direction() {
        // Arrange
        setupWydotTim();
        wydotTim.setDirection(null);

        // Act
        ResponseEntity<Collection<com.trihydro.cvdatacontroller.model.Milepost>> data = uut
                .getMilepostsByStartEndPoint(wydotTim);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
    }

    @Test
    public void getMilepostsByStartEndPoint_FAIL_route() {
        // Arrange
        setupWydotTim();
        wydotTim.setRoute(null);

        // Act
        ResponseEntity<Collection<com.trihydro.cvdatacontroller.model.Milepost>> data = uut
                .getMilepostsByStartEndPoint(wydotTim);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
    }

    @Test
    public void getMilepostsByStartEndPoint_SUCCESS() {
        // Arrange
        setupWydotTim();
        Collection<com.trihydro.cvdatacontroller.model.Milepost> resp = new ArrayList<>();
        resp.add(new com.trihydro.cvdatacontroller.model.Milepost());
        doReturn(resp).when(mockMilepostService).getPathWithBuffer(anyString(), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyString());

        // Act
        ResponseEntity<Collection<com.trihydro.cvdatacontroller.model.Milepost>> data = uut
                .getMilepostsByStartEndPoint(wydotTim);

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        assertEquals(1, data.getBody().size());
    }

}