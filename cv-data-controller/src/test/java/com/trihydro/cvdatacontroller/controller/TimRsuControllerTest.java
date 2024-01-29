package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.sql.SQLException;
import java.util.List;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.model.TimRsu;
import com.trihydro.library.tables.TimDbTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TimRsuControllerTest extends TestBase<TimRsuController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimDbTables mockTimDbTables;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
    }

    private void setupInsertQueryStatement() {
        doReturn("").when(mockTimDbTables).buildInsertQueryStatement(any(), any());
    }

    @Test
    public void AddTimRsu_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<Long> data = uut.AddTimRsu(-1l, -1, 99);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);// TIM_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 2, -1);// RSU_ID
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 3, 99);// RSU_INDEX
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddTimRsu_FAIL() throws SQLException {
        // Arrange
        setupInsertQueryStatement();
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);

        // Act
        ResponseEntity<Long> data = uut.AddTimRsu(-1l, -1, 99);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
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
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertEquals(1, data.getBody().size());
        verify(mockStatement).executeQuery("select rsu_id,tim_id,rsu_index from tim_rsu where tim_id = " + timId
                + " group by rsu_id,tim_id,rsu_index");
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
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertEquals(0, data.getBody().size());
        verify(mockStatement).executeQuery("select rsu_id,tim_id,rsu_index from tim_rsu where tim_id = " + timId
                + " group by rsu_id,tim_id,rsu_index");
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
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        Assertions.assertNotNull(data.getBody());
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
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertNull(data.getBody());
        verify(mockStatement).executeQuery("select * from TIM_RSU where rsu_id = " + rsuId + " and tim_id = " + timId);
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
    }
}