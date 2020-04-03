package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.trihydro.library.model.WydotRsu;
import com.trihydro.library.model.WydotRsuTim;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class RsuControllerTest extends TestBase<RsuController> {

    @Test
    public void SelectAllRsus_SUCCESS() throws SQLException {
        // Arrange
        String selectStatement = "select * from rsu inner join rsu_vw on rsu.deviceid = rsu_vw.deviceid order by milepost asc";

        // Act
        ResponseEntity<List<WydotRsu>> data = uut.SelectAllRsus();

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getInt("RSU_ID");
        verify(mockRs).getString("IPV4_ADDRESS");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
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
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
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
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getString("IPV4_ADDRESS");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
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
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
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
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getString("IPV4_ADDRESS");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
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
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
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
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockRs).getInt("RSU_ID");
        verify(mockRs).getString("IPV4_ADDRESS");
        verify(mockRs).getDouble("LATITUDE");
        verify(mockRs).getDouble("LONGITUDE");
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
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(selectStatement);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }
}