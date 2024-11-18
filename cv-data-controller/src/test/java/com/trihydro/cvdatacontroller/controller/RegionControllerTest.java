package com.trihydro.cvdatacontroller.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
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

import us.dot.its.jpo.ode.plugin.j2735.OdePosition3D;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Circle;
import us.dot.its.jpo.ode.plugin.j2735.OdeTravelerInformationMessage.DataFrame.Region.Geometry;
import us.dot.its.jpo.ode.plugin.j2735.timstorage.DistanceUnits.DistanceUnitsEnum;

public class RegionControllerTest extends TestBase<RegionController> {

    @Mock
    private SQLNullHandler mockSqlNullHandler;
    @Spy
    private TimDbTables mockTimDbTables;

    @BeforeEach
    public void setupSubTest() throws SQLException {
        uut.InjectDependencies(mockTimDbTables, mockSqlNullHandler);
    }

    private void setupInsertQueryStatement() {
        doReturn("").when(mockTimDbTables).buildInsertQueryStatement(any(), any());
    }

    @Test
    public void updateRegionName_SUCCESS() throws SQLException {
        // Arrange

        // Act
        ResponseEntity<Boolean> data = uut.UpdateRegionName(-1l, "name");

        // Assert
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
        Assertions.assertTrue(data.getBody(), "UpdateRegionName returned false when success");
    }

    @Test
    public void updateRegionName_FAIL() throws SQLException {
        // Arrange
        doThrow(new SQLException()).when(mockDbInteractions).getConnectionPool();

        // Act
        ResponseEntity<Boolean> data = uut.UpdateRegionName(-1l, "name");

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        Assertions.assertFalse(data.getBody(), "UpdateRegionName returned true when error");
    }

    @Test
    public void AddRegion_SUCCESS() throws SQLException {
        // Arrange
        setupInsertQueryStatement();
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
        Assertions.assertEquals(HttpStatus.OK, data.getStatusCode());
        verify(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);// DATA_FRAME_ID
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 2, region.getName());// NAME
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 3, region.getLaneWidth());// LANE_WIDTH
        verify(mockSqlNullHandler).setBigDecimalOrNull(mockPreparedStatement, 4, region.getDirectionality());// DIRECTIONALITY
        verify(mockSqlNullHandler).setStringOrNull(mockPreparedStatement, 5, region.getDirection());// DIRECTION
        verify(mockPreparedStatement).setInt(6, region.isClosedPath() ? 1 : 0);// CLOSED_PATH
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
        verify(mockSqlNullHandler).setIntegerOrNull(mockPreparedStatement, 17, null);// GEOMETRY_CIRCLE_UNITS
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }

    @Test
    public void AddRegion_FAIL() throws SQLException {
        // Arrange
        setupInsertQueryStatement();
        Region region = new Region();
        doThrow(new SQLException()).when(mockSqlNullHandler).setLongOrNull(mockPreparedStatement, 1, -1l);

        // Act
        ResponseEntity<Long> data = uut.AddRegion(-1l, -1l, region);

        // Assert
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, data.getStatusCode());
        verify(mockPreparedStatement).close();
        verify(mockConnection).close();
    }
}