package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

@RunWith(MockitoJUnitRunner.class)
public class PathNodeXYControllerTest extends TestBase<PathNodeXYController> {

    @Test
    public void GetNodeXYForPath_Fail() throws SQLException {
        // Arrange
        when(mockStatement.executeQuery(isA(String.class))).thenThrow(new SQLException("Unit test exception"));

        // Act
        NodeXY[] data = uut.GetNodeXYForPath(-1);

        // Assert
        // verify everything was closed despite error
        verify(mockStatement).executeQuery(
                "select * from node_xy where node_xy_id in (select node_xy_id from path_node_xy where path_id = -1)");
        verify(mockRs, times(0)).getString(isA(String.class));
        verify(mockRs, times(0)).getBigDecimal(isA(String.class));
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(0, data.length);
    }

    @Test
    public void GetNodeXYForPath_Success() throws SQLException {
        // Arrange

        // Act
        NodeXY[] data = uut.GetNodeXYForPath(-1);

        // Assert
        // verify everything was closed despite error
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
        assertEquals(1, data.length);
    }
}