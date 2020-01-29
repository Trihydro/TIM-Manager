package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.trihydro.cvdatacontroller.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.NodeXY;

@RunWith(MockitoJUnitRunner.class)
public class PathNodeXYControllerTest {
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockRs;
    @Mock
    private TimOracleTables mockTimOracleTables;

    private PathNodeXYController uut;

    @Before
    public void setup() throws SQLException {
        uut = spy(new PathNodeXYController());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(isA(String.class))).thenReturn(mockPreparedStatement);
        doReturn(mockConnection).when(uut).GetConnectionPool();
        when(mockStatement.executeQuery(isA(String.class))).thenReturn(mockRs);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true).thenReturn(false);

        doReturn(mockPreparedStatement).when(mockTimOracleTables).buildUpdateStatement(any(), any(), any(), any(),
                any());
    }

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
        verify(mockStatement).close();
        verify(mockConnection).close();
        verify(mockRs).close();
        assertEquals(1, data.length);
    }
}