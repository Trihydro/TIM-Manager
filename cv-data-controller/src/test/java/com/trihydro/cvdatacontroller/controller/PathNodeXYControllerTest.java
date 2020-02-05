package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import com.trihydro.library.helpers.SQLNullHandler;
import com.trihydro.library.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

@RunWith(MockitoJUnitRunner.class)
public class PathNodeXYControllerTest extends TestBase<PathNodeXYController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimOracleTables mockTimOracleTables;

    @Before
    public void setupSubTest() {
        doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
        doReturn(-1l).when(uut).log(mockPreparedStatement, "pathId");
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void GetNodeXYForPath_FAIL() throws SQLException {
        // Arrange
        when(mockStatement.executeQuery(isA(String.class))).thenThrow(new SQLException("Unit test exception"));

        // Act
        ResponseEntity<NodeXY[]> data = uut.GetNodeXYForPath(-1);

        // Assert
        // verify everything was closed despite error
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockStatement).executeQuery(
                "select * from node_xy where node_xy_id in (select node_xy_id from path_node_xy where path_id = -1)");
        verify(mockRs, times(0)).getString(isA(String.class));
        verify(mockRs, times(0)).getBigDecimal(isA(String.class));
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(0, data.getBody().length);
    }

    @Test
    public void GetNodeXYForPath_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<NodeXY[]> data = uut.GetNodeXYForPath(-1);

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
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
        assertEquals(1, data.getBody().length);
    }

    @Test
    public void insertPathNodeXY_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<Long> data = uut.AddPathNodeXY(-1l, -1l);

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);// NODE_XY_ID
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 2, -1l);// PATH_ID
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void insertPathNodeXY_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);
        // Act
        ResponseEntity<Long> data = uut.AddPathNodeXY(-1l, -1l);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}