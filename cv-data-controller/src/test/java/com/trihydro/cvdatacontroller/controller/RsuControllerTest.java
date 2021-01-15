package com.trihydro.cvdatacontroller.controller;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RsuControllerTest extends TestBase<RsuController> {

    @Test
    public void SelectAllRsus_SUCCESS() throws SQLException {
        // Arrange
        String selectStatement = "select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid order by milepost asc";

        // Act
        ResponseEntity<List<WydotRsu>> data = uut.SelectAllRsus();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getInt("RSU_ID");
        verify(mockRs).getString("IPV4_ADDRESS");
        verify(mockRs).getBigDecimal("LATITUDE");
        verify(mockRs).getBigDecimal("LONGITUDE");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectAllRsus_FAIL() throws SQLException {
        // Arrange
        String selectStatement = "select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid order by milepost asc";
        doThrow(new SQLException()).when(mockRs).getInt("RSU_ID");
        // Act
        ResponseEntity<List<WydotRsu>> data = uut.SelectAllRsus();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectActiveRsus_SUCCESS() throws SQLException {
        // Arrange
        String selectStatement = "select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing'";

        // Act
        ResponseEntity<List<WydotRsu>> data = uut.SelectActiveRsus();

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getString("IPV4_ADDRESS");
        verify(mockRs).getBigDecimal("LATITUDE");
        verify(mockRs).getBigDecimal("LONGITUDE");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectActiveRsus_FAIL() throws SQLException {
        // Arrange
        String selectStatement = "select rsu.*, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.status = 'Existing'";
        doThrow(new SQLException()).when(mockRs).getString("IPV4_ADDRESS");

        // Act
        ResponseEntity<List<WydotRsu>> data = uut.SelectActiveRsus();

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetFullRsusTimIsOn_SUCCESS() throws SQLException {
        // Arrange
        Long timId = -1l;
        String selectStatement = "select rsu.*, tim_rsu.rsu_index, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address ";
        selectStatement += "from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid ";
        selectStatement += "inner join tim_rsu on tim_rsu.rsu_id = rsu.rsu_id where tim_rsu.tim_id = " + timId;

        // Act
        ResponseEntity<List<WydotRsuTim>> data = uut.GetFullRsusTimIsOn(timId);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getString("IPV4_ADDRESS");
        verify(mockRs).getBigDecimal("LATITUDE");
        verify(mockRs).getBigDecimal("LONGITUDE");
        verify(mockRs).getInt("RSU_INDEX");
        verify(mockRs).getString("UPDATE_USERNAME");
        verify(mockRs).getString("UPDATE_PASSWORD");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetFullRsusTimIsOn_FAIL() throws SQLException {
        // Arrange
        Long timId = -1l;
        String selectStatement = "select rsu.*, tim_rsu.rsu_index, rsu_vw.latitude, rsu_vw.longitude, rsu_vw.ipv4_address from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid inner join tim_rsu on tim_rsu.rsu_id = rsu.rsu_id where tim_rsu.tim_id = "
                + timId;
        doThrow(new SQLException()).when(mockRs).getString("IPV4_ADDRESS");

        // Act
        ResponseEntity<List<WydotRsuTim>> data = uut.GetFullRsusTimIsOn(timId);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectRsusByRoute_SUCCESS() throws SQLException {
        // Arrange
        String route = "I80";
        String selectStatement = "select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.route like '%"
                + route + "%' and rsu_vw.status = 'Existing' order by milepost asc";
        // Act
        ResponseEntity<ArrayList<WydotRsu>> data = uut.SelectRsusByRoute(route);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getInt("RSU_ID");
        verify(mockRs).getString("IPV4_ADDRESS");
        verify(mockRs).getBigDecimal("LATITUDE");
        verify(mockRs).getBigDecimal("LONGITUDE");
        verify(mockRs).getString("ROUTE");
        verify(mockRs).getDouble("MILEPOST");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void SelectRsusByRoute_FAIL() throws SQLException {
        // Arrange
        String route = "I80";
        String selectStatement = "select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid where rsu_vw.route like '%"
                + route + "%' and rsu_vw.status = 'Existing' order by milepost asc";
        doThrow(new SQLException()).when(mockRs).getInt("RSU_ID");
        // Act
        ResponseEntity<ArrayList<WydotRsu>> data = uut.SelectRsusByRoute(route);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetRsuClaimedIndexes_SUCCESS() throws SQLException {
        // Arrange
        when(mockRs.getInt("RSU_INDEX")).thenReturn(-1);
        var statement = "select rsu_index from active_tim inner join tim_rsu on active_tim.tim_id = tim_rsu.tim_id"
                + " where sat_record_id is null and rsu_id = ?";

        // Act
        var result = uut.GetRsuClaimedIndexes(123);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
        Assertions.assertEquals(1, result.getBody().size());
        Assertions.assertEquals(-1, result.getBody().get(0));

        verify(mockConnection).prepareStatement(statement);
        verify(mockPreparedStatement).setLong(1, 123);
        verify(mockPreparedStatement).executeQuery();
        verify(mockRs).getInt("RSU_INDEX");
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetRsuClaimedIndexes_FAIL() throws SQLException {
        // Arrange
        var statement = "select rsu_index from active_tim inner join tim_rsu on active_tim.tim_id = tim_rsu.tim_id"
                + " where sat_record_id is null and rsu_id = ?";
        when(mockPreparedStatement.executeQuery()).thenThrow(new SQLException());

        // Act
        var result = uut.GetRsuClaimedIndexes(123);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        verify(mockConnection).prepareStatement(statement);
        verify(mockPreparedStatement).setLong(1, 123);
        verify(mockPreparedStatement).executeQuery();
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}