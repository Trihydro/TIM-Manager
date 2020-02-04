package com.trihydro.cvdatacontroller.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.trihydro.cvdatacontroller.helpers.SQLNullHandler;
import com.trihydro.cvdatacontroller.tables.TimOracleTables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Circle;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Geometry;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.DistanceUnits.DistanceUnitsEnum;

@RunWith(MockitoJUnitRunner.class)
public class RegionControllerTest extends TestBase<RegionController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimOracleTables mockTimOracleTables;

    @Before
    public void setupSubTest() throws SQLException {
        doReturn(mockPreparedStatement).when(mockTimOracleTables).buildUpdateStatement(any(), any(), any(), any(),
                any());
        doReturn("").when(mockTimOracleTables).buildInsertQueryStatement(any(), any());
        doReturn(mockPreparedStatement).when(mockConnection).prepareStatement("", new String[] { "region_id" });
        doReturn(true).when(uut).updateOrDelete(mockPreparedStatement);
        uut.InjectDependencies(mockTimOracleTables, mockSqlNullHandler);
    }

    @Test
    public void updateRegionName_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<Boolean> data = uut.UpdateRegionName(-1l, "name");

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        assertTrue("UpdateRegionName returned false when success", data.getBody());
    }

    @Test
    public void updateRegionName_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(uut).GetConnectionPool();

        // Act
        ResponseEntity<Boolean> data = uut.UpdateRegionName(-1l, "name");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        assertFalse("UpdateRegionName returned true when error", data.getBody());
    }

    @Test
    public void AddRegion_SUCCESS() throws SQLException {
        // Arrange
        Region region = new Region();
        Geometry geometry = new Geometry();
        Circle circle = new Circle();
        OdePosition3D position = new OdePosition3D();
        position.setLatitude(new BigDecimal(1));
        position.setLongitude(new BigDecimal(2));
        position.setElevation(new BigDecimal(3));
        circle.setPosition(position);
        circle.setRadius(4);
        circle.setUnits(DistanceUnitsEnum.centimeter);
        geometry.setCircle(circle);
        geometry.setLaneWidth(new BigDecimal(5));
        geometry.setExtent(6);
        geometry.setDirection("direction");
        region.setDirection("direction");
        region.setGeometry(geometry);
        region.setAnchorPosition(position);

        // Act
        ResponseEntity<Long> data = uut.AddRegion(-1l, -1l, region);

        // Assert
        assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);// DATA_FRAME_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, region.getName());// NAME
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 3, region.getLaneWidth());// LANE_WIDTH
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 4, region.getDirectionality());// DIRECTIONALITY
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5, region.getDirection());// DIRECTION
        verify(mockPreparedStatement).setBoolean(6, region.isClosedPath());// CLOSED_PATH
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 7, position.getLatitude());// ANCHOR_LAT
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 8, position.getLongitude());// ANCHOR_LONG
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 9, -1l);// PATH_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 10, null);// GEOMETRY_DIRECTION
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 11, null);// GEOMETRY_EXTENT
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 12, (BigDecimal) null);// GEOMETRY_LANE_WIDTH
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 13, (BigDecimal) null);// GEOMETRY_CIRCLE_POSITION_LAT
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 14, (BigDecimal) null);// GEOMETRY_CIRCLE_POSITION_LONG
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 15, (BigDecimal) null);// GEOMETRY_CIRCLE_POSITION_ELEV
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 16, null);// GEOMETRY_CIRCLE_RADIUS
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 17, null);// GEOMETRY_CIRCLE_UNITS
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddRegion_FAIL() throws SQLException {
        // Arrange
        Region region = new Region();
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);

        // Act
        ResponseEntity<Long> data = uut.AddRegion(-1l, -1l, region);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}