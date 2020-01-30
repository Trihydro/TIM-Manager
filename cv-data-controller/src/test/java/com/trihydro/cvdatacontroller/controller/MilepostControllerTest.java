package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
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
import java.util.List;

import com.trihydro.library.model.Milepost;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MilepostControllerTest {
    @Mock
    private Connection mockConnection;
    @Mock
    private Statement mockStatement;
    @Mock
    private PreparedStatement mockPreparedStatement;
    @Mock
    private ResultSet mockRs;

    private MilepostController uut;

    private String direction = "direction";
    private String route = "route";
    private double fromMilepost = 0d;
    private double toMilepost = 10d;
    private boolean mod = false;

    @Before
    public void setup() throws SQLException {
        uut = spy(new MilepostController());
        when(mockConnection.createStatement()).thenReturn(mockStatement);
        when(mockConnection.prepareStatement(isA(String.class))).thenReturn(mockPreparedStatement);
        doReturn(mockConnection).when(uut).GetConnectionPool();
        when(mockStatement.executeQuery(isA(String.class))).thenReturn(mockRs);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockRs);
        when(mockRs.next()).thenReturn(true).thenReturn(false);
    }

    @Test
    public void getMileposts_Success() throws SQLException {
        // Arrange

        // Act
        List<Milepost> data = uut.getMileposts();

        // Assert
        verify(mockStatement)
                .executeQuery("select * from MILEPOST_VW where MOD(milepost, 1) = 0 order by milepost asc");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, data.size());
    }

    @Test
    public void getMilepostRange_Direction_asc_Success() throws SQLException {
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
        List<Milepost> milePosts = uut.getMilepostRange(direction, route, fromMilepost, toMilepost);

        // Assert
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.size());
    }

    @Test
    public void getMilepostRange_Direction_desc_Success() throws SQLException {
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
        List<Milepost> milePosts = uut.getMilepostRange(direction, route, toMilepost, fromMilepost);

        // Assert
        verify(mockStatement).executeQuery(statementStr);
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.size());
    }

    @Test
    public void getMilepostsRoute_Success() throws SQLException {
        // Arrange

        // Act
        List<Milepost> milePosts = uut.getMilepostsRoute(route, mod);

        // Assert
        verify(mockStatement).executeQuery("select * from MILEPOST_VW where route like '%" + route + "%'");
        verify(mockRs).getString("route");
        verify(mockRs).getDouble("milepost");
        verify(mockRs).getString("direction");
        verify(mockRs).getDouble("latitude");
        verify(mockRs).getDouble("longitude");
        verify(mockRs).getDouble("elevation_ft");
        verify(mockRs).getDouble("bearing");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.size());
    }
    // TODO: pickup unit tests here

    @Test
    public void getMilepostRangeNoDirection_asc_Success() throws SQLException{
        // Arrange
        String statementStr = "select * from MILEPOST_VW where milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost asc";
        
        // Act
        List<Milepost> milePosts = uut.getMilepostRangeNoDirection(route, fromMilepost, toMilepost);

        // Assert
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("route");
        verify(mockRs).getDouble("milepost");
        verify(mockRs).getDouble("latitude");
        verify(mockRs).getDouble("longitude");
        verify(mockRs).getDouble("elevation_ft");
        verify(mockRs).getDouble("bearing");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.size());
    }

    @Test
    public void getMilepostRangeNoDirection_desc_Success() throws SQLException{
        // Arrange
        String statementStr = "select * from MILEPOST_VW where milepost between ";
        statementStr += fromMilepost;
        statementStr += " and " + toMilepost;
        statementStr += " and route like '%";
        statementStr += route;
        statementStr += "%' order by milepost desc";
        
        // Act
        List<Milepost> milePosts = uut.getMilepostRangeNoDirection(route, toMilepost, fromMilepost);

        // Assert
        verify(mockStatement).executeQuery(statementStr);
        verify(mockRs).getString("route");
        verify(mockRs).getDouble("milepost");
        verify(mockRs).getDouble("latitude");
        verify(mockRs).getDouble("longitude");
        verify(mockRs).getDouble("elevation_ft");
        verify(mockRs).getDouble("bearing");
        verify(mockStatement).close();
        verify(mockConnection).close();
        assertEquals(1, milePosts.size());
    }
}