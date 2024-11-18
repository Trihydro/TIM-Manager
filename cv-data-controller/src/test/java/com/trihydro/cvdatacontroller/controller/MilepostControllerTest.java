package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.trihydro.cvdatacontroller.services.MilepostService;
import com.trihydro.library.model.Coordinate;
import com.trihydro.library.model.Milepost;
import com.trihydro.library.model.MilepostBuffer;
import com.trihydro.library.model.WydotTim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MilepostControllerTest extends TestBase<MilepostController> {
    private Coordinate startPoint;
    private Coordinate endPoint;
    private WydotTim wydotTim;
    private String direction = "B";
    private String commonName = "commonName";
    private double fromMilepost = 0d;
    private double toMilepost = 10d;

    @Mock
    MilepostService mockMilepostService;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockMilepostService);
        
        startPoint = new Coordinate(BigDecimal.valueOf(-1), BigDecimal.valueOf(-2));
        endPoint = new Coordinate(BigDecimal.valueOf(-3), BigDecimal.valueOf(-4));
    }

    private void setupWydotTim() {
        wydotTim = new WydotTim();
        wydotTim.setDirection("direction");
        wydotTim.setRoute("route");
        wydotTim.setStartPoint(startPoint);
        wydotTim.setEndPoint(endPoint);
    }

    @Test
    public void getMilepostsCommonName_SUCCESS() throws SQLException {
        // Arrange
        String commonName = "I 80";

        // Act
        ResponseEntity<List<Milepost>> data = uut.getMilepostsCommonName(commonName, false);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement)
                .executeQuery(String.format("select * from MILEPOST_VW_NEW where COMMON_NAME = '%s'", commonName));
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, data.getBody().size());
    }

    @Test
    public void getMilepostsCommonName_modSUCCESS() throws SQLException {
        // Arrange
        String commonName = "I 80";

        // Act
        ResponseEntity<List<Milepost>> data = uut.getMilepostsCommonName(commonName, true);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(String
                .format("select * from MILEPOST_VW_NEW where COMMON_NAME = '%s' and MOD(milepost, 1) = 0", commonName));
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, data.getBody().size());
    }

    @Test
    public void getMilepostsCommonName_FAIL() throws SQLException {
        // Arrange
        String commonName = "I 80";
        when(mockRs.getString(isA(String.class))).thenThrow(new SQLException());
        // Act
        ResponseEntity<List<Milepost>> data = uut.getMilepostsCommonName(commonName, false);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement)
                .executeQuery(String.format("select * from MILEPOST_VW_NEW where COMMON_NAME = '%s'", commonName));
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(0, data.getBody().size());
    }

    @Test
    public void getRoutes_SUCCESS() throws SQLException {
        // Arrange
        doReturn("common name").when(mockRs).getString("COMMON_NAME");

        // Act
        ResponseEntity<List<String>> data = uut.getRoutes();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
    }

    @Test
    public void getRoutes_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockRs).getString("COMMON_NAME");

        // Act
        ResponseEntity<List<String>> data = uut.getRoutes();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
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
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
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
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
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
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
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
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, data.getStatusCode());
    }

    @Test
    public void getMilepostsByStartEndPoint_SUCCESS() {
        // Arrange
        setupWydotTim();
        Collection<com.trihydro.cvdatacontroller.model.Milepost> resp = new ArrayList<>();
        resp.add(new com.trihydro.cvdatacontroller.model.Milepost());
        doReturn(resp).when(mockMilepostService).getPathWithBuffer(anyString(), any(), any(), any(),
                any(), anyString());

        // Act
        ResponseEntity<Collection<com.trihydro.cvdatacontroller.model.Milepost>> data = uut
                .getMilepostsByStartEndPoint(wydotTim);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
    }

    @Test
    public void getMilepostsByPointWithBuffer_SUCCESS() {
        // Arrange
        Collection<com.trihydro.cvdatacontroller.model.Milepost> resp = new ArrayList<>();
        resp.add(new com.trihydro.cvdatacontroller.model.Milepost());
        doReturn(resp).when(mockMilepostService).getPathWithSpecifiedBuffer(anyString(), any(), any(),
                anyString(), anyDouble());
        MilepostBuffer mpb = new MilepostBuffer();
        mpb.setCommonName("route");
        mpb.setDirection("direction");
        mpb.setPoint(endPoint);
        mpb.setBufferMiles(1d);

        // Act
        ResponseEntity<Collection<com.trihydro.cvdatacontroller.model.Milepost>> data = uut
                .getMilepostsByPointWithBuffer(mpb);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
    }

    @Test
    public void getMilepostRangeNoDirection_asc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW_NEW where milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost asc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRangeNoDirection(commonName, fromMilepost,
                toMilepost);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("COMMON_NAME");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getBigDecimal("LATITUDE");
        verify(mockRs).getBigDecimal("LONGITUDE");
        // verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostRangeNoDirection_desc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW_NEW where milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost desc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRangeNoDirection(commonName, toMilepost,
                fromMilepost);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("COMMON_NAME");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getBigDecimal("LATITUDE");
        verify(mockRs).getBigDecimal("LONGITUDE");
        // verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostRangeNoDirection_FAIL() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW_NEW where milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost desc";
        when(mockRs.getString(isA(String.class))).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRangeNoDirection(commonName, toMilepost,
                fromMilepost);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        Assertions.assertEquals(0, milePosts.getBody().size());
    }

    @Test
    public void getMilepostRange_Direction_asc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW_NEW where direction = '";
        statementStr += direction;
        statementStr += "' and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost asc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRange(direction, commonName, fromMilepost,
                toMilepost);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostRange_Direction_desc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW_NEW where direction = '";
        statementStr += direction;
        statementStr += "' and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost desc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRange(direction, commonName, toMilepost,
                fromMilepost);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostRange_Direction_FAIL() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW_NEW where direction = '";
        statementStr += direction;
        statementStr += "' and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost desc";
        when(mockRs.getString("COMMON_NAME")).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRange(direction, commonName, toMilepost,
                fromMilepost);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(0, milePosts.getBody().size());
    }

}