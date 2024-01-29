package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimDbTables;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

public class PathNodeXYControllerTest extends TestBase<PathNodeXYController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimDbTables mockTimDbTables;

    @BeforeEach
    public void setupSubTest() {
        uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
    }

    private void setupInsertQueryStatement(){
        doReturn("").when(mockTimDbTables).buildInsertQueryStatement(any(), any());
    }

    @Test
    public void GetNodeXYForPath_FAIL() throws SQLException {
        // Arrange
        when(mockStatement.executeQuery(isA(String.class))).thenThrow(new SQLException("Unit test exception"));

        // Act
        ResponseEntity<NodeXY[]> data = uut.GetNodeXYForPath(-1);

        // Assert
        // verify everything was closed despite error
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(
                "select * from node_xy where node_xy_id in (select node_xy_id from path_node_xy where path_id = -1)");
        verify(mockRs, times(0)).getString(isA(String.class));
        verify(mockRs, times(0)).getBigDecimal(isA(String.class));
        verify(mockStatement).close();
        verify(mockConnection).close();
        Assertions.assertEquals(0, data.getBody().length);
    }

    @Test
    public void GetNodeXYForPath_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<NodeXY[]> data = uut.GetNodeXYForPath(-1);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockStatement).executeQuery(
                "select * from node_xy where node_xy_id in (select node_xy_id from path_node_xy where path_id = -1)");
        verify(mockRs).getString("DELTA");
        verify(mockRs).getBigDecimal("NODE_LAT");
        verify(mockRs).getBigDecimal("NODE_LONG");
        verify(mockRs).getBigDecimal("X");
        verify(mockRs).getBigDecimal("Y");
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        Assertions.assertEquals(1, data.getBody().length);
    }

    @Test
    public void insertPathNodeXY_SUCCESS() throws SQLException {
        // Arrange
        setupInsertQueryStatement();

        // Act
        ResponseEntity<Long> data = uut.AddPathNodeXY(-1l, -1l);

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);// NODE_XY_ID
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 2, -1l);// PATH_ID
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void insertPathNodeXY_FAIL() throws SQLException {
        // Arrange
        setupInsertQueryStatement();
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);
        // Act
        ResponseEntity<Long> data = uut.AddPathNodeXY(-1l, -1l);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}