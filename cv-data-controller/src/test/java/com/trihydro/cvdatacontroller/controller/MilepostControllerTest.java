package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.Milepost;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class MilepostControllerTest extends TestBase<MilepostController> {
    private String direction = "direction";
    private String route = "route";
    private double fromMilepost = 0d;
    private double toMilepost = 10d;
    private boolean mod = false;

    @Test
    public void getMileposts_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<Milepost>> data = uut.getMileposts();

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement)
                .executeQuery("select * from MILEPOST_VW where MOD(milepost, 1) = 0 order by milepost asc");
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
                .executeQuery("select * from MILEPOST_VW where MOD(milepost, 1) = 0 order by milepost asc");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(0, data.getBody().size());
    }

    @Test
    public void getMilepostRange_Direction_asc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where direction = '";
        statementStr += direction;
        statementStr += "' and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost asc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRange(direction, route, fromMilepost, toMilepost);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostRange_Direction_desc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where direction = '";
        statementStr += direction;
        statementStr += "' and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost desc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRange(direction, route, toMilepost, fromMilepost);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostRange_Direction_FAIL() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where direction = '";
        statementStr += direction;
        statementStr += "' and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost desc";
        when(mockRs.getString("ROUTE")).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRange(direction, route, toMilepost, fromMilepost);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(0, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsRoute_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsRoute(route, mod);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery("select * from MILEPOST_VW where route like '%" + route + "%'");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        verify(mockRs).getDouble("ELEVATION_FT");
        verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsRoute_FAIL() throws SQLException {
        // Arrange
        when(mockRs.getString(isA(String.class))).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsRoute(route, mod);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery("select * from MILEPOST_VW where route like '%" + route + "%'");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(0, milePosts.getBody().size());
    }

    @Test
    public void getMilepostRangeNoDirection_asc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost asc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRangeNoDirection(route, fromMilepost, toMilepost);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        verify(mockRs).getDouble("ELEVATION_FT");
        verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostRangeNoDirection_desc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost desc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRangeNoDirection(route, toMilepost, fromMilepost);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        verify(mockRs).getDouble("ELEVATION_FT");
        verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostRangeNoDirection_FAIL() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost desc";
        when(mockRs.getString(isA(String.class))).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRangeNoDirection(route, toMilepost, fromMilepost);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        assertEquals(0, milePosts.getBody().size());
    }

    @Test
    public void getMilepostTestRange_asc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_TEST where direction = '";
        statementStr += direction;
        statementStr += "' and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost asc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostTestRange(direction, route, fromMilepost, toMilepost);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        verify(mockRs).getDouble("ELEVATION_FT");
        verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostTestRange_desc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_TEST where direction = '";
        statementStr += direction;
        statementStr += "' and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost desc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostTestRange(direction, route, toMilepost, fromMilepost);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        verify(mockRs).getDouble("ELEVATION_FT");
        verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostTestRange_FAIL() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_TEST where direction = '";
        statementStr += direction;
        statementStr += "' and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost desc";
        when(mockRs.getString(isA(String.class))).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostTestRange(direction, route, toMilepost, fromMilepost);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        assertEquals(0, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsTest_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_TEST order by milepost asc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsTest();

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        verify(mockRs).getDouble("ELEVATION_FT");
        verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsTest_FAIL() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_TEST order by milepost asc";
        when(mockRs.getString(isA(String.class))).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsTest();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        assertEquals(0, milePosts.getBody().size());
    }
}