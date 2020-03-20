package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class TimRsuControllerTest extends TestBase<TimRsuController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimOracleTables mockTimOracleTables;

    @Before
    public void setupSubTest() {
        doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void AddTimRsu_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<Long> data = uut.AddTimRsu(-1l, -1, 99);

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);// TIM_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 2, -1);// RSU_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 3, 99);// RSU_INDEX
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddTimRsu_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);

        // Act
        ResponseEntity<Long> data = uut.AddTimRsu(-1l, -1, 99);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void GetTimRsusByTimId_SUCCESS() throws SQLException {
        // Arrange
        Long timId = -1l;

        // Act
        ResponseEntity<List<TimRsu>> data = uut.GetTimRsusByTimId(timId);

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        assertEquals(1, data.getBody().size());
        verify(mockStatement).executeQuery("select * from TIM_RSU where tim_id = " + timId);
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getLong("RSU_ID");
        verify(mockRs).getInt("RSU_INDEX");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetTimRsusByTimId_FAIL() throws SQLException {
        // Arrange
        Long timId = -1l;
        doThrow(new SQLException()).when(mockRs).getLong("TIM_ID");

        // Act
        ResponseEntity<List<TimRsu>> data = uut.GetTimRsusByTimId(timId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        assertEquals(0, data.getBody().size());
        verify(mockStatement).executeQuery("select * from TIM_RSU where tim_id = " + timId);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetTimRsu_SUCCESS() throws SQLException {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;

        // Act
        ResponseEntity<TimRsu> data = uut.GetTimRsu(timId, rsuId);

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        assertNotNull(data.getBody());
        verify(mockStatement).executeQuery("select * from TIM_RSU where rsu_id = " + rsuId + " and tim_id = " + timId);
        verify(mockRs).getLong("TIM_RSU_ID");
        verify(mockRs).getLong("TIM_ID");
        verify(mockRs).getLong("RSU_ID");
        verify(mockRs).getInt("RSU_INDEX");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }

    @Test
    public void GetTimRsu_FAIL() throws SQLException {
        // Arrange
        Long timId = -1l;
        int rsuId = -2;
        doThrow(new SQLException()).when(mockRs).getLong("TIM_RSU_ID");

        // Act
        ResponseEntity<TimRsu> data = uut.GetTimRsu(timId, rsuId);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        assertNull(data.getBody());
        verify(mockStatement).executeQuery("select * from TIM_RSU where rsu_id = " + rsuId + " and tim_id = " + timId);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }
}