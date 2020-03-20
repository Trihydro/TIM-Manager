package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.model.Milepost;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Ignore
@RunWith(MockitoJUnitRunner.class)
public class MilepostControllerTest extends TestBase<MilepostController> {
    private String direction = "both";
    private String commonName = "commonName";
    private double fromMilepost = 0d;
    private double toMilepost = 10d;
    private double startLong = -105.406993;
    private double endLong = -105.360182;
    private double startLat = 42.76202563;
    private double endLat = 42.76341358;
    private boolean mod = false;

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
    public void getMilepostRange_Direction_asc_SUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where direction in ('B')";
        statementStr += " and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost asc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRange(direction, commonName, fromMilepost,
                toMilepost);

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
        String statementStr = "select * from MILEPOST_VW where direction in ('B')";
        statementStr += " and milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost desc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRange(direction, commonName, toMilepost,
                fromMilepost);

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
        String statementStr = "select * from MILEPOST_VW where direction in ('B')";
        statementStr += " and milepost between ";
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
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(0, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsCommonName_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsCommonName(commonName, mod);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery("select * from MILEPOST_VW where COMMON_NAME = '" + commonName + "'");
        verify(mockRs).getString("COMMON_NAME");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getString("DIRECTION");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        // verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsCommonName_FAIL() throws SQLException {
        // Arrange
        when(mockRs.getString(isA(String.class))).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsCommonName(commonName, mod);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery("select * from MILEPOST_VW where COMMON_NAME = '" + commonName + "'");
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
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost asc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRangeNoDirection(commonName, fromMilepost,
                toMilepost);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("COMMON_NAME");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        // verify(mockRs).getDouble("BEARING");
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
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost desc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRangeNoDirection(commonName, toMilepost,
                fromMilepost);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("COMMON_NAME");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        // verify(mockRs).getDouble("BEARING");
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
        statementStr += " and common_name = '";
        statementStr += commonName;
        statementStr += "' order by milepost desc";
        when(mockRs.getString(isA(String.class))).thenThrow(new SQLException());

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostRangeNoDirection(commonName, toMilepost,
                fromMilepost);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        assertEquals(0, milePosts.getBody().size());
    }

    @Test
    public void routeExists_SUCCESS() {
        // Arrange

        // Act
        ResponseEntity<Boolean> data = uut.routeExists("route");

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        assertTrue("routeExists failed", data.getBody());
    }

    @Test
    public void routeExists_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockPreparedStatement).executeQuery();

        // Act
        ResponseEntity<Boolean> data = uut.routeExists("route");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        assertFalse("routeExists succeeded when exception", data.getBody());
    }

    @Test
    public void getMilepostsByLongitudeRange_ascSUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where direction in ('B') and longitude between ";
        statementStr += startLong + " and ";
        statementStr += endLong;
        statementStr += " and common_name = '" + commonName + "'";
        statementStr += " order by milepost, longitude";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsByLongitudeRange(direction, startLong, endLong,
                commonName);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("COMMON_NAME");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        // verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsByLongitudeRange_descSUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where direction in ('B') and longitude between ";
        statementStr += endLong + " and ";
        statementStr += startLong;
        statementStr += " and common_name = '" + commonName + "'";
        statementStr += " order by milepost desc, longitude desc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsByLongitudeRange(direction, endLong, startLong,
                commonName);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("COMMON_NAME");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        // verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsByLongitudeRange_FAIL() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where direction in ('B') and longitude between ";
        statementStr += endLong + " and ";
        statementStr += startLong;
        statementStr += " and common_name = '" + commonName + "'";
        statementStr += " order by milepost desc, longitude desc";
        doThrow(new SQLException()).when(mockRs).getString("COMMON_NAME");

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsByLongitudeRange(direction, endLong, startLong,
                commonName);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
        assertEquals(0, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsByLatitudeRange_ascSUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where direction in ('B') and latitude between ";
        statementStr += startLat + " and ";
        statementStr += endLat;
        statementStr += " and common_name = '" + commonName + "'";
        statementStr += " order by milepost, latitude";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsByLatitudeRange(direction, startLat, endLat,
                commonName);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("COMMON_NAME");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        // verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsByLatitudeRange_descSUCCESS() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where direction in ('B') and latitude between ";
        statementStr += endLat + " and ";
        statementStr += startLat;
        statementStr += " and common_name = '" + commonName + "'";
        statementStr += " order by milepost desc, latitude desc";

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsByLatitudeRange(direction, endLat, startLat,
                commonName);

        // Assert
        assertEquals(HttpStatus.OK, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("COMMON_NAME");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
        // verify(mockRs).getDouble("BEARING");
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.getBody().size());
    }

    @Test
    public void getMilepostsByLatitudeRange_FAIL() throws SQLException {
        // Arrange
        String statementStr = "select * from MILEPOST_VW where direction in ('B') and latitude between ";
        statementStr += endLat + " and ";
        statementStr += startLat;
        statementStr += " and common_name = '" + commonName + "'";
        statementStr += " order by milepost desc, latitude desc";
        doThrow(new SQLException()).when(mockRs).getString("COMMON_NAME");

        // Act
        ResponseEntity<List<Milepost>> milePosts = uut.getMilepostsByLatitudeRange(direction, endLat, startLat,
                commonName);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, milePosts.getStatusCode());
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockRs).close();
        verify(mockConnection).close();
        assertEquals(0, milePosts.getBody().size());
    }
}